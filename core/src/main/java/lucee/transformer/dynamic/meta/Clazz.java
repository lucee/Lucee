package lucee.transformer.dynamic.meta;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.Type;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.java.JavaObject;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.dynamic.DynamicClassLoader;
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.dynamic.ClazzDynamic;
import lucee.transformer.dynamic.meta.reflection.ClazzReflection;

public abstract class Clazz implements Serializable {
	public static final int VERSION = 4;
	public static final boolean DEBUG;

	private static final long serialVersionUID = 4236939474343760825L;
	private static Boolean allowReflection = null;
	static {
		DEBUG = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.NoSuchMethodException.list", null), false);
	}
	private DynamicClassLoader dcl;
	protected final Class clazz;
	protected final Log log;

	public abstract List<Method> getMethods(String methodName, boolean nameCaseSensitive, int argumentLength) throws IOException;

	public abstract List<Method> getDeclaredMethods(String methodName, boolean nameCaseSensitive, int argumentLength) throws IOException;

	public abstract List<Constructor> getConstructors(int argumentLength) throws IOException;

	public abstract List<Constructor> getDeclaredConstructors(int argumentLength) throws IOException;

	public abstract Method getDeclaredMethod(String methodName, Class[] arguments, boolean nameCaseSensitive) throws IOException, NoSuchMethodException;

	public abstract Method[] getMethods();

	public abstract Method getMethod(String methodName, Class[] arguments, boolean nameCaseSensitive) throws IOException, NoSuchMethodException;

	public abstract Method getMethod(String methodName, Class[] arguments, boolean nameCaseSensitive, Method defaultValue);

	public abstract Constructor[] getConstructors();

	public abstract Constructor getConstructor(Class[] arguments) throws IOException, NoSuchMethodException;

	public abstract Constructor getDeclaredConstructor(Class[] arguments) throws IOException, NoSuchMethodException;

	public abstract Class getDeclaringClass();

	public abstract Type getDeclaringType();

	public abstract String id();

	public Clazz(Class clazz, Log log) {
		this.clazz = clazz;
		this.log = log;
	}

	public final Constructor getConstructor(Object[] args, boolean convertArgument, boolean convertComparsion, Constructor defaultValue) {
		return getConstructor(clazz, getConstructors(), args, convertArgument, convertComparsion, defaultValue);
	}

	public final Constructor getConstructor(Object[] args, boolean convertArgument, boolean convertComparsion) throws NoSuchMethodException {
		Constructor constructor = getConstructor(clazz, getConstructors(), args, convertArgument, convertComparsion, null);
		if (constructor != null) return constructor;

		throw new NoSuchMethodException("No matching Constructor for " + clazz.getName() + "(" + Reflector.getDspMethods(Reflector.getClasses(args)) + ") found.");
	}

	public final Method getMethod(String methodName, Object[] args, boolean nameCaseSensitive, boolean convertArgument, boolean convertComparsion, Method defaultValue) {
		return getMethod(clazz, getMethods(), cachedMethods, methodName, args, nameCaseSensitive, convertArgument, convertComparsion, defaultValue);
	}

	public final Method getMethod(String methodName, Object[] args, boolean nameCaseSensitive, boolean convertArgument, boolean convertComparsion) throws NoSuchMethodException {
		Method method = getMethod(clazz, getMethods(), cachedMethods, methodName, args, nameCaseSensitive, convertArgument, convertComparsion, null);
		if (method != null) return method;
		if (DEBUG) {
			try {
				StringBuilder sb = new StringBuilder();
				for (Method m: getMethods(null, true, -1)) {
					sb.append(m.toString()).append(";");
				}
				throw new NoSuchMethodException("No matching method for " + clazz.getName() + "." + methodName + "(" + Reflector.getDspMethods(Reflector.getClasses(args))
						+ ") found. Available methods are [" + sb + "]");
			}
			catch (IOException ioe) {

			}
		}
		throw new NoSuchMethodException("No matching method for " + clazz.getName() + "." + methodName + "(" + Reflector.getDspMethods(Reflector.getClasses(args)) + ") found.");
	}

	public static boolean allowReflection() {
		if (allowReflection == null) {
			allowReflection = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.allow.reflection", null), true);
		}
		return allowReflection;
	}

	public static Clazz getClazzReflection(Class clazz) {
		return new ClazzReflection(clazz, null);
	}

	public static Clazz getClazz(Class clazz, Resource root, Log log, boolean useReflection) {
		if (useReflection) return new ClazzReflection(clazz, log);
		return getClazz(clazz, root, log);
	}

	public static Clazz getClazz(Class clazz, Resource root, Log log) {
		try {
			return ClazzDynamic.getInstance(clazz, root, log);
		}
		catch (Exception e) {
			if (allowReflection()) return new ClazzReflection(clazz, log);
			else throw new RuntimeException(e);
		}
	}

	private static RefInteger nirvana = new RefIntegerImpl();

	/*
	 * private static double cleanArgs = 0; private static double checkAccessibility = 0; private static
	 * double lmethods = 0; private static double cache = 0; private static double exact = 0; private
	 * static double like = 0; private static double convert = 0; private static double lclasses = 0;
	 * private static double lclasses2 = 0; private static int count = 0;
	 */

	private static Map<String, SoftReference<Pair<Method, Boolean>>> cachedMethods = new ConcurrentHashMap<>();

	public static String id(FunctionMember fm) {
		// public java.lang.String java.lang.String.toString()
		StringBuilder sb = new StringBuilder();

		// name
		sb.append(fm.getName())

				.append('(');

		// arguments;
		String[] args = fm.getArguments();
		if (args != null && args.length > 0) {
			for (String cn: args) {
				sb.append(cn).append(';');
			}
		}

		sb.append(")");

		return sb.toString();
	}

	public static String getAccessModifierAsString(FunctionMember fm) {
		if (fm.isPublic()) return "public";
		if (fm.isProtected()) return "protected";
		if (fm.isPrivate()) return "private";

		return "";
	}

	public static ClassLoader getClassLoader(Class clazz) {
		ClassLoader tmp = clazz.getClassLoader();
		if (tmp == null) tmp = ClassLoader.getSystemClassLoader();
		return tmp;
	}

	public DynamicClassLoader getDynamicClassLoader(DynamicInvoker dynamicInvoker) {
		if (dcl == null) {
			dcl = dynamicInvoker.getCL(getDeclaringClass());
		}
		return dcl;
	}

	public static Type[] toTypes(Class[] classes) {
		Type[] types = new Type[classes.length];
		for (int i = 0; i < classes.length; i++) {
			types[i] = Type.getType(classes[i]);
		}
		return types;
	}

	public static String toTypeNames(Class[] classes) {
		StringBuilder sb = new StringBuilder();
		String del = "";
		for (int i = 0; i < classes.length; i++) {
			sb.append(del).append(Caster.toTypeName(classes[i]));
			del = ",";
		}
		return sb.toString();
	}

	public static boolean isPrimitive(Type type) {
		int sort = type.getSort();
		return sort >= Type.BOOLEAN && sort <= Type.DOUBLE;
	}

	/**
	 * Compares two FunctionMember objects based on their access modifiers. The comparison is done in
	 * the order of access levels from most restrictive to least restrictive: private (1), default (2),
	 * protected (3), and public (4).
	 *
	 * @param left the first FunctionMember to compare
	 * @param right the second FunctionMember to compare
	 * @return a negative integer, zero, or a positive integer if the access level of the left
	 *         FunctionMember is less than, equal to, or greater than the access level of the right
	 *         FunctionMember.
	 */
	public static int compareAccess(FunctionMember left, FunctionMember right) {
		int l = 1, r = 1;

		if (left.isPublic()) l = 4;
		else if (left.isProtected()) l = 3;
		else if (left.isDefault()) l = 2;

		if (right.isPublic()) r = 4;
		else if (right.isProtected()) r = 3;
		else if (right.isDefault()) r = 2;

		return l - r;

	}

	public static int getAccessModifier(FunctionMember fm) {
		if (fm.isPublic()) return 4;
		if (fm.isProtected()) return 3;
		if (fm.isPrivate()) return 1;
		return 2;
	}

	public static int getAccessModifier(int reflectionAccess) {
		if (Modifier.isPublic(reflectionAccess)) return 4;
		if (Modifier.isProtected(reflectionAccess)) return 3;
		if (Modifier.isPrivate(reflectionAccess)) return 1;
		return 2;
	}

	public static Class toClass(ClassLoader cl, Type type) throws ClassException {
		return ClassUtil.loadClass(cl, ASMUtil.getClassName(type));
	}

	public static String getPackagePrefix() {
		return "lucee/invoc/wrap/v" + VERSION + "/";
	}

	public static Constructor getConstructor(Class clazz, Constructor[] constructors, Object[] args, boolean convertArgument, boolean convertComparsion, Constructor defaultValue) {
		// like
		Class[] parameterTypes;
		outer: for (Constructor fm: constructors) {
			if ((args.length == fm.getArgumentCount()) && clazz.getName().equals(fm.getDeclaringClassName())) {
				parameterTypes = fm.getArgumentClasses();
				for (int y = 0; y < parameterTypes.length; y++) {
					Class argClass = args[y] == null ? Object.class : args[y].getClass();
					Class paramClass = Reflector.toReferenceClass(parameterTypes[y]);
					if (!paramClass.isAssignableFrom(argClass) && !Reflector.isInstaneOf(argClass, paramClass, false)) continue outer;
				}
				return fm;
			}
		}

		// in case there are no arguments the code below will not find any match, nothing to convert
		if (args.length == 0) return defaultValue;

		// convert comparsion
		Pair<Constructor, Object[]> result = null;
		int _rating = 0;
		if (convertComparsion) {
			outer: for (Constructor fm: constructors) {
				if ((args.length == fm.getArgumentCount()) && clazz.getName().equals(fm.getDeclaringClassName())) {
					RefInteger rating = (constructors.length > 1) ? new RefIntegerImpl(0) : null;
					parameterTypes = fm.getArgumentClasses();
					Object[] newArgs = new Object[args.length];
					for (int y = 0; y < parameterTypes.length; y++) {
						try {
							newArgs[y] = Reflector.convert(args[y], Reflector.toReferenceClass(parameterTypes[y]), rating);
						}
						catch (PageException e) {
							continue outer;
						}
					}
					if (result == null || rating.toInt() > _rating) {
						if (rating != null) _rating = rating.toInt();
						result = new Pair<Constructor, Object[]>(fm, newArgs);
					}
					// return new ConstructorInstance(constructors[i],newArgs);
				}
			}
		}

		if (result != null) {
			if (convertArgument) {
				Object[] newArgs = result.getValue();
				for (int x = 0; x < args.length; x++) {
					args[x] = newArgs[x];
				}
			}
			return result.getName();
		}
		return defaultValue;
	}

	public static Method getMethod(Class clazz, Method[] methods, Map<String, SoftReference<Pair<Method, Boolean>>> cachedMethods, String methodName, Object[] args,
			boolean nameCaseSensitive, boolean convertArgument, boolean convertComparsion, Method defaultValue) {

		// like
		Class[] parameterTypes;
		outer: for (Method fm: methods) {
			if ((args.length == fm.getArgumentCount()) && (nameCaseSensitive ? methodName.equals(fm.getName()) : methodName.equalsIgnoreCase(fm.getName()))) {
				parameterTypes = fm.getArgumentClasses();
				for (int y = 0; y < parameterTypes.length; y++) {
					if (args[y] == null) {
						if (parameterTypes[y].isPrimitive()) continue outer;
					}
					else {
						Class argClass = args[y].getClass();
						Class paramClass = Reflector.toReferenceClass(parameterTypes[y]);
						if (!paramClass.isAssignableFrom(argClass) && !Reflector.isInstaneOf(argClass, paramClass, false)) continue outer;
					}
				}
				return fm;
			}
		}

		// in case there are no arguments the code below will not find any match, nothing to convert
		if (args.length == 0) return defaultValue;

		// cache
		StringBuilder sb = new StringBuilder(100).append(clazz.getName()).append(';').append(methodName).append(';'); // append(id()).
		for (Object arg: args) {
			if (arg == null) {
				sb.append("java.lang.Object").append(';');
			}
			else if (arg instanceof JavaObject) {
				sb.append(((JavaObject) arg).getClazz().getName()).append(';');
			}
			else {
				sb.append(arg.getClass().getName()).append(';');
			}
		}

		String key = sb.toString();

		// get match from cache
		if (cachedMethods != null) {
			SoftReference<Pair<Method, Boolean>> sr = cachedMethods.get(key);
			if (sr != null) {
				Pair<Method, Boolean> p = sr.get();
				if (p != null) {
					try {
						if (p.getValue()) {
							Class[] trgArgs = p.getName().getArgumentClasses();
							for (int x = 0; x < trgArgs.length; x++) {
								if (args[x] != null) {
									args[x] = Reflector.convert(args[x], Reflector.toReferenceClass(trgArgs[x]), nirvana);
								}
							}
						}
						return p.getName();
					}
					catch (PageException pe) {}
				}
			}
		}

		// convert comparsion
		Pair<Method, Object[]> result = null;
		int _rating = 0;
		if (convertComparsion) {
			outer: for (Method fm: methods) {
				if ((args.length == fm.getArgumentCount()) && ((nameCaseSensitive ? methodName.equals(fm.getName()) : methodName.equalsIgnoreCase(fm.getName())))) {
					RefInteger rating = (methods.length > 1) ? new RefIntegerImpl(0) : null;
					parameterTypes = fm.getArgumentClasses();
					Object[] newArgs = new Object[args.length];

					for (int y = 0; y < parameterTypes.length; y++) {
						try {
							newArgs[y] = Reflector.convert(args[y], Reflector.toReferenceClass(parameterTypes[y]), rating);
						}
						catch (PageException e) {
							continue outer;
						}
					}
					if (result == null || rating.toInt() > _rating) {
						if (rating != null) _rating = rating.toInt();
						result = new Pair<Method, Object[]>(fm, newArgs);
					}
				}
			}
		}

		if (result != null) {
			if (convertArgument) {
				Object[] newArgs = result.getValue();
				for (int x = 0; x < args.length; x++) {
					args[x] = newArgs[x];
				}
			}
			// we only cache classes using fix classloaders
			ClassLoader cl = clazz.getClassLoader();
			if (cl == null || cl == SystemUtil.getCoreClassLoader() || cl == SystemUtil.getLoaderClassLoader()) {
				if (cachedMethods == null) cachedMethods = new ConcurrentHashMap<>();
				cachedMethods.put(key, new SoftReference<Pair<Method, Boolean>>(new Pair<Method, Boolean>(result.getName(), Boolean.TRUE)));
			}
			return result.getName();
		}
		return defaultValue;
	}
}