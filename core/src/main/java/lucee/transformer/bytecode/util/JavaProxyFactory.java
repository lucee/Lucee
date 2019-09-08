/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.transformer.bytecode.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.KeyGenerator;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.util.JavaProxyUtil;
import lucee.transformer.bytecode.visitor.ArrayVisitor;

/**
 * creates a Java Proxy for components, so you can use componets as java classes following a certain
 * interface or class
 */
public class JavaProxyFactory {

	private static final String COMPONENT_NAME = "L" + Types.COMPONENT.getInternalName() + ";";
	private static final String CONFIG_WEB_NAME = "L" + Types.CONFIG_WEB.getInternalName() + ";";

	// private static final Type JAVA_PROXY = Type.getType(JavaProxy.class);

	private static final Type CFML_ENGINE_FACTORY = Type.getType(CFMLEngineFactory.class);
	private static final Type CFML_ENGINE = Type.getType(CFMLEngine.class);
	private static final Type JAVA_PROXY_UTIL = Type.getType(JavaProxyUtil.class);

	private static final org.objectweb.asm.commons.Method CALL = new org.objectweb.asm.commons.Method("call", Types.OBJECT,
			new Type[] { Types.CONFIG_WEB, Types.COMPONENT, Types.STRING, Types.OBJECT_ARRAY });

	private static final org.objectweb.asm.commons.Method CONSTRUCTOR = new org.objectweb.asm.commons.Method("<init>", Types.VOID,
			new Type[] { Types.CONFIG_WEB, Types.COMPONENT });
	private static final org.objectweb.asm.commons.Method SUPER_CONSTRUCTOR = new org.objectweb.asm.commons.Method("<init>", Types.VOID, new Type[] {});

	private static final org.objectweb.asm.commons.Method TO_BOOLEAN = new org.objectweb.asm.commons.Method("toBoolean", Types.BOOLEAN_VALUE, new Type[] { Types.OBJECT });
	private static final org.objectweb.asm.commons.Method TO_FLOAT = new org.objectweb.asm.commons.Method("toFloat", Types.FLOAT_VALUE, new Type[] { Types.OBJECT });
	private static final org.objectweb.asm.commons.Method TO_INT = new org.objectweb.asm.commons.Method("toInt", Types.INT_VALUE, new Type[] { Types.OBJECT });
	private static final org.objectweb.asm.commons.Method TO_DOUBLE = new org.objectweb.asm.commons.Method("toDouble", Types.DOUBLE_VALUE, new Type[] { Types.OBJECT });
	private static final org.objectweb.asm.commons.Method TO_LONG = new org.objectweb.asm.commons.Method("toLong", Types.LONG_VALUE, new Type[] { Types.OBJECT });
	private static final org.objectweb.asm.commons.Method TO_CHAR = new org.objectweb.asm.commons.Method("toChar", Types.CHAR, new Type[] { Types.OBJECT });
	private static final org.objectweb.asm.commons.Method TO_BYTE = new org.objectweb.asm.commons.Method("toByte", Types.BYTE_VALUE, new Type[] { Types.OBJECT });
	private static final org.objectweb.asm.commons.Method TO_SHORT = new org.objectweb.asm.commons.Method("toShort", Types.SHORT, new Type[] { Types.OBJECT });

	private static final org.objectweb.asm.commons.Method TO_STRING = new org.objectweb.asm.commons.Method("toString", Types.STRING, new Type[] { Types.OBJECT });

	private static final org.objectweb.asm.commons.Method TO_ = new org.objectweb.asm.commons.Method("to", Types.OBJECT, new Type[] { Types.OBJECT, Types.CLASS });

	private static final org.objectweb.asm.commons.Method _BOOLEAN = new org.objectweb.asm.commons.Method("toCFML", Types.OBJECT, new Type[] { Types.BOOLEAN_VALUE });
	private static final org.objectweb.asm.commons.Method _FLOAT = new org.objectweb.asm.commons.Method("toCFML", Types.OBJECT, new Type[] { Types.FLOAT_VALUE });
	private static final org.objectweb.asm.commons.Method _INT = new org.objectweb.asm.commons.Method("toCFML", Types.OBJECT, new Type[] { Types.INT_VALUE });
	private static final org.objectweb.asm.commons.Method _DOUBLE = new org.objectweb.asm.commons.Method("toCFML", Types.OBJECT, new Type[] { Types.DOUBLE_VALUE });
	private static final org.objectweb.asm.commons.Method _LONG = new org.objectweb.asm.commons.Method("toCFML", Types.OBJECT, new Type[] { Types.LONG_VALUE });
	private static final org.objectweb.asm.commons.Method _CHAR = new org.objectweb.asm.commons.Method("toCFML", Types.OBJECT, new Type[] { Types.CHAR });
	private static final org.objectweb.asm.commons.Method _BYTE = new org.objectweb.asm.commons.Method("toCFML", Types.OBJECT, new Type[] { Types.BYTE_VALUE });
	private static final org.objectweb.asm.commons.Method _SHORT = new org.objectweb.asm.commons.Method("toCFML", Types.OBJECT, new Type[] { Types.SHORT });
	private static final org.objectweb.asm.commons.Method _OBJECT = new org.objectweb.asm.commons.Method("toCFML", Types.OBJECT, new Type[] { Types.OBJECT });

	private static final org.objectweb.asm.commons.Method GET_INSTANCE = new org.objectweb.asm.commons.Method("getInstance", CFML_ENGINE, new Type[] {});
	private static final org.objectweb.asm.commons.Method GET_JAVA_PROXY_UTIL = new org.objectweb.asm.commons.Method("getJavaProxyUtil", Types.OBJECT, // FUTURE change to JavaProxy
			new Type[] {});

	/*
	 * 
	 * public static Object to(Object obj, Class clazz) { return obj; }
	 */

	/*
	 * public static Object createProxy(Config config,Component cfc, String className) throws
	 * PageException, IOException { return createProxy(cfc, null,
	 * ClassUtil.loadClass(config.getClassLoader(), className)); }
	 */

	public static Object createProxy(PageContext pc, Component cfc, Class extendz, Class... interfaces) throws PageException, IOException {
		PageContextImpl pci = (PageContextImpl) pc;
		// ((ConfigImpl)pci.getConfig()).getClassLoaderEnv()
		ClassLoader[] parents = extractClassLoaders(null, interfaces);

		if (extendz == null) extendz = Object.class;
		if (interfaces == null) interfaces = new Class[0];
		else {
			for (int i = 0; i < interfaces.length; i++) {
				if (!interfaces[i].isInterface()) throw new IOException("definition [" + interfaces[i].getName() + "] is a class and not an interface");
			}
		}

		Type typeExtends = Type.getType(extendz);
		Type[] typeInterfaces = ASMUtil.toTypes(interfaces);
		String[] strInterfaces = new String[typeInterfaces.length];
		for (int i = 0; i < strInterfaces.length; i++) {
			strInterfaces[i] = typeInterfaces[i].getInternalName();
		}

		String className = createClassName(extendz, interfaces);
		// Mapping mapping = cfc.getPageSource().getMapping();

		// get ClassLoader
		PhysicalClassLoader pcl = null;
		try {
			pcl = (PhysicalClassLoader) pci.getRPCClassLoader(false, parents);// mapping.getConfig().getRPCClassLoader(false)
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		Resource classFile = pcl.getDirectory().getRealResource(className.concat(".class"));

		// check if already exists, if yes return
		if (classFile.exists()) {
			try {
				Object obj = newInstance(pcl, className, pc.getConfig(), cfc);
				if (obj != null) return obj;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		/*
		 * String classNameOriginal=component.getPageSource().getFullClassName(); String
		 * realOriginal=classNameOriginal.replace('.','/'); Resource classFileOriginal =
		 * mapping.getClassRootDirectory().getRealResource(realOriginal.concat(".class"));
		 */
		ClassWriter cw = ASMUtil.getClassWriter();

		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, className, null, typeExtends.getInternalName(), strInterfaces);
		// BytecodeContext statConstr = null;//new
		// BytecodeContext(null,null,null,cw,real,ga,Page.STATIC_CONSTRUCTOR);
		// BytecodeContext constr = null;//new BytecodeContext(null,null,null,cw,real,ga,Page.CONSTRUCTOR);

		// field Component
		FieldVisitor _fv = cw.visitField(Opcodes.ACC_PRIVATE, "cfc", COMPONENT_NAME, null, null);
		_fv.visitEnd();
		_fv = cw.visitField(Opcodes.ACC_PRIVATE, "config", CONFIG_WEB_NAME, null, null);
		_fv.visitEnd();

		// Constructor
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR, null, null, cw);
		Label begin = new Label();
		adapter.visitLabel(begin);
		adapter.loadThis();
		adapter.invokeConstructor(Types.OBJECT, SUPER_CONSTRUCTOR);

		// adapter.putField(JAVA_PROXY, arg1, arg2)

		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.visitVarInsn(Opcodes.ALOAD, 1);
		adapter.visitFieldInsn(Opcodes.PUTFIELD, className, "config", CONFIG_WEB_NAME);

		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.visitVarInsn(Opcodes.ALOAD, 2);
		adapter.visitFieldInsn(Opcodes.PUTFIELD, className, "cfc", COMPONENT_NAME);

		adapter.visitInsn(Opcodes.RETURN);
		Label end = new Label();
		adapter.visitLabel(end);
		adapter.visitLocalVariable("config", CONFIG_WEB_NAME, null, begin, end, 1);
		adapter.visitLocalVariable("cfc", COMPONENT_NAME, null, begin, end, 2);

		// adapter.returnValue();
		adapter.endMethod();

		// create methods
		Set<Class> cDone = new HashSet<Class>();
		Map<String, Class> mDone = new HashMap<String, Class>();
		for (int i = 0; i < interfaces.length; i++) {
			_createProxy(cw, cDone, mDone, cfc, interfaces[i], className);
		}
		cw.visitEnd();

		// create class file
		byte[] barr = cw.toByteArray();

		try {
			ResourceUtil.touch(classFile);
			IOUtil.copy(new ByteArrayInputStream(barr), classFile, true);

			pcl = (PhysicalClassLoader) pci.getRPCClassLoader(true, parents);
			Class<?> clazz = pcl.loadClass(className, barr);
			return newInstance(clazz, pc.getConfig(), cfc);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	private static ClassLoader[] extractClassLoaders(ClassLoader cl, Class[] classes) {
		HashSet<ClassLoader> set = new HashSet<>();
		if (cl != null) {
			set.add(cl);
			cl = null;
		}
		if (classes != null) {
			for (int i = 0; i < classes.length; i++) {
				set.add(ClassUtil.getClassLoader(classes[i]));
			}
		}
		return set.toArray(new ClassLoader[set.size()]);
	}

	private static void _createProxy(ClassWriter cw, Set<Class> cDone, Map<String, Class> mDone, Component cfc, Class clazz, String className) throws IOException {
		if (cDone.contains(clazz)) return;

		cDone.add(clazz);

		// super class
		Class superClass = clazz.getSuperclass();
		if (superClass != null) _createProxy(cw, cDone, mDone, cfc, superClass, className);

		// interfaces
		Class[] interfaces = clazz.getInterfaces();
		if (interfaces != null) for (int i = 0; i < interfaces.length; i++) {
			_createProxy(cw, cDone, mDone, cfc, interfaces[i], className);
		}

		Method[] methods = clazz.getMethods();
		if (methods != null) for (int i = 0; i < methods.length; i++) {
			_createMethod(cw, mDone, methods[i], className);
		}
	}

	private static void _createMethod(ClassWriter cw, Map<String, Class> mDone, Method src, String className) throws IOException {
		final Class<?>[] classArgs = src.getParameterTypes();
		final Class<?> classRtn = src.getReturnType();

		String str = src.getName() + "(" + Reflector.getDspMethods(classArgs) + ")";
		Class rtnClass = mDone.get(str);
		if (rtnClass != null) {
			if (rtnClass != classRtn) throw new IOException("there is a conflict with method [" + str + "], this method is declared more than once with different return types.");
			return;
		}
		mDone.put(str, classRtn);

		Type[] typeArgs = ASMUtil.toTypes(classArgs);
		Type typeRtn = Type.getType(classRtn);

		org.objectweb.asm.commons.Method method = new org.objectweb.asm.commons.Method(src.getName(), typeRtn, typeArgs);
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, method, null, null, cw);
		// BytecodeContext bc = new
		// BytecodeContext(statConstr,constr,null,null,keys,cw,className,adapter,method,writeLog);
		Label start = adapter.newLabel();
		adapter.visitLabel(start);

		// if the result of "call" need castring, we have to do this here
		if (needCastring(classRtn)) {
			adapter.invokeStatic(CFML_ENGINE_FACTORY, GET_INSTANCE);
			adapter.invokeInterface(CFML_ENGINE, GET_JAVA_PROXY_UTIL);
			adapter.checkCast(JAVA_PROXY_UTIL);
		}

		adapter.invokeStatic(CFML_ENGINE_FACTORY, GET_INSTANCE);
		adapter.invokeInterface(CFML_ENGINE, GET_JAVA_PROXY_UTIL);
		adapter.checkCast(JAVA_PROXY_UTIL);

		// Java Proxy.call(cfc,"add",new Object[]{arg0})
		// config (first argument)
		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.visitFieldInsn(Opcodes.GETFIELD, className, "config", CONFIG_WEB_NAME);

		// cfc (second argument)
		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.visitFieldInsn(Opcodes.GETFIELD, className, "cfc", COMPONENT_NAME);

		// name (3th argument)
		adapter.push(src.getName());

		// arguments (4th argument)
		ArrayVisitor av = new ArrayVisitor();
		av.visitBegin(adapter, Types.OBJECT, typeArgs.length);
		for (int y = 0; y < typeArgs.length; y++) {
			av.visitBeginItem(adapter, y);

			adapter.invokeStatic(CFML_ENGINE_FACTORY, GET_INSTANCE);
			adapter.invokeInterface(CFML_ENGINE, GET_JAVA_PROXY_UTIL);
			adapter.checkCast(JAVA_PROXY_UTIL);

			adapter.loadArg(y);
			if (classArgs[y] == boolean.class) adapter.invokeInterface(JAVA_PROXY_UTIL, _BOOLEAN);
			else if (classArgs[y] == byte.class) adapter.invokeInterface(JAVA_PROXY_UTIL, _BYTE);
			else if (classArgs[y] == char.class) adapter.invokeInterface(JAVA_PROXY_UTIL, _CHAR);
			else if (classArgs[y] == double.class) adapter.invokeInterface(JAVA_PROXY_UTIL, _DOUBLE);
			else if (classArgs[y] == float.class) adapter.invokeInterface(JAVA_PROXY_UTIL, _FLOAT);
			else if (classArgs[y] == int.class) adapter.invokeInterface(JAVA_PROXY_UTIL, _INT);
			else if (classArgs[y] == long.class) adapter.invokeInterface(JAVA_PROXY_UTIL, _LONG);
			else if (classArgs[y] == short.class) adapter.invokeInterface(JAVA_PROXY_UTIL, _SHORT);
			else adapter.invokeInterface(JAVA_PROXY_UTIL, _OBJECT);

			av.visitEndItem(adapter);
		}
		av.visitEnd();
		adapter.invokeInterface(JAVA_PROXY_UTIL, CALL);

		// CFMLEngineFactory.getInstance().getCastUtil().toBooleanValue(o);

		// Java Proxy.to...(...);
		int rtn = Opcodes.IRETURN;
		if (classRtn == boolean.class) adapter.invokeInterface(JAVA_PROXY_UTIL, TO_BOOLEAN);
		else if (classRtn == byte.class) adapter.invokeInterface(JAVA_PROXY_UTIL, TO_BYTE);
		else if (classRtn == char.class) adapter.invokeInterface(JAVA_PROXY_UTIL, TO_CHAR);
		else if (classRtn == double.class) {
			rtn = Opcodes.DRETURN;
			adapter.invokeInterface(JAVA_PROXY_UTIL, TO_DOUBLE);
		}
		else if (classRtn == float.class) {
			rtn = Opcodes.FRETURN;
			adapter.invokeInterface(JAVA_PROXY_UTIL, TO_FLOAT);
		}
		else if (classRtn == int.class) adapter.invokeInterface(JAVA_PROXY_UTIL, TO_INT);
		else if (classRtn == long.class) {
			rtn = Opcodes.LRETURN;
			adapter.invokeInterface(JAVA_PROXY_UTIL, TO_LONG);
		}
		else if (classRtn == short.class) adapter.invokeInterface(JAVA_PROXY_UTIL, TO_SHORT);
		else if (classRtn == void.class) {
			rtn = Opcodes.RETURN;
			adapter.pop();
		}
		else if (classRtn == String.class) {
			rtn = Opcodes.ARETURN;
			adapter.invokeInterface(JAVA_PROXY_UTIL, TO_STRING);
		}
		else {
			rtn = Opcodes.ARETURN;
			adapter.checkCast(typeRtn);
		}

		adapter.visitInsn(rtn);
		adapter.endMethod();

	}

	private static boolean needCastring(Class<?> classRtn) {
		return classRtn == boolean.class || classRtn == byte.class || classRtn == char.class || classRtn == double.class || classRtn == float.class || classRtn == int.class
				|| classRtn == long.class || classRtn == short.class || classRtn == String.class;
	}

	private static Object newInstance(PhysicalClassLoader cl, String className, ConfigWeb config, Component cfc) throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, ClassNotFoundException {
		return newInstance(cl.loadClass(className), config, cfc);
	}

	private static Object newInstance(Class<?> _clazz, ConfigWeb config, Component cfc)
			throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		Constructor<?> constr = _clazz.getConstructor(new Class[] { ConfigWeb.class, Component.class });
		return constr.newInstance(new Object[] { config, cfc });
	}

	private static String createClassName(Class extendz, Class[] interfaces) throws IOException {
		if (extendz == null) extendz = Object.class;

		StringBuilder sb = new StringBuilder(extendz.getName());
		if (interfaces != null && interfaces.length > 0) {
			sb.append(';');

			String[] arr = new String[interfaces.length];
			for (int i = 0; i < interfaces.length; i++) {
				arr[i] = interfaces[i].getName();
			}
			Arrays.sort(arr);

			sb.append(lucee.runtime.type.util.ListUtil.arrayToList(arr, ";"));
		}

		String key = KeyGenerator.createVariable(sb.toString());

		return key;
	}

}