package lucee.transformer.direct;

import java.io.IOException;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import lucee.print;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.lang.Pair;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.SystemOut;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.bytecode.util.ASMUtil;

public class DirectCallEngine {

	private static DirectCallEngine engine;
	private Map<Integer, DirectClassLoader> loaders = new HashMap<>();
	private Resource root;
	private static final Object token = new SerializableObject();

	public DirectCallEngine(Resource configDir) {
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

	public static DirectCallEngine getInstance(Resource configDir) {
		if (engine == null) {
			engine = new DirectCallEngine(configDir);
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

	// TODO handles isStatic better with proper exceptions
	/*
	 * executes a instance method of the given object
	 * 
	 */
	private Object invoke(Object objMaybeNull, Class<?> objClass, Key methodName, Object[] arguments) throws Exception {
		if (objMaybeNull != null && objMaybeNull.getClass() != objClass) {
			String a = Caster.toTypeName(objMaybeNull.getClass());
			String b = Caster.toTypeName(objClass);
			if (a.equals(b)) throw new NoSuchMethodException("given object from type [" + a + "] is loaded by a different classloader than the given class");
			else throw new NoSuchMethodException("given object from type [" + a + "] from given class [" + b + "]");
		}
		BIF instance = (BIF) createInstance(objClass, methodName, arguments).getValue();
		if (objMaybeNull == null) {
			return instance.invoke(null, arguments);
		}
		else {
			PageContextDummy dummy = PageContextDummy.getDummy(objMaybeNull);
			try {
				return instance.invoke(dummy, arguments);
			}
			finally {
				PageContextDummy.returnDummy(dummy);
			}
		}

	}

	public Pair<Method, Object> createInstance(Class<?> clazz, Key methodName, Object[] arguments) throws NoSuchMethodException, IOException, ClassNotFoundException,
			UnmodifiableClassException, PageException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {

		arguments = Reflector.cleanArgs(arguments);
		Method method = Reflector.getMethod(clazz, methodName, arguments, true);
		// constr = Reflector.getConstructorInstance(clazz, methodName, arguments, true);

		Parameter[] parameters = method.getParameters();
		clazz = method.getDeclaringClass(); // we wanna go as low as possible, to be as open as possible also this avoid not allow to access

		StringBuilder sbClassPath = new StringBuilder();
		sbClassPath.append(clazz.getName().replace('.', '/')).append('/').append(method.getName());
		StringBuilder sbArgs = new StringBuilder();
		for (int i = 0; i < parameters.length; i++) {
			sbArgs.append(':').append(parameters[i].getType().getName().replace('.', '_'));
		}
		sbClassPath.append('_').append(HashUtil.create64BitHashAsString(sbArgs, Character.MAX_RADIX));
		String classPath = "lucee/invoc/wrap/" + sbClassPath.toString();// StringUtil.replace(sbClassPath.toString(), "javae/lang/", "java_lang/", false);
		String className = classPath.replace('/', '.');
		// print.e("classPath: " + classPath);
		// print.e("className: " + className);
		DirectClassLoader loader = getCL(clazz);
		if (loader.hasClass(className)) {
			// print.e("existing!!!" + className);
			return new Pair<Method, Object>(method, loader.loadInstance(className));
		}

		ClassWriter cw = ASMUtil.getClassWriter();
		MethodVisitor mv;

		String abstractClassPath = "lucee/runtime/ext/function/BIF";
		cw.visit(ASMUtil.getJavaVersionForBytecodeGeneration(), Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, classPath, null, abstractClassPath, null);

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
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "invoke", "(Llucee/runtime/PageContext;[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
		mv.visitCode();

		boolean isStatic = Modifier.isStatic(method.getModifiers());
		if (!isStatic) {
			// Load the instance to call the method on
			mv.visitVarInsn(Opcodes.ALOAD, 1); // Load the first method argument (instance)
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "lucee/runtime/PageContext", "getPage", "()Ljava/lang/Object;", false);
			if (!clazz.equals(Object.class)) { // Only cast if clazz is not java.lang.Object
				mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(clazz));
			}
		}
		// Assuming no arguments are needed for the invoked method, i.e., toString()
		// For methods that require arguments, you would need to manipulate the args array appropriately
		// here

		// print.e(Type.getInternalName(clazz));

		StringBuilder methodDesc = new StringBuilder();
		String del = "(";
		if (method.getParameterCount() > 0) {
			// Load method arguments from the args array
			Type[] args = Type.getArgumentTypes(method);
			// TODO if args!=arguments throw !
			for (int i = 0; i < args.length; i++) {

				methodDesc.append(del).append(args[i].getDescriptor());
				del = "";

				mv.visitVarInsn(Opcodes.ALOAD, 2); // Load the args array
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
		Type rt = Type.getReturnType(method);
		methodDesc.append(')').append(rt.getDescriptor());
		// print.e(methodDesc);
		mv.visitMethodInsn(isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL, Type.getInternalName(clazz), method.getName(), methodDesc.toString(), false); // Dynamically
																																									// invoke
																																									// the

		boxIfPrimitive(mv, rt);
		// method on the
		// instance
		mv.visitInsn(Opcodes.ARETURN); // Return the result of the method call

		mv.visitMaxs(1, 3); // Compute automatically
		mv.visitEnd();

		cw.visitEnd();
		byte[] barr = cw.toByteArray();
		Object result = loader.loadInstance(className, barr);

		return new Pair<Method, Object>(method, result);
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

	private DirectClassLoader getCL(Class<?> clazz) {
		ClassLoader parent = clazz.getClassLoader();
		if (parent == null) parent = getClass().getClassLoader();// core classloader

		DirectClassLoader cl = loaders.get(parent.hashCode());
		if (cl == null) {
			synchronized (token) {
				cl = loaders.get(parent.getName());
				if (cl == null) {
					print.e("---- newnewnewnewnewnew ---- " + parent.toString());
					loaders.put(parent.hashCode(), cl = new DirectClassLoader(parent, root));
				}
			}
		}
		return cl;
	}

	public static void main(String[] args) throws Exception {
		DirectCallEngine e = new DirectCallEngine(ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/tmp8/classes/"));
		StringBuilder sb = new StringBuilder("Susi");
		Test t = new Test();
		Integer i = Integer.valueOf(3);
		BigDecimal bd = BigDecimal.TEN;

		TimeZone tz = java.util.TimeZone.getDefault();
		ArrayList arr = new ArrayList<>();
		System.identityHashCode(arr);

		print.e(tz.getID());
		print.e(e.invokeInstanceMethod(tz, "getID", new Object[] {}));

		print.e(e.invokeInstanceMethod(t, "test", new Object[] { 134D }));

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

	private static class Test {
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
