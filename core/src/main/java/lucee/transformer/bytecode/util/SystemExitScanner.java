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
						classReader.accept(new ClassVisitor(Opcodes.ASM4) {
							@Override
							public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
								return new MethodVisitor(Opcodes.ASM4) {

									@Override
									public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
										if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/System") && name.equals("exit")) {
											throw new RuntimeException(MSG);
										}
										super.visitMethodInsn(opcode, owner, name, descriptor);
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
						classReader.accept(new ClassVisitor(Opcodes.ASM4) {

							@Override
							public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
								return new MethodVisitor(Opcodes.ASM4) {
									private int lineNumber;

									@Override
									public void visitLineNumber(int line, Label start) {
										this.lineNumber = line;
										super.visitLineNumber(line, start);
									}

									@Override
									public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
										if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/System") && name.equals("exit")) {
											String cn = storeClassName ? classReader.getClassName() : jarEntry.getName();
											List<Integer> lines = matches.get(cn);
											if (lines == null) {
												lines = new ArrayList<>();
												matches.put(cn, lines);
											}
											lines.add(lineNumber);
										}
										super.visitMethodInsn(opcode, owner, name, descriptor);
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
						ClassWriter cw = new ClassWriter(ClassReader.EXPAND_FRAMES);
						ExitReplacerClassVisitor cv = new ExitReplacerClassVisitor(cw);
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
		public ExitReplacerClassVisitor(ClassVisitor cv) {
			super(Opcodes.ASM4, cv);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
			MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
			return new ExitReplacerMethodVisitor(mv);
		}

		private static class ExitReplacerMethodVisitor extends MethodVisitor {
			public ExitReplacerMethodVisitor(MethodVisitor mv) {
				super(Opcodes.ASM4, mv);
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
				// Check for System.exit() method call
				if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/System") && name.equals("exit") && descriptor.equals("(I)V")) {
					// Replace System.exit(0) with throwing RuntimeException
					visitInsn(Opcodes.ICONST_1); // true for the if condition
					visitJumpInsn(Opcodes.IFEQ, new Label()); // Jump to the next instruction if false (not executed)
					visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
					visitInsn(Opcodes.DUP);
					visitLdcInsn("blocked System.exit");
					visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
					visitInsn(Opcodes.ATHROW);
				}
				else {
					// For any other method call, just delegate to parent method visitor
					super.visitMethodInsn(opcode, owner, name, descriptor);
				}
			}
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