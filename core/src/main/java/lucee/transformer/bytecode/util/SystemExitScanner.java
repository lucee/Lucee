package lucee.transformer.bytecode.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.FileWrapper;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class SystemExitScanner {

	private static final String MSG = "found a match";
	private static Boolean validateSystemExit;
	private static final SerializableObject token = new SerializableObject();

	public static boolean has(File file) throws IOException {
		JarFile jarFile = new JarFile(file);
		Enumeration<JarEntry> entries = jarFile.entries();
		try {
			while (entries.hasMoreElements()) {
				JarEntry jarEntry = entries.nextElement();
				if (jarEntry.getName().endsWith(".class")) {
					try (InputStream classFileInputStream = jarFile.getInputStream(jarEntry)) {
						ClassReader classReader = new ClassReader(classFileInputStream);
						classReader.accept(new ClassVisitor(Opcodes.ASM9) {
							@Override
							public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
								return new MethodVisitor(Opcodes.ASM9) {

									@Override
									public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
										if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/System") && name.equals("exit")) {
											throw new RuntimeException(MSG);
										}
										super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
									}
								};
							}
						}, 0);
					}
					catch (RuntimeException re) {
						if (MSG.equals(re.getMessage())) return true;
					}
				}
			}
		}
		finally {
			jarFile.close();
		}
		return false;
	}

	public static Map<String, List<Integer>> scan(File file, boolean storeClassName) throws Exception {
		JarFile jarFile = new JarFile(file);
		Enumeration<JarEntry> entries = jarFile.entries();
		final Map<String, List<Integer>> matches = new HashMap<>();
		try {
			while (entries.hasMoreElements()) {
				JarEntry jarEntry = entries.nextElement();
				if (jarEntry.getName().endsWith(".class")) {
					try (InputStream classFileInputStream = jarFile.getInputStream(jarEntry)) {
						ClassReader classReader = new ClassReader(classFileInputStream);
						classReader.accept(new ClassVisitor(Opcodes.ASM9) {

							@Override
							public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
								return new MethodVisitor(Opcodes.ASM9) {
									private int lineNumber;

									@Override
									public void visitLineNumber(int line, Label start) {
										this.lineNumber = line;
										super.visitLineNumber(line, start);
									}

									@Override
									public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
										if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/System") && name.equals("exit")) {
											String cn = storeClassName ? classReader.getClassName() : jarEntry.getName();
											List<Integer> lines = matches.get(cn);
											if (lines == null) {
												lines = new ArrayList<>();
												matches.put(cn, lines);
											}
											lines.add(lineNumber);
										}
										super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
									}
								};
							}
						}, 0);
					}
				}
			}
		}
		finally {
			jarFile.close();
		}
		return matches;
	}

	public static void main(String[] args) throws Exception {
		clean(new File("...."), new File("...."));
	}

	public static void clean(File existingJar, File newJar) throws Exception {
		Map<String, List<Integer>> matches = scan(existingJar, false);
		if (matches.size() == 0) return;
		Set<String> classes = matches.keySet();

		try (JarInputStream jis = new JarInputStream(new FileInputStream(existingJar)); JarOutputStream jos = new JarOutputStream(new FileOutputStream(newJar))) {
			JarEntry entry;
			while ((entry = jis.getNextJarEntry()) != null) {
				String name = entry.getName();
				JarEntry newEntry = new JarEntry(name);
				jos.putNextEntry(newEntry);
				if (name.endsWith(".class")) {
					if (classes.contains(name)) {
						// If the class is in the list, modify it
						ClassReader cr = new ClassReader(jis);
						ClassWriter cw = new ClassWriter(ASMUtil.CLASSWRITER_ARGS);
						ExitReplacerClassVisitor cv = new ExitReplacerClassVisitor(cw, name.substring(0, name.length() - 6));
						cr.accept(cv, 0);

						byte[] modifiedClassBytes = cw.toByteArray();
						jos.write(modifiedClassBytes);
					}
					else {
						// For classes not in the list, simply copy the content
						IOUtil.copy(jis, jos, false, false);
					}
				}
				else {
					// For non-class entries, simply copy the content
					IOUtil.copy(jis, jos, false, false);
				}
				jis.closeEntry();
				jos.closeEntry();
			}
		}
	}

	private static class ExitReplacerClassVisitor extends ClassVisitor {
		RefBoolean add = new RefBooleanImpl(false);
		private String className;

		public ExitReplacerClassVisitor(ClassVisitor cv, String className) {
			super(Opcodes.ASM9, cv);
			this.className = className;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
			MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
			return new ExitReplacerMethodVisitor(mv, className, add);
		}

		private static class ExitReplacerMethodVisitor extends MethodVisitor {
			private RefBoolean add;
			private String className;

			public ExitReplacerMethodVisitor(MethodVisitor mv, String className, RefBoolean add) {
				super(Opcodes.ASM9, mv);
				this.className = className;
				this.add = add;
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
				// Check for System.exit() method call
				if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/System") && name.equals("exit") && descriptor.equals("(I)V")) {
					// Replace System.exit(0) with throwing AWTError

					super.visitTypeInsn(Opcodes.NEW, "java/awt/AWTError");
					super.visitInsn(Opcodes.DUP);
					super.visitLdcInsn("blocked System.exit");
					super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/awt/AWTError", "<init>", "(Ljava/lang/String;)V", false);
					super.visitInsn(Opcodes.ATHROW);

				}
				else if (opcode == Opcodes.INVOKESPECIAL && "java/io/File".equals(owner) && "<init>".equals(name) && "(Ljava/lang/String;)V".equals(descriptor)) {
					add.setValue(true);
					super.visitMethodInsn(Opcodes.INVOKESTATIC, className.replace('.', '/'), "root", "(Ljava/lang/String;)Ljava/lang/String;", false); // Call the static root
																																						// method
					super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
				}

				else {
					// For any other method call, just delegate to parent method visitor
					super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
				}
			}
		}

		@Override
		public void visitEnd() {
			if (add.toBooleanValue()) {
				MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "root", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
				mv.visitCode();

				// if (!".".equals(path)) return path;
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitLdcInsn(".");
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
				Label l1 = new Label();
				mv.visitJumpInsn(Opcodes.IFEQ, l1);

				// path = System.getenv("LUCEE_SYSTEMEXIT_ROOT");
				mv.visitLdcInsn("LUCEE_SYSTEMEXIT_ROOT");
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getenv", "(Ljava/lang/String;)Ljava/lang/String;", false);
				mv.visitVarInsn(Opcodes.ASTORE, 0);

				// if (path != null) return path;
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitJumpInsn(Opcodes.IFNONNULL, l1);

				// path = System.getProperty("lucee.systemexit.root");
				mv.visitLdcInsn("lucee.systemexit.root");
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
				mv.visitVarInsn(Opcodes.ASTORE, 0);

				// if (path != null) return path;
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitJumpInsn(Opcodes.IFNONNULL, l1);

				// return ".";
				mv.visitLdcInsn(".");
				mv.visitInsn(Opcodes.ARETURN);

				mv.visitLabel(l1);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitInsn(Opcodes.ARETURN);

				mv.visitMaxs(1, 2); // Adjusting for max stack size and number of local variables
				mv.visitEnd();
			}
			super.visitEnd();
		}
	}

	public static void validate(Resource[] resources) throws PageException {
		if (resources != null && resources.length > 0 && Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.validate.systemexit", null), false)) {
			for (Resource r: resources) {
				validate(r);
			}
		}
	}

	public static void validate(List<Resource> resources) throws PageException {
		if (resources != null && resources.size() > 0 && Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.validate.systemexit", null), false)) {
			for (Resource r: resources) {
				validate(r);
			}
		}
	}

	public static void validate(Resource res) throws PageException {

		if (validateSystemExit == null) {
			synchronized (token) {
				if (validateSystemExit == null) {
					validateSystemExit = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.validate.systemexit", null), Boolean.FALSE);
				}
			}
		}

		if (validateSystemExit && res != null && res.exists()) {
			try {
				if (has(FileWrapper.toFile(res))) {
					throw new ApplicationException("The JAR file [" + res + "] has been blocked due to a detected 'System.exit' call. "
							+ "This action is restricted when the environment variable or system property 'LUCEE_VALIDATE_SYSTEMEXIT' or 'lucee.validate.systemexit' is enabled.");
				}
			}
			catch (IOException ioe) {
				throw Caster.toPageException(ioe);
			}
		}
	}
}