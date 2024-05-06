package lucee.transformer.dynamic;

import java.io.IOException;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import lucee.aprint;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.SystemOut;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.FunctionMember;
import lucee.transformer.dynamic.meta.LegacyMethod;

public class DynamicInvoker {

	private static DynamicInvoker engine;
	private Map<Integer, DynamicClassLoader> loaders = new HashMap<>();
	private Resource root;
	private Log log;
	private static final Object token = new SerializableObject();

	private static Map<String, AtomicInteger> observer = new ConcurrentHashMap<>();

	public DynamicInvoker(Resource configDir) {
		try {
			this.log = CFMLEngineFactory.getInstance().getThreadConfig().getLog("application");
		}
		catch (Exception e) {

		}
		try {
			this.root = configDir.getRealResource("dynclasses");
			// loader = new DirectClassLoader(configDir.getRealResource("reflection"));
		}
		catch (Exception e) {
			if (log != null) log.error("dynamic", e);
			this.root = SystemUtil.getTempDirectory();
			// loader = new DirectClassLoader(SystemUtil.getTempDirectory());

			// e.printStackTrace();
		}

	}

	public static DynamicInvoker getInstance(Resource configDir) {
		if (engine == null) {
			engine = new DynamicInvoker(configDir);
		}
		return engine;
	}

	public Object invokeStaticMethod(Class<?> clazz, Key methodName, Object[] arguments) throws Exception {
		return invoke(null, clazz, methodName, arguments);
	}

	public Object invokeStaticMethod(Class<?> clazz, String methodName, Object[] arguments) throws Exception {
		return invoke(null, clazz, KeyImpl.init(methodName), arguments);
	}

	public Object invokeInstanceMethod(Object obj, Key methodName, Object[] arguments) throws Exception {
		return invoke(obj, obj.getClass(), methodName, arguments);
	}

	public Object invokeInstanceMethod(Object obj, String methodName, Object[] arguments) throws Exception {
		return invoke(obj, obj.getClass(), KeyImpl.init(methodName), arguments);
	}

	public Object invokeConstructor(Class<?> clazz, Object[] arguments) throws Exception {
		return invoke(null, clazz, null, arguments);
	}

	// TODO handles isStatic better with proper exceptions
	/*
	 * executes a instance method of the given object
	 * 
	 */
	private Object invoke(Object objMaybeNull, Class<?> objClass, Key methodName, Object[] arguments) throws Exception {
		try {
			return ((BiFunction<Object, Object[], Object>) createInstance(objClass, methodName, arguments).getValue()).apply(objMaybeNull, arguments);
		}
		catch (IncompatibleClassChangeError | IllegalStateException e) {
			if (log != null) log.error("dynamic", e);
			if (!Clazz.allowReflection()) throw e;
			lucee.transformer.dynamic.meta.Method method = Clazz.getMethodMatch(getClazz(objClass, true), methodName, arguments, true);
			return ((LegacyMethod) method).getMethod().invoke(objClass, arguments);
		}
	}

	public Clazz getClazz(Class<?> clazz) {
		return Clazz.getClazz(clazz, root, log);
	}

	public Clazz getClazz(Class<?> clazz, boolean useReflection) {
		return Clazz.getClazz(clazz, root, log, useReflection);
	}

	/*
	 * private static double loadClassTotal = 0; private static double getMatchTotal = 0; private static
	 * double hasMatchTotal = 0; private static double create1Total = 0; private static double
	 * create2Total = 0;
	 * 
	 * private static int loadClassCount = 0; private static int getMatchCount = 0; private static int
	 * hasMatchCount = 0; private static int create1Count = 0; private static int create2Count = 0;
	 */
	public Pair<FunctionMember, Object> createInstance(Class<?> clazz, Key methodName, Object[] arguments) throws NoSuchMethodException, IOException, ClassNotFoundException,
			UnmodifiableClassException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, PageException {
		// observe(clazz, methodName);
		// double start = SystemUtil.millis();

		boolean isConstr = methodName == null;
		Clazz clazzz = getClazz(clazz);
		/*
		 * { loadClassCount++; loadClassTotal += (SystemUtil.millis() - start); print.e("loadClass(" +
		 * loadClassCount + "):" + Caster.toString(loadClassTotal / loadClassCount)); start =
		 * SystemUtil.millis(); }
		 */
		lucee.transformer.dynamic.meta.FunctionMember fm = null;
		lucee.transformer.dynamic.meta.Method method = null;
		lucee.transformer.dynamic.meta.Constructor constr = null;
		// <init>
		if (isConstr) {
			fm = constr = Clazz.getConstructorMatch(clazzz, arguments, true);
		}
		else {
			// Clazz clazz, final Collection.Key methodName, final Object[] args, boolean convertArgument
			fm = method = Clazz.getMethodMatch(clazzz, methodName, arguments, true);
		}
		/*
		 * { getMatchCount++; getMatchTotal += (SystemUtil.millis() - start); print.e("get match(" +
		 * getMatchCount + "):" + Caster.toString(getMatchTotal / getMatchCount)); start =
		 * SystemUtil.millis(); }
		 */

		Type[] parameterTypes = fm.getArgumentTypes();
		clazz = fm.getDeclaringClass(); // we wanna go as low as possible, to be as open as possible also this avoid not allow to access

		StringBuilder sbClassPath = new StringBuilder();
		sbClassPath.append(clazz.getName().replace('.', '/')).append('/').append(isConstr ? "____init____" : fm.getName());
		StringBuilder sbArgs = new StringBuilder();
		for (int i = 0; i < parameterTypes.length; i++) {
			sbArgs.append(':').append(parameterTypes[i].getClassName().replace('.', '_'));
		}
		sbClassPath.append('_').append(HashUtil.create64BitHashAsString(sbArgs, Character.MAX_RADIX));
		String classPath = Clazz.getPackagePrefix() + sbClassPath.toString();// StringUtil.replace(sbClassPath.toString(), "javae/lang/", "java_lang/", false);
		String className = classPath.replace('/', '.');

		DynamicClassLoader loader = getCL(clazz);
		if (loader.hasClass(className)) {
			// try {
			return new Pair<FunctionMember, Object>(fm, loader.loadInstance(className));
			/*
			 * } finally { hasMatchCount++; hasMatchTotal += (SystemUtil.millis() - start); print.e("has match("
			 * + hasMatchCount + "):" + Caster.toString(hasMatchTotal / hasMatchCount)); start =
			 * SystemUtil.millis(); }
			 */
		}
		Class[] parameterClasses = fm.getArgumentClasses();

		ClassWriter cw = ASMUtil.getClassWriter();
		MethodVisitor mv;
		String abstractClassPath = "java/lang/Object";
		cw.visit(ASMUtil.getJavaVersionForBytecodeGeneration(), Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, classPath,
				"Ljava/lang/Object;Ljava/util/function/BiFunction<Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;>;", "java/lang/Object",
				new String[] { "java/util/function/BiFunction" });

		// Constructor
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0); // Load "this"
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, abstractClassPath, "<init>", "()V", false); // Call the constructor of super class (Object)
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1); // Compute automatically
		mv.visitEnd();

		// Dynamic invoke method
		// public abstract Object invoke(PageContext pc, Object[] args) throws PageException;
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "apply", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
		mv.visitCode();
		boolean isStatic = true;
		if (isConstr) {
			mv.visitTypeInsn(Opcodes.NEW, Type.getType(clazz).getInternalName());
			mv.visitInsn(Opcodes.DUP); // Duplicate the top operand stack value

		}
		else {
			isStatic = fm.isStatic();
			if (!isStatic) {
				// Load the instance to call the method on
				mv.visitVarInsn(Opcodes.ALOAD, 1); // Load the first method argument (instance)
				if (!fm.getDeclaringProviderClass().equals(Object.class)) { // Only cast if clazz is not java.lang.Object
					mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(fm.getDeclaringProviderClass()));
				}
			}
		}
		// Assuming no arguments are needed for the invoked method, i.e., toString()
		// For methods that require arguments, you would need to manipulate the args array appropriately
		// here

		// print.e(Type.getInternalName(clazz));

		StringBuilder methodDesc = new StringBuilder();
		String del = "(";
		if (fm.getArgumentCount() > 0) {
			// Load method arguments from the args array
			Type[] args = fm.getArgumentTypes();
			// TODO if args!=arguments throw !
			for (int i = 0; i < args.length; i++) {

				methodDesc.append(del).append(args[i].getDescriptor());
				del = "";

				mv.visitVarInsn(Opcodes.ALOAD, 2); // Load the args array
				mv.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;"); // Cast it to Object[]

				mv.visitIntInsn(Opcodes.BIPUSH, i); // Index of the argument in the array
				mv.visitInsn(Opcodes.AALOAD); // Load the argument from the array

				// Cast or unbox the argument as necessary
				// TOOD Caster.castTo(null, clazz, methodDesc)
				Class<?> argType = parameterClasses[i]; // TODO get the class from args
				if (argType.isPrimitive()) {
					Type type = Type.getType(argType);
					Class<?> wrapperType = Reflector.toReferenceClass(argType);
					mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(wrapperType)); // Cast to wrapper type
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(wrapperType), type.getClassName() + "Value", "()" + type.getDescriptor(), false); // Unbox
				}
				else {
					mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(argType)); // Cast to correct type
				}
			}
		}
		else {
			methodDesc.append('(');
		}
		Type rt = isConstr ? Type.getType(clazz) : method.getReturnType();
		methodDesc.append(')').append(isConstr ? Types.VOID : rt.getDescriptor());
		if (isConstr) {
			// Create a new instance of java/lang/String
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, rt.getInternalName(), "<init>", methodDesc.toString(), false); // Call the constructor of String
		}
		else {
			mv.visitMethodInsn(isStatic ? Opcodes.INVOKESTATIC : (fm.getDeclaringProviderClass().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL),
					Type.getInternalName(fm.getDeclaringProviderClass()), method.getName(), methodDesc.toString(), fm.getDeclaringProviderClass().isInterface());

		}

		boxIfPrimitive(mv, rt);
		// method on the
		// instance
		mv.visitInsn(Opcodes.ARETURN); // Return the result of the method call
		if (isConstr) mv.visitMaxs(2, 1);
		else mv.visitMaxs(1, 3); // Compute automatically
		mv.visitEnd();

		cw.visitEnd();
		byte[] barr = cw.toByteArray();

		/*
		 * { create1Count++; create1Total += (SystemUtil.millis() - start); print.e("create 1(" +
		 * create1Count + "):" + Caster.toString(create1Total / create1Count)); start = SystemUtil.millis();
		 * }
		 */
		Object result = loader.loadInstance(className, barr);
		/*
		 * { create2Count++; create2Total += (SystemUtil.millis() - start); print.e("create 2(" +
		 * create2Count + "):" + Caster.toString(create2Total / create2Count)); start = SystemUtil.millis();
		 * }
		 */
		return new Pair<FunctionMember, Object>(fm, result);
	}

	private static void observe(Class<?> clazz, Key methodName) {
		String key = clazz.getName() + ":" + methodName;
		AtomicInteger count = observer.get(key);
		if (count == null) {
			observer.put(key, new AtomicInteger(1));
		}
		else {
			count.incrementAndGet();
		}
	}

	public static Struct observeData() {
		Struct sct = new StructImpl();
		for (Entry<String, AtomicInteger> e: observer.entrySet()) {
			sct.put(e.getKey(), e.getValue().doubleValue());
		}
		return sct;
	}

	private static void boxIfPrimitive(MethodVisitor mv, Type returnType) {
		switch (returnType.getSort()) {
		case Type.BOOLEAN:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
			break;
		case Type.CHAR:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
			break;
		case Type.BYTE:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
			break;
		case Type.SHORT:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
			break;
		case Type.INT:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			break;
		case Type.FLOAT:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
			break;
		case Type.LONG:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
			break;
		case Type.DOUBLE:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
			break;
		case Type.VOID:
			// For void methods, push null onto the stack to comply with the Object return type.
			mv.visitInsn(Opcodes.ACONST_NULL);
			break;
		// No need to handle Type.ARRAY or Type.OBJECT, as they are already Objects
		}
	}

	private DynamicClassLoader getCL(Class<?> clazz) {
		ClassLoader parent = clazz.getClassLoader();
		if (parent == null) parent = getClass().getClassLoader();// core classloader

		DynamicClassLoader cl = loaders.get(parent.hashCode());
		if (cl == null) {
			synchronized (token) {
				cl = loaders.get(parent.getName());
				if (cl == null) {
					// print.e("---- newnewnewnewnewnew ---- " + parent.hashCode() + ":" + parent.toString());
					loaders.put(parent.hashCode(), cl = new DynamicClassLoader(parent, root, log));
				}
			}
		}
		return cl;
	}

	/**
	 * Gets the argument types for a given constructor.
	 *
	 * @param constructor The constructor for which to get argument types.
	 * @return An array of Type objects representing the argument types of the constructor.
	 */
	public static Type[] getArgumentTypes(Constructor<?> constructor) {
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		StringBuilder descriptor = new StringBuilder("(");
		for (Class<?> paramType: parameterTypes) {
			descriptor.append(Type.getDescriptor(paramType));
		}
		descriptor.append(")V"); // Constructors always return void, denoted as 'V'

		return Type.getArgumentTypes(descriptor.toString());
	}

	public static void main(String[] args) throws Exception {
		Resource classes = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/tmp8/classes/");
		ResourceUtil.deleteContent(classes, null);
		DynamicInvoker e = new DynamicInvoker(classes);
		StringBuilder sb = new StringBuilder("Susi");
		Test t = new Test();
		Integer i = Integer.valueOf(3);
		BigDecimal bd = BigDecimal.TEN;

		TimeZone tz = java.util.TimeZone.getDefault();
		ArrayList arr = new ArrayList<>();

		Object sadas1 = e.invokeInstanceMethod(sb, "append", new Object[] { "sss" });
		aprint.e(sadas1);

		// java.util.HashMap.EntrySet
		Thread.getAllStackTraces().entrySet().iterator();
		Object sadasd = e.invokeInstanceMethod(Thread.getAllStackTraces().entrySet(), "iterator", new Object[] {});
		// System.exit(0);
		String str = new String("Susi exclusive");
		aprint.e(str);
		aprint.e(e.invokeConstructor(String.class, new Object[] { "Susi exclusive" }));

		// System.exit(0);

		Object eee = e.invokeInstanceMethod(t, "setSource", new Object[] { "" });
		// System.exit(0);

		// source
		// instance ():String
		{
			Object reflection = tz.getID();
			Object dynamic = e.invokeInstanceMethod(tz, "getID", new Object[] {});
			if (!reflection.equals(dynamic)) {
				aprint.e("direct:");
				aprint.e(reflection);
				aprint.e("dynamic:");
				aprint.e(dynamic);
			}
		}

		// instance (double->int):String
		{
			Object reflection = t.test(134);
			Object dynamic = e.invokeInstanceMethod(t, "test", new Object[] { 134D });
			if (!reflection.equals(dynamic)) {
				aprint.e("direct:");
				aprint.e(reflection);
				aprint.e("dynamic:");
				aprint.e(dynamic);
			}
		}

		// instance (double->int):String
		{
			Object reflection = t.test(134);
			Object dynamic = e.invokeInstanceMethod(t, "test", new Object[] { 134D });
			if (!reflection.equals(dynamic)) {
				aprint.e("direct:");
				aprint.e(reflection);
				aprint.e("dynamic:");
				aprint.e(dynamic);
			}
		}

		aprint.e(t.complete("", 1, null));
		aprint.e(e.invokeInstanceMethod(t, "complete", new Object[] { "", i, null }));
		aprint.e(e.invokeInstanceMethod(t, "complete", new Object[] { "", bd, null }));

		aprint.e(t.testb(true, true));
		aprint.e(e.invokeInstanceMethod(t, "testb", new Object[] { null, true }));

		aprint.e(t.testStr(1, "string", 1L));
		aprint.e(e.invokeInstanceMethod(t, "testStr", new Object[] { "1", 1, Double.valueOf(1D) }));

		aprint.e(e.invokeInstanceMethod(t, "test", new Object[] { "1" }));
		aprint.e(e.invokeInstanceMethod(t, "test", new Object[] { 1D }));

		aprint.e(e.invokeInstanceMethod(new SystemOut(), "setOut", new Object[] { null }));
		System.setProperty("a.b.c", "- value -");
		aprint.e(e.invokeInstanceMethod(sb, "toSTring", new Object[] {}));
		aprint.e(e.invokeStaticMethod(SystemUtil.class, "getSystemPropOrEnvVar", new Object[] { "a.b.c", "default-value" }));
		aprint.e(e.invokeStaticMethod(ListUtil.class, "arrayToList", new Object[] { new String[] { "a", "b" }, "," }));

	}

	public static class Test {
		public final int complete(String var1, int var2, List var3) {
			return 5;
		}

		public final void setSource(Object o) {

		}

		public final String testb(Boolean b1, boolean b2) {
			return b1 + ":" + b2;
		}

		public final String testStr(int i, String str, long l) {
			return i + ":" + str;
		}

		public final String test(String str) {
			return "string:" + str;
		}

		public final String test(int i) {
			return "int:" + i;
		}

	}

}
