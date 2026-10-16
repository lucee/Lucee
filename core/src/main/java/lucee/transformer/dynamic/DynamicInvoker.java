package lucee.transformer.dynamic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import lucee.aprint;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.SystemOut;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.MethodInstance;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.FunctionMember;
import lucee.transformer.dynamic.meta.Method;

public final class DynamicInvoker {

	private static final long MAX_AGE = 30 * 60 * 60 * 1000;
	private static DynamicInvoker engine;
	private Map<Integer, SoftReference<DynamicClassLoader>> loaders = new ConcurrentHashMap<>();
	private Resource root;
	private Log _log;
	private static final Object token = new SerializableObject();

	private static Map<String, AtomicInteger> observer = new ConcurrentHashMap<>();

	public DynamicInvoker(Resource configDir) {

		try {
			this.root = configDir.getRealResource("dynclasses");
			// loader = new DirectClassLoader(configDir.getRealResource("reflection"));
		}
		catch (Exception e) {
			if (getLog() != null) getLog().error("dynamic", e);
			this.root = SystemUtil.getTempDirectory();
			// loader = new DirectClassLoader(SystemUtil.getTempDirectory());

			// e.printStackTrace();
		}

	}

	public Log getLog() {
		if (_log == null) {
			_log = ThreadLocalPageContext.getLog("application");
		}
		return _log;
	}

	public static DynamicInvoker getInstance(Resource configDir) {
		if (engine == null) {
			engine = new DynamicInvoker(configDir);
		}
		return engine;
	}

	public static DynamicInvoker getExistingInstance() {
		return engine;
	}

	public Object invokeStaticMethod(Class<?> clazz, Key methodName, Object[] arguments, boolean nameCaseSensitive, boolean convertComparsion) throws Exception {
		return invoke(null, clazz, methodName, arguments, nameCaseSensitive, convertComparsion);
	}

	public Object invokeStaticMethod(Class<?> clazz, String methodName, Object[] arguments, boolean nameCaseSensitive, boolean convertComparsion) throws Exception {
		return invoke(null, clazz, KeyImpl.init(methodName), arguments, nameCaseSensitive, convertComparsion);
	}

	public Object invokeInstanceMethod(Object obj, Key methodName, Object[] arguments, boolean nameCaseSensitive, boolean convertComparsion) throws Exception {
		return invoke(obj, obj.getClass(), methodName, arguments, nameCaseSensitive, convertComparsion);
	}

	public Object invokeInstanceMethod(Object obj, String methodName, Object[] arguments, boolean nameCaseSensitive, boolean convertComparsion) throws Exception {
		return invoke(obj, obj.getClass(), KeyImpl.init(methodName), arguments, nameCaseSensitive, convertComparsion);
	}

	public Object invokeConstructor(Class<?> clazz, Object[] arguments, boolean convertComparsion) throws Exception {
		return invoke(null, clazz, null, arguments, true, convertComparsion);
	}

	// TODO handles isStatic better with proper exceptions
	/*
	 * executes a instance method of the given object
	 * 
	 */
	private Object invoke(Object objMaybeNull, Class<?> objClass, Key methodName, Object[] arguments, boolean nameCaseSensitive, boolean convertComparsion) throws Exception {
		Clazz clazzz = toClazz(objClass);
		if (methodName == null) {
			return clazzz.getConstructor(arguments, true, convertComparsion).newInstance(arguments);
		}
		return clazzz.getMethod(methodName.getString(), arguments, nameCaseSensitive, true, convertComparsion).invoke(objMaybeNull, arguments);
	}

	public Clazz getClazz(Class<?> clazz) {
		return Clazz.getClazz(clazz, root, getLog());
	}

	public Clazz getClazz(Class<?> clazz, boolean useReflection) {
		return Clazz.getClazz(clazz, root, getLog(), useReflection);
	}

	private static double getClass = 0;
	private static double match = 0;
	private static double getDeclaringClass = 0;
	private static double pathName = 0;
	private static double clsLoader = 0;
	private static double loadInstance = 0;

	public Clazz toClazz(Class<?> clazz) {
		return Clazz.getClazz(clazz, root, getLog());
	}

	private lucee.transformer.dynamic.meta.FunctionMember getFunctionMember(Clazz clazzz, Key methodName, Object[] arguments, boolean nameCaseSensitive, boolean convertComparsion)
			throws NoSuchMethodException {
		return (methodName == null) ? clazzz.getConstructor(arguments, true, convertComparsion)
				: clazzz.getMethod(methodName.getString(), arguments, nameCaseSensitive, true, convertComparsion);
	}

	public Object getInstance(Clazz clazzz, final lucee.transformer.dynamic.meta.FunctionMember fm, Object[] arguments) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, UnmodifiableClassException, IOException {

		String className = fm.getClassName();
		DynamicClassLoader loader = clazzz.getDynamicClassLoader(this);

		if (loader.hasClass(className)) {
			try {
				return loader.loadInstance(className);

			}
			catch (Exception e) {
				// simply ignore when fail
			}
		}
		return createInstance(clazzz, fm, fm instanceof lucee.transformer.dynamic.meta.Constructor, className, loader);
	}

	private Object createInstance(Clazz clazzz, lucee.transformer.dynamic.meta.FunctionMember fm, boolean isConstr, String className, DynamicClassLoader loader)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException,
			UnmodifiableClassException, IOException {

		synchronized (SystemUtil.createToken("dyninvocer", className)) {
			Class[] parameterClasses = fm.getArgumentClasses();

			ClassWriter cw = ASMUtil.getClassWriter();
			MethodVisitor mv;
			String abstractClassPath = "java/lang/Object";
			cw.visit(ASMUtil.getJavaVersionForBytecodeGeneration(), Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, fm.getClassPath(),
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
				mv.visitTypeInsn(Opcodes.NEW, Type.getType(clazzz.getDeclaringClass()).getInternalName());
				mv.visitInsn(Opcodes.DUP); // Duplicate the top operand stack value

			}
			else {
				isStatic = fm.isStatic();
				if (!isStatic) {
					// Load the instance to call the method on
					mv.visitVarInsn(Opcodes.ALOAD, 1); // Load the first method argument (instance)
					if (!fm.getDeclaringProviderClassWithSameAccess().equals(Object.class)) { // Only cast if clazz is not java.lang.Object
						mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(fm.getDeclaringProviderClassWithSameAccess()));
					}
				}
			}
			// Assuming no arguments are needed for the invoked method, i.e., toString()
			// For methods that require arguments, you would need to manipulate the args array appropriately
			// here

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
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(wrapperType), ASMUtil.getClassName(type) + "Value", "()" + ASMUtil.getDescriptor(type),
								false); // Unbox
					}
					else {
						mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(argType)); // Cast to correct type
					}
				}
			}
			else {
				methodDesc.append('(');
			}
			Type rt;
			// return type
			{
				// constructor
				if (isConstr) {
					rt = Type.getType(clazzz.getDeclaringClass());
				}
				// static method
				else if (isStatic) {
					rt = fm.getReturnType();
				}
				// instance method
				else {
					Class tmp = ((lucee.transformer.dynamic.meta.Method) fm).getDeclaringProviderRtnClassWithSameAccess();
					if (tmp != null) rt = Type.getType(tmp);
					else rt = fm.getReturnType();
				}
			}

			methodDesc.append(')').append(isConstr ? Types.VOID : rt.getDescriptor());
			// constructor
			if (isConstr) {
				// Create a new instance of java/lang/String
				mv.visitMethodInsn(Opcodes.INVOKESPECIAL, rt.getInternalName(), "<init>", methodDesc.toString(), false); // Call the constructor of String
			}
			// static method
			else if (isStatic) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(fm.getDeclaringClass()), fm.getName(), methodDesc.toString(), fm.getDeclaringClass().isInterface());
			}
			// instance method
			else {
				mv.visitMethodInsn((fm.getDeclaringProviderClassWithSameAccess().isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL),
						Type.getInternalName(fm.getDeclaringProviderClassWithSameAccess()), fm.getName(), methodDesc.toString(),
						fm.getDeclaringProviderClassWithSameAccess().isInterface());

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
			return loader.loadInstance(className, barr);
		}
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

	public DynamicClassLoader getCL(Class<?> clazz) {
		ClassLoader parent = clazz.getClassLoader();
		if (parent == null) parent = SystemUtil.getCoreClassLoader();
		int hash = System.identityHashCode(parent);
		SoftReference<DynamicClassLoader> ref = loaders.get(hash);
		DynamicClassLoader cl = ref == null ? null : ref.get();
		if (cl == null) {
			synchronized (token) {
				ref = loaders.get(hash);
				cl = ref == null ? null : ref.get();
				if (cl == null) {
					loaders.put(parent.hashCode(), new SoftReference<>(cl = new DynamicClassLoader(parent, root, getLog())));
				}
			}
		}
		return cl;
	}

	public int remove(ClassLoader parent) {
		int count = 0;
		int hash = System.identityHashCode(parent);
		SoftReference<DynamicClassLoader> ref = loaders.get(hash);
		DynamicClassLoader cl = ref == null ? null : ref.get();
		if (cl != null) {
			synchronized (token) {
				ref = loaders.get(hash);
				cl = ref == null ? null : ref.get();
				if (cl != null) {
					count++;
					loaders.remove(hash);
				}
			}
		}
		return count;
	}

	public void cleanup() {
		Set<Resource> set = new java.util.HashSet<>();
		DynamicClassLoader cl;
		for (SoftReference<DynamicClassLoader> ref: loaders.values()) {
			cl = ref.get();
			if (cl == null) continue;
			Resource directory = cl.getRootDirectory();
			if (!set.contains(directory) && directory.isDirectory()) {
				set.add(directory);
			}
		}
		for (Resource directory: set) {
			if (ResourceUtil.deleteFileOlderThan(directory, System.currentTimeMillis() - MAX_AGE, null)) {
				try {
					ResourceUtil.deleteEmptyFolders(directory);
				}
				catch (IOException e) {}
			}
		}
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

	public static class TestMule {

		public TestMule() {

		}

		public TestMule(int x) {

		}

		public void test(int x) {

		}
	}

	public Pair<FunctionMember, Object> createInstance2(Class<?> clazz, Key methodName, Object[] arguments, boolean convertComparsion) {

		return null;
	}

	public static class C<E> extends ArrayList<E> {
		public static <E> C<E> of(E e1) {
			C c = new C();
			c.add(e1);
			c.add(e1);
			c.add(e1);
			return c;
		}
	}

	public static void main(String[] argsw) throws Throwable {
		System.setProperty("lucee.allow.reflection", "true");
		Resource classes = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/tmp8/classes/");
		ResourceUtil.deleteContent(classes, null);

		DynamicInvoker e = DynamicInvoker.getInstance(classes);
		if (false) {
			aprint.e(A.x());
			aprint.e(B.x());
			aprint.e(e.invokeStaticMethod(A.class, "x", new Object[] {}, true, true));
			aprint.e(e.invokeStaticMethod(B.class, "x", new Object[] {}, true, true));

		}

		if (true) {
			Test t = new Test();
			e.invokeInstanceMethod(t, "setX", new Object[] { BigDecimal.valueOf(1) }, true, true);
			e.invokeInstanceMethod(t, "setX", new Object[] { BigInteger.valueOf(1) }, true, true);
			e.invokeInstanceMethod(t, "setX", new Object[] { Double.valueOf(1) }, true, true);
			e.invokeInstanceMethod(t, "setX", new Object[] { "1" }, true, true);
			e.invokeInstanceMethod(t, "setLong", new Object[] { "1" }, true, true);
			e.invokeInstanceMethod(t, "setLong", new Object[] { 1 }, true, true);
			e.invokeInstanceMethod(t, "setLong", new Object[] { (double) 1 }, true, true);
			e.invokeInstanceMethod(t, "setLong", new Object[] { BigDecimal.valueOf(1) }, true, true);
			e.invokeInstanceMethod(t, "setLong", new Object[] { BigInteger.valueOf(1) }, true, true);
			e.invokeInstanceMethod(t, "setLong", new Object[] { (double) 1 }, true, true);

			aprint.e(e.invokeConstructor(Date.class, new Object[] { (long) 1 }, true));
			aprint.e(e.invokeConstructor(Date.class, new Object[] { (int) 1 }, true));
			aprint.e(e.invokeConstructor(Date.class, new Object[] { (double) 1 }, true));
			aprint.e(e.invokeConstructor(Date.class, new Object[] { BigDecimal.valueOf(1) }, true));
			aprint.e(e.invokeConstructor(Date.class, new Object[] { BigInteger.valueOf(1) }, true));

			// aprint.e(e.invokeConstructor(Date.class, new Object[] { "1" }, true));

			// new Date( javacast( "long", lastModifiedCommit.getCommitTime() * 1000 ) )

			return;
		}

		if (true) {

			/*
			 * for (java.lang.reflect.Method m: C.class.getMethods()) { if (m.getName().equals("of"))
			 * aprint.e(">" + m); }
			 */

			// aprint.e(e.invokeStaticMethod(List.class, "of", new Object[] { "Susi" }, true, true));
			aprint.e(e.invokeStaticMethod(List.class, "of", new Object[] { "Susi" }, true, true));
			aprint.e(e.invokeStaticMethod(C.class, "of", new Object[] { "Susi" }, true, true));

			// aprint.e(List.of("ww"));
			// aprint.e(C.of("ww"));
			// aprint.e(ArrayList.of(""));
			return;
		}

		if (true) {

			HashMap<String, String> arr = new HashMap<>();
			arr.put("Susi", "Sorglos");
			arr.clone();
			MethodInstance mi = Reflector.getMethodInstance(arr.getClass(), KeyImpl.init("clone"), new Object[] {}, true, true);
			aprint.e(mi.hasMethod());
			aprint.e(mi.invoke(arr));

		}

		{

			LinkedHashMap<String, String> arr = new LinkedHashMap<>();
			arr.put("Susi", "Sorglos");
			arr.clone();
			MethodInstance mi = Reflector.getMethodInstance(arr.getClass(), KeyImpl.init("clone"), new Object[] {}, true, true);
			aprint.e(mi.hasMethod());
			aprint.e(mi.invoke(arr));

			if (true) return;
		}

		{
			ArrayImpl arr = new ArrayImpl();
			arr.setE(1, "Susi");

			aprint.e(arr);
			aprint.e(arr.clone());

			aprint.e(e.invokeInstanceMethod(arr, KeyImpl.init("clone"), new Object[] {}, true, true));

		}

		DynamicInvoker.getInstance(classes);
		if (true) {

			int rounds = 6;
			int max = 500000;
			long dynamicInvoker = Long.MAX_VALUE;
			long dynamicInvoker2 = Long.MAX_VALUE;
			long dynamicInvoker3 = Long.MAX_VALUE;
			long reflection = Long.MAX_VALUE;
			long direct = Long.MAX_VALUE;
			long methodHandle = Long.MAX_VALUE;
			long tmp;
			TestMule tm = new TestMule();
			Class<? extends TestMule> clazz = tm.getClass();
			Class[] cargs = new Class[] { int.class };
			Object[] args = new Object[] { 1 };
			Key methodName = new KeyImpl("Test");
			e.invokeConstructor(clazz, new Object[] { 1 }, false);
			e.invokeConstructor(String.class, new Object[] { "" }, false);

			// reflection
			for (int i = 0; i < rounds; i++) {
				long start = System.currentTimeMillis();
				for (int y = 0; y < max; y++) {
					clazz.getMethod("test", cargs).invoke(tm, args);
				}
				tmp = System.currentTimeMillis() - start;
				if (tmp < reflection) reflection = tmp;
			}

			// invokeInstanceMethod
			for (int i = 0; i < rounds; i++) {
				long start = System.currentTimeMillis();
				for (int y = 0; y < max; y++) {
					e.invokeInstanceMethod(tm, methodName, args, false, false);
				}
				tmp = System.currentTimeMillis() - start;
				if (tmp < dynamicInvoker) dynamicInvoker = tmp;
			}

			// invokeInstanceMethod
			for (int i = 0; i < rounds; i++) {
				long start = System.currentTimeMillis();
				for (int y = 0; y < max; y++) {
					// Reflector.getMethodInstance(clazz, methodName, new Object[] { 1 }, false, false).invoke(tm);
					// Reflector.getMethod(clazz, "test", cargs, true);
					Reflector.getMethod(clazz, "test", cargs, true).invoke(tm, args);
				}
				tmp = System.currentTimeMillis() - start;
				if (tmp < dynamicInvoker2) dynamicInvoker2 = tmp;
			}

			// invokeInstanceMethod
			for (int i = 0; i < rounds; i++) {
				long start = System.currentTimeMillis();
				for (int y = 0; y < max; y++) {
					Reflector.callMethod(tm, methodName, args, false);
				}
				tmp = System.currentTimeMillis() - start;
				if (tmp < dynamicInvoker3) dynamicInvoker3 = tmp;
			}

			// MethodHandles
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			for (int i = 0; i < rounds; i++) {
				long start = System.currentTimeMillis();
				for (int y = 0; y < max; y++) {
					MethodType methodType = MethodType.methodType(void.class, int.class);
					MethodHandle testHandle = lookup.findVirtual(clazz, "test", methodType);
					testHandle.invoke(tm, 1);

				}
				tmp = System.currentTimeMillis() - start;
				if (tmp < methodHandle) methodHandle = tmp;
			}

			// direct
			for (int i = 0; i < rounds; i++) {
				long start = System.currentTimeMillis();
				for (int y = 0; y < max; y++) {
					tm.test(1);
				}
				tmp = System.currentTimeMillis() - start;
				if (tmp < direct) direct = tmp;
			}

			aprint.e("invokeInstanceMethod:" + dynamicInvoker);
			aprint.e("Reflector.getMethod:" + dynamicInvoker2);
			aprint.e("Reflector.callMethod:" + dynamicInvoker3);
			aprint.e("reflection:" + reflection);
			aprint.e("methodHandle:" + methodHandle);
			aprint.e("direct:" + direct);

			aprint.e("-------------------");
			double total = getClass + match + getDeclaringClass + pathName + clsLoader + loadInstance;

			aprint.e(((int) getClass) + ":" + Caster.toIntValue(100d / total * getClass) + "% :getClass");
			aprint.e(((int) match) + ":" + Caster.toIntValue(100d / total * match) + "% :match");
			aprint.e(((int) getDeclaringClass) + ":" + Caster.toIntValue(100d / total * getDeclaringClass) + "% :getDeclaringClass");
			aprint.e(((int) pathName) + ":" + Caster.toIntValue(100d / total * pathName) + "% :pathName");
			aprint.e(((int) clsLoader) + ":" + Caster.toIntValue(100d / total * clsLoader) + "% :clsLoader");
			aprint.e(((int) loadInstance) + ":" + Caster.toIntValue(100d / total * loadInstance) + "% :loadInstance");

			// if (true) return;
		}

		{

			List<Method> methods;

			DynamicInvoker.getInstance(classes);
			// methods = Reflector.getMethods(lucee.runtime.config.ConfigWebImpl.class);
			// methods = Reflector.getMethods(lucee.runtime.PageContextImpl.class);
			List<Method> methodsw = Reflector.getMethods(lucee.runtime.config.ConfigWebImpl.class);

			// methods = Reflector.getMethods(lucee.runtime.config.ConfigServerImpl.class);
			methods = Reflector.getMethods(lucee.runtime.config.ConfigServerImpl.class);

			// int max = 500;
			aprint.e("xxxxxxxxxxxxxxxx ConfigServerImpl  xxxxxxxxxxxxxxxxx");
			for (Method method: methods) {
				// if (!method.getName().startsWith("reset") || method.getName().equals("reset") ||
				// method.getName().equals("resetAll") || method.getArgumentCount() != 0) continue;
				if (method.getDeclaringProviderClassNameWithSameAccess().indexOf("ConfigWeb") != -1) {
					aprint.e("->" + method.getDeclaringProviderClassNameWithSameAccess() + ":"

							+ method.getDeclaringProviderClassName() + ":" + method.getDeclaringClassName() + ":" + method.getName());
					// throw new RuntimeException("ups!");
					// if (--max == 0) break;
				}
			}
			aprint.e("xxxxxxxxxxxxxxxx ConfigWebImpl  xxxxxxxxxxxxxxxxx");
			for (Method method: methodsw) {
				// if (!method.getName().startsWith("reset") || method.getName().equals("reset") ||
				// method.getName().equals("resetAll") || method.getArgumentCount() != 0) continue;
				if (method.getDeclaringProviderClassNameWithSameAccess().indexOf("ConfigServer") != -1) {
					aprint.e("->" + method.getDeclaringProviderClassNameWithSameAccess() + ":"

							+ method.getDeclaringProviderClassName() + ":" + method.getDeclaringClassName() + ":" + method.getName());
					// throw new RuntimeException("ups!");
					// if (--max == 0) break;
				}
			}
		}
		/// if (true) return;
		HashMap map = new HashMap<>();
		map.put("aaa", "sss");
		Iterator it = map.keySet().iterator();
		// aprint.e(e.invokeInstanceMethod(it, "next", new Object[] {}, false));
		aprint.e(e.invokeInstanceMethod(it, "hasNext", new Object[] {}, true, false));

		if (false) {
			FileInputStream fis = new java.io.FileInputStream("/Users/mic/Tmp3/test.prop");
			InputStreamReader fir = new java.io.InputStreamReader(fis, "UTF-8");
			PropertyResourceBundle prb = new java.util.PropertyResourceBundle(fir);
			Enumeration<String> keys = prb.getKeys();
			String key;
			aprint.e(e.invokeInstanceMethod(keys, "hasMoreElements", new Object[] {}, true, false));
			while (keys.hasMoreElements()) {
				key = (String) e.invokeInstanceMethod(keys, "nextElement", new Object[] {}, true, false);
				aprint.e(key);
				aprint.e(prb.handleGetObject(key));
				aprint.e(e.invokeInstanceMethod(prb, "handleGetObject", new Object[] { key }, true, false));
			}
			fis.close();
			System.exit(0);
		}

		{

			int[] arr = new int[] { 1, 2 };

			aprint.e(e.invokeInstanceMethod(arr, "toString", new Object[] {}, true, false));
		}

		{

			// zoneId = createObject( "java", "java.time.ZoneId" );
			// chronoField = createObject( "java", "java.time.temporal.ChronoField" );

			// dump( now().toInstant().atZone( zoneId.of( "US/Central" ) ).toLocalDateTime()
			// .with( ChronoField.DAY_OF_WEEK, javacast( "long", 1 ) ))

			ZoneId zoneId = (ZoneId) e.invokeStaticMethod(java.time.ZoneId.class, "of", new Object[] { "US/Central" }, true, false);
			Instant instant = (Instant) e.invokeInstanceMethod(new DateTimeImpl(), "toInstant", new Object[] {}, true, false);

			ZonedDateTime zdt = (ZonedDateTime) e.invokeInstanceMethod(instant, "atZone", new Object[] { zoneId }, true, false);
			LocalDateTime ldt = (LocalDateTime) e.invokeInstanceMethod(zdt, "toLocalDateTime", new Object[] {}, true, false);
			Object r = e.invokeInstanceMethod(ldt, "with", new Object[] { ChronoField.DAY_OF_WEEK, 1L }, true, false);
			aprint.e(r);

		}

		// if (true) return;

		StringBuilder sb = new StringBuilder("Susi");
		Test t = new Test();
		Integer i = Integer.valueOf(3);
		BigDecimal bd = BigDecimal.TEN;

		TimeZone tz = java.util.TimeZone.getDefault();
		ArrayList arr = new ArrayList<>();

		Object sadas1 = e.invokeInstanceMethod(sb, "append", new Object[] { "sss" }, true, false);
		aprint.e(sadas1);

		// java.util.HashMap.EntrySet
		Thread.getAllStackTraces().entrySet().iterator();
		Object sadasd = e.invokeInstanceMethod(Thread.getAllStackTraces().entrySet(), "iterator", new Object[] {}, true, false);
		// System.exit(0);
		String str = new String("Susi exclusive");
		aprint.e(str);
		aprint.e(e.invokeConstructor(String.class, new Object[] { "Susi exclusive" }, false));

		// System.exit(0);

		Object eee = e.invokeInstanceMethod(t, "setSource", new Object[] { "" }, true, false);
		// System.exit(0);

		// source
		// instance ():String
		{
			Object reflection = tz.getID();
			Object dynamic = e.invokeInstanceMethod(tz, "getID", new Object[] {}, true, false);
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
			Object dynamic = e.invokeInstanceMethod(t, "test", new Object[] { 134D }, true, true);
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
			Object dynamic = e.invokeInstanceMethod(t, "test", new Object[] { 134D }, true, true);
			if (!reflection.equals(dynamic)) {
				aprint.e("direct:");
				aprint.e(reflection);
				aprint.e("dynamic:");
				aprint.e(dynamic);
			}
		}

		aprint.e(t.complete("", 1, null));
		aprint.e(e.invokeInstanceMethod(t, "complete", new Object[] { "", i, null }, true, true));
		aprint.e(e.invokeInstanceMethod(t, "complete", new Object[] { "", bd, null }, true, true));

		aprint.e(t.testb(true, true));
		aprint.e(e.invokeInstanceMethod(t, "testb", new Object[] { null, true }, true, true));

		aprint.e(t.testStr(1, "string", 1L));
		aprint.e(e.invokeInstanceMethod(t, "testStr", new Object[] { "1", 1, Double.valueOf(1D) }, true, true));

		aprint.e(e.invokeInstanceMethod(t, "test", new Object[] { "1" }, true, true));
		aprint.e(e.invokeInstanceMethod(t, "test", new Object[] { 1D }, true, true));

		aprint.e(e.invokeInstanceMethod(new SystemOut(), "setOut", new Object[] { null }, true, true));
		System.setProperty("a.b.c", "- value -");
		aprint.e(e.invokeInstanceMethod(sb, "toSTring", new Object[] {}, false, false));
		aprint.e(e.invokeStaticMethod(SystemUtil.class, "getSystemPropOrEnvVar", new Object[] { "a.b.c", "default-value" }, true, true));
		aprint.e(e.invokeStaticMethod(ListUtil.class, "arrayToList", new Object[] { new String[] { "a", "b" }, "," }, true, true));
		aprint.e("done");

	}

	public static class Test {
		public final int complete(String var1, int var2, List var3) {
			return 5;
		}

		public final void setSource(Object o) {

		}

		public final void setX(BigInteger bi) {
			aprint.e("->BigInteger:" + bi);
		}

		public final void setX(BigDecimal bi) {
			aprint.e("->BigDecimal:" + bi);
		}

		public final void setLong(long l) {
			aprint.e("->long:" + l);
		}

		public final void setDouble(double d) {
			aprint.e("->double:" + d);
		}

		public final void setDouble(int i) {
			aprint.e("->int:" + i);
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

	public static class A {
		public static String x() {
			return "A";
		}
	}

	public static class B extends A {
		public static String x() {
			return "B";
		}
	}

}
