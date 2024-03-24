package lucee.transformer.dynamic;

import java.io.IOException;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
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

import lucee.print;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.SystemOut;
import lucee.runtime.exp.PageException;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;

public class DynamicInvoker {

	private static DynamicInvoker engine;
	private Map<Integer, DynamicClassLoader> loaders = new HashMap<>();
	private Resource root;
	private static final Object token = new SerializableObject();

	private static Map<String, AtomicInteger> observer = new ConcurrentHashMap<>();

	public DynamicInvoker(Resource configDir) {
		try {
			print.e(configDir);
			this.root = configDir.getRealResource("reflection");
			// loader = new DirectClassLoader(configDir.getRealResource("reflection"));
		}
		catch (Exception e) {
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
			print.e(e);
			LogUtil.log("direct", e);
			Method method = Reflector.getMethod(objClass, methodName, arguments, true);
			return method.invoke(null, arguments);
		}
	}

	public Pair<Executable, Object> createInstance(Class<?> clazz, Key methodName, Object[] arguments) throws NoSuchMethodException, IOException, ClassNotFoundException,
			UnmodifiableClassException, PageException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
		observe(clazz, methodName);

		arguments = Reflector.cleanArgs(arguments);
		boolean isConstr = methodName == null;
		Method method = null;
		Constructor constr = null;
		// <init>
		if (isConstr) {
			constr = Reflector.getConstructor(clazz, arguments, true);
		}
		else {
			method = Reflector.getMethod(clazz, methodName, arguments, true);
		}

		Parameter[] parameters = isConstr ? constr.getParameters() : method.getParameters();
		clazz = isConstr ? constr.getDeclaringClass() : method.getDeclaringClass(); // we wanna go as low as possible, to be as open as possible also this avoid not allow to access

		StringBuilder sbClassPath = new StringBuilder();
		sbClassPath.append(clazz.getName().replace('.', '/')).append('/').append(isConstr ? "____init____" : method.getName());
		StringBuilder sbArgs = new StringBuilder();
		for (int i = 0; i < parameters.length; i++) {
			sbArgs.append(':').append(parameters[i].getType().getName().replace('.', '_'));
		}
		sbClassPath.append('_').append(HashUtil.create64BitHashAsString(sbArgs, Character.MAX_RADIX));
		String classPath = "lucee/invoc/wrap/" + sbClassPath.toString();// StringUtil.replace(sbClassPath.toString(), "javae/lang/", "java_lang/", false);
		String className = classPath.replace('/', '.');
		// print.e("classPath: " + classPath);
		// print.e("className: " + className);
		DynamicClassLoader loader = getCL(clazz);
		if (loader.hasClass(className)) {
			// print.e("existing!!!" + className);
			return new Pair<Executable, Object>(isConstr ? constr : method, loader.loadInstance(className));
		}

		ClassWriter cw = ASMUtil.getClassWriter();
		MethodVisitor mv;
		print.e(classPath);
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
			isStatic = Modifier.isStatic(method.getModifiers());
			if (!isStatic) {
				// Load the instance to call the method on
				mv.visitVarInsn(Opcodes.ALOAD, 1); // Load the first method argument (instance)
				if (!clazz.equals(Object.class)) { // Only cast if clazz is not java.lang.Object
					mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(clazz));
				}
			}
		}
		// Assuming no arguments are needed for the invoked method, i.e., toString()
		// For methods that require arguments, you would need to manipulate the args array appropriately
		// here

		// print.e(Type.getInternalName(clazz));

		StringBuilder methodDesc = new StringBuilder();
		String del = "(";
		if ((isConstr ? constr : method).getParameterCount() > 0) {
			// Load method arguments from the args array
			Type[] args = isConstr ? getArgumentTypes(constr) : Type.getArgumentTypes(method);
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
				Class<?> argType = parameters[i].getType(); // TODO get the class from args
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
		Type rt = isConstr ? Type.getType(clazz) : Type.getReturnType(method);
		methodDesc.append(')').append(isConstr ? Types.VOID : rt.getDescriptor());
		print.e(methodDesc);
		if (isConstr) {
			// Create a new instance of java/lang/String
			print.e("new " + rt.getInternalName());
			print.e(methodDesc);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, rt.getInternalName(), "<init>", methodDesc.toString(), false); // Call the constructor of String
		}
		else {
			print.e(methodDesc);
			mv.visitMethodInsn(isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL, Type.getInternalName(clazz), method.getName(), methodDesc.toString(), false);

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
		Object result = loader.loadInstance(className, barr);

		return new Pair<Executable, Object>(isConstr ? constr : method, result);
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
					print.e("---- newnewnewnewnewnew ---- " + parent.toString());
					loaders.put(parent.hashCode(), cl = new DynamicClassLoader(parent, root));
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

		String str = new String("Susi exclusive");
		print.e(str);
		print.e(e.invokeConstructor(String.class, new Object[] { "Susi exclusive" }));
		System.exit(0);

		// instance ():String
		{
			Object reflection = tz.getID();
			Object dynamic = e.invokeInstanceMethod(tz, "getID", new Object[] {});
			if (!reflection.equals(dynamic)) {
				print.e("direct:");
				print.e(reflection);
				print.e("dynamic:");
				print.e(dynamic);
			}
		}

		// instance (double->int):String
		{
			Object reflection = t.test(134);
			Object dynamic = e.invokeInstanceMethod(t, "test", new Object[] { 134D });
			if (!reflection.equals(dynamic)) {
				print.e("direct:");
				print.e(reflection);
				print.e("dynamic:");
				print.e(dynamic);
			}
		}

		// instance (double->int):String
		{
			Object reflection = t.test(134);
			Object dynamic = e.invokeInstanceMethod(t, "test", new Object[] { 134D });
			if (!reflection.equals(dynamic)) {
				print.e("direct:");
				print.e(reflection);
				print.e("dynamic:");
				print.e(dynamic);
			}
		}

		print.e(t.complete("", 1, null));
		print.e(e.invokeInstanceMethod(t, "complete", new Object[] { "", i, null }));
		print.e(e.invokeInstanceMethod(t, "complete", new Object[] { "", bd, null }));

		print.e(t.testb(true, true));
		print.e(e.invokeInstanceMethod(t, "testb", new Object[] { null, true }));

		print.e(t.testStr(1, "string", 1L));
		print.e(e.invokeInstanceMethod(t, "testStr", new Object[] { "1", 1, Double.valueOf(1D) }));

		print.e(e.invokeInstanceMethod(t, "test", new Object[] { "1" }));
		print.e(e.invokeInstanceMethod(t, "test", new Object[] { 1D }));

		print.e(e.invokeInstanceMethod(new SystemOut(), "setOut", new Object[] { null }));
		System.setProperty("a.b.c", "- value -");
		print.e(e.invokeInstanceMethod(sb, "toSTring", new Object[] {}));
		print.e(e.invokeStaticMethod(SystemUtil.class, "getSystemPropOrEnvVar", new Object[] { "a.b.c", "default-value" }));
		print.e(e.invokeStaticMethod(ListUtil.class, "arrayToList", new Object[] { new String[] { "a", "b" }, "," }));

	}

	public static class Test {
		public final int complete(String var1, int var2, List var3) {
			return 5;
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
