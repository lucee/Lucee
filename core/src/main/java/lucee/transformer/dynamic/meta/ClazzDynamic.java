package lucee.transformer.dynamic.meta;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import lucee.print;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.runtime.converter.JavaConverter.ObjectInputStreamImpl;

public class ClazzDynamic extends Clazz {

	private static final long serialVersionUID = 862370302422701585L;
	private transient Class clazz;
	private static Map<String, SoftReference<ClazzDynamic>> classes = new ConcurrentHashMap<>();
	private Map<String, FunctionMember> members;

	protected static ClazzDynamic getInstance(Class clazz, Resource dir, Log log) throws IOException {

		ClazzDynamic cd = null;
		SoftReference<ClazzDynamic> sr = classes.get(clazz.getName());
		if (sr == null || (cd = sr.get()) == null) {
			synchronized (clazz) {
				sr = classes.get(clazz.getName());
				if (sr == null || (cd = sr.get()) == null) {

					StringBuilder sbClassPath = new StringBuilder();
					sbClassPath.append(clazz.getName().replace('.', '/')).append(".ser");
					Resource ser = dir.getRealResource("lucee/invoc/wrap/" + sbClassPath.toString());
					if (ser.isFile()) {
						try {
							cd = (ClazzDynamic) deserialize(getClassLoader(clazz), ser.getInputStream());
							cd.clazz = clazz;
							if (log != null) log.info("dynamic", "loaded metadata for [" + clazz.getName() + "] from serialized file:" + ser);
							classes.put(clazz.getName(), new SoftReference<ClazzDynamic>(cd));
						}
						catch (Exception e) {
							print.e(e);
							if (log != null) log.error("dynamic", e);
						}
					}
					if (cd == null) {
						if (log != null) log.info("dynamic", "read metadata from [" + clazz.getName() + "]");
						cd = new ClazzDynamic(clazz, log);
						ser.getParentResource().mkdirs();
						serialize(cd, ser.getOutputStream());
						if (log != null) log.info("dynamic", "stored metadata for [" + clazz.getName() + "] to serialized file:" + ser);
						classes.put(clazz.getName(), new SoftReference<ClazzDynamic>(cd));

					}
				}
			}
		}
		return cd;
	}

	private ClazzDynamic(Class clazz, Log log) throws IOException {
		this.clazz = clazz;
		this.members = new LinkedHashMap<>();
		_getFunctionMembers(clazz, members, log);
	}

	@Override
	public Class getDeclaringClass() {
		return this.clazz;
	}

	@Override
	public Method getDeclaredMethod(String methodName, Class[] arguments) throws IOException, NoSuchMethodException {
		Type[] types = toTypes(arguments);
		outer: for (FunctionMember fm: members.values()) {
			if (fm instanceof Method && clazz.getName().equals(fm.getDeclaringClassName()) && methodName.equals(fm.getName())) {
				Type[] args = ((FunctionMemberDynamic) fm).getArgumentTypes();
				if (types.length == args.length) {
					for (int i = 0; i < args.length; i++) {
						if (!types[i].equals(args[i])) continue outer;
					}
					return (Method) fm;
				}
			}
		}
		throw new NoSuchMethodException("no matching method for " + methodName + "(" + Clazz.toTypeNames(arguments) + ") found");
	}

	@Override
	public Method getMethod(String methodName, Class[] arguments) throws IOException, NoSuchMethodException {
		Type[] types = toTypes(arguments);
		outer: for (FunctionMember fm: members.values()) {
			if (fm.isPublic() && fm instanceof Method && methodName.equals(fm.getName())) {
				Type[] args = ((FunctionMemberDynamic) fm).getArgumentTypes();
				if (types.length == types.length) {
					for (int i = 0; i < args.length; i++) {
						if (!types[i].equals(args[i])) continue outer;
					}
					return (Method) fm;
				}
			}
		}
		throw new NoSuchMethodException("no matching method for " + methodName + "(" + Clazz.toTypeNames(arguments) + ") found");
	}

	@Override
	public Constructor getConstructor(Class[] arguments) throws IOException, NoSuchMethodException {
		Type[] types = toTypes(arguments);
		outer: for (FunctionMember fm: members.values()) {
			if (fm.isPublic() && fm instanceof Constructor && clazz.getName().equals(fm.getDeclaringClassName())) {
				Type[] args = ((FunctionMemberDynamic) fm).getArgumentTypes();
				if (types.length == args.length) {
					for (int i = 0; i < args.length; i++) {
						if (!types[i].equals(args[i])) continue outer;
					}
					return (Constructor) fm;
				}
			}
		}
		throw new NoSuchMethodException("no matching constructor for (" + Clazz.toTypeNames(arguments) + ") found");
	}

	@Override
	public Constructor getDeclaredConstructor(Class[] arguments) throws IOException, NoSuchMethodException {
		Type[] types = toTypes(arguments);
		outer: for (FunctionMember fm: members.values()) {
			if (fm instanceof Constructor && clazz.getName().equals(fm.getDeclaringClassName())) {
				Type[] args = ((FunctionMemberDynamic) fm).getArgumentTypes();
				if (types.length == args.length) {
					for (int i = 0; i < args.length; i++) {
						if (!types[i].equals(args[i])) continue outer;
					}
					return (Constructor) fm;
				}
			}
		}
		throw new NoSuchMethodException("no matching constructor for (" + Clazz.toTypeNames(arguments) + ") found");
	}

	@Override
	public List<Method> getMethods(String methodName, boolean nameCaseSensitive, int argumentLength) throws IOException {
		List<Method> list = new ArrayList<>();
		for (FunctionMember fm: members.values()) {
			// print.e(fm.getName() + ":" + fm.isPublic());
			if (fm.isPublic() && fm instanceof Method && (argumentLength < 0 || argumentLength == fm.getArguments().length)
					&& (methodName == null || (nameCaseSensitive ? methodName.equals(fm.getName()) : methodName.equalsIgnoreCase(fm.getName())))) {
				list.add((Method) fm);
			}
		}
		return list;
	}

	@Override
	public List<Method> getDeclaredMethods(String methodName, boolean nameCaseSensitive, int argumentLength) throws IOException {
		List<Method> list = new ArrayList<>();
		for (FunctionMember fm: members.values()) {
			if (fm instanceof Method && clazz.getName().equals(fm.getDeclaringClassName()) && (argumentLength < 0 || argumentLength == fm.getArguments().length)
					&& (methodName == null || (nameCaseSensitive ? methodName.equals(fm.getName()) : methodName.equalsIgnoreCase(fm.getName()))))
				list.add((Method) fm);
		}
		return list;
	}

	@Override
	public List<Constructor> getConstructors(int argumentLength) throws IOException {
		List<Constructor> list = new ArrayList<>();

		for (FunctionMember fm: members.values()) {
			if (fm.isPublic() && fm instanceof Constructor && clazz.getName().equals(fm.getDeclaringClassName())
					&& (argumentLength < 0 || argumentLength == fm.getArguments().length))
				list.add((Constructor) fm);
		}
		return list;
	}

	@Override
	public List<Constructor> getDeclaredConstructors(int argumentLength) throws IOException {
		List<Constructor> list = new ArrayList<>();
		for (FunctionMember fm: members.values()) {
			if (fm instanceof Constructor && clazz.getName().equals(fm.getDeclaringClassName()) && (argumentLength < 0 || argumentLength == fm.getArguments().length))
				list.add((Constructor) fm);
		}
		return list;
	}

	private static void _getFunctionMembers(final Class clazz, Map<String, FunctionMember> members, Log log) throws IOException {

		final String classPath = clazz.getName().replace('.', '/') + ".class";
		final ClassLoader cl = getClassLoader(clazz);
		ClassReader classReader = new ClassReader(cl.getResourceAsStream(classPath));

		// Create a ClassVisitor to visit the methods
		ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {

			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				if (interfaces != null && interfaces.length > 0) {
					for (String interf: interfaces) {
						try {
							_getFunctionMembers(cl.loadClass(Type.getObjectType(interf).getClassName()), members, log);
						}
						catch (Exception e) {
							print.e(e);
							if (log != null) log.error("dynamic", e);
						}
					}
				}
				if (superName != null) {
					try {
						_getFunctionMembers(cl.loadClass(Type.getObjectType(superName).getClassName()), members, log);
					}
					catch (Exception e) {
						print.e(e);
						if (log != null) log.error("dynamic", e);
					}
				}

				super.visit(version, access, name, signature, superName, interfaces);
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
				FunctionMember fm = FunctionMemberDynamic.createInstance(clazz, name, access, descriptor, exceptions);

				// if ((!onlyPublic || fm.isPublic()) && ((includeConstructor && fm instanceof Constructor) ||
				// (includeMethods && fm instanceof Method))) {
				members.put(Clazz.id(fm), fm);
				// }
				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		};
		// Start visiting the class
		classReader.accept(visitor, 0);
	}

	public static void serialize(Serializable o, OutputStream os) throws IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(os);
			oos.writeObject(o);
		}
		finally {
			IOUtil.close(oos, os);
		}
	}

	public static Object deserialize(ClassLoader cl, InputStream is) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = null;
		Object o = null;
		try {
			ois = new ObjectInputStreamImpl(cl, is);
			o = ois.readObject();
		}
		finally {
			IOUtil.close(ois);
		}
		return o;
	}

}
