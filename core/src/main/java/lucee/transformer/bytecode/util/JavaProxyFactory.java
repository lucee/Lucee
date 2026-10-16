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
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.DirectoryProvider;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.ExtendableClassLoader;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.PhysicalClassLoaderFactory;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Component;
import lucee.runtime.ComponentImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.java.JavaProxy;
import lucee.runtime.op.Caster;
import lucee.runtime.op.JavaProxyUtilImpl;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.UDF;
import lucee.runtime.util.JavaProxyUtil;
import lucee.transformer.bytecode.visitor.ArrayVisitor;

/**
 * creates a Java Proxy for components, so you can use componets as java classes following a certain
 * interface or class
 */
public final class JavaProxyFactory {

	// !!! for every change in terfaces this version need to be updated TODO generate automatically
	private static final int VERSION = 5;

	private static final short TYPE_CFC = 1;
	private static final short TYPE_UDF = 2;

	private static final String UDF_NAME = "L" + Types.UDF.getInternalName() + ";";
	private static final String COMPONENT_NAME = "L" + Types.COMPONENT.getInternalName() + ";";
	private static final String CONFIG_WEB_NAME = "L" + Types.CONFIG_WEB.getInternalName() + ";";

	// private static final Type JAVA_PROXY = Type.getType(JavaProxy.class);
	private static final Type JAVA_PROXY = Type.getType(JavaProxy.class);

	private static final Type CFML_ENGINE_FACTORY = Type.getType(CFMLEngineFactory.class);
	private static final Type CFML_ENGINE = Type.getType(CFMLEngine.class);
	private static final Type JAVA_PROXY_UTIL = Type.getType(JavaProxyUtil.class);
	private static final Type JAVA_PROXY_UTIL_IMPL = Type.getType(JavaProxyUtilImpl.class);

	private static final org.objectweb.asm.commons.Method CALL_CFC = new org.objectweb.asm.commons.Method("call", Types.OBJECT,
			new Type[] { Types.CONFIG_WEB, Types.COMPONENT, Types.STRING, Types.OBJECT_ARRAY });
	private static final org.objectweb.asm.commons.Method CALL_UDF = new org.objectweb.asm.commons.Method("call", Types.OBJECT,
			new Type[] { Types.CONFIG_WEB, Types.UDF, Types.STRING, Types.OBJECT_ARRAY });
	// JavaProxy.call() overloads that also convert the result to the declared return type
	private static final org.objectweb.asm.commons.Method CALL_CFC_RTN = new org.objectweb.asm.commons.Method("call", Types.OBJECT,
			new Type[] { Types.CONFIG_WEB, Types.COMPONENT, Types.STRING, Types.CLASS, Types.OBJECT_ARRAY });
	private static final org.objectweb.asm.commons.Method CALL_UDF_RTN = new org.objectweb.asm.commons.Method("call", Types.OBJECT,
			new Type[] { Types.CONFIG_WEB, Types.UDF, Types.STRING, Types.CLASS, Types.OBJECT_ARRAY });

	private static final org.objectweb.asm.commons.Method CONSTRUCTOR_CONFIG_CFC_0 = new org.objectweb.asm.commons.Method("<init>", Types.VOID, new Type[] {});
	private static final org.objectweb.asm.commons.Method CONSTRUCTOR_CONFIG_CFC_2 = new org.objectweb.asm.commons.Method("<init>", Types.VOID,
			new Type[] { Types.CONFIG_WEB, Types.COMPONENT });
	private static final org.objectweb.asm.commons.Method CONSTRUCTOR_CONFIG_UDF = new org.objectweb.asm.commons.Method("<init>", Types.VOID,
			new Type[] { Types.CONFIG_WEB, Types.UDF });

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
	private static final org.objectweb.asm.commons.Method GET_THREAD_CONFIG = new org.objectweb.asm.commons.Method("getThreadConfig", Types.CONFIG, new Type[] {});
	private static final org.objectweb.asm.commons.Method GET_THREAD_PAGECONTEXT = new org.objectweb.asm.commons.Method("getThreadPageContext", Types.PAGE_CONTEXT, new Type[] {});
	private static final org.objectweb.asm.commons.Method CREATE_PAGECONTEXT = new org.objectweb.asm.commons.Method("createPageContext", Types.PAGE_CONTEXT, new Type[] {});

	private static final org.objectweb.asm.commons.Method GET_JAVA_PROXY = new org.objectweb.asm.commons.Method("getJavaProxy", JAVA_PROXY_UTIL, new Type[] {});
	private static final org.objectweb.asm.commons.Method GET_CONFIG = new org.objectweb.asm.commons.Method("getConfig", Types.CONFIG_WEB, new Type[] {});
	private static final org.objectweb.asm.commons.Method GET = new org.objectweb.asm.commons.Method("get", Types.PAGE_CONTEXT, new Type[] {});
	private static final org.objectweb.asm.commons.Method LOAD_COMPONENT = new org.objectweb.asm.commons.Method("loadComponent", Types.COMPONENT, new Type[] { Types.STRING });
	// private static final org.objectweb.asm.commons.Method LOAD_INLINE = new
	// org.objectweb.asm.commons.Method("loadInline", Types.COMPONENT,new Type[] { Types.PAGE_CONTEXT,
	// Types.STRING, Types.STRING });

	private static final org.objectweb.asm.commons.Method LOAD_INLINE = new org.objectweb.asm.commons.Method("loadInline", Types.COMPONENT,
			new Type[] { Types.STRING, Types.STRING });
	private static final org.objectweb.asm.commons.Method TO_COMPONENT = new org.objectweb.asm.commons.Method("_toComponent", Types.COMPONENT, new Type[] {});

	public static Object createProxy(Object defaultValue, PageContext pc, UDF udf, Class interf) {
		try {
			return createProxy(pc, udf, interf);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static Object createProxy(PageContext pc, UDF udf, Class interf) throws PageException, IOException {
		PageContextImpl pci = (PageContextImpl) pc;
		ClassLoader pcl = getRPCClassLoaderFromClass(pc, interf);
		if (pcl == null) pcl = pci.getRPCClassLoader();

		if (!interf.isInterface()) throw new IOException("definition [" + interf.getName() + "] is a class and not a interface");

		Type typeExtends = Types.OBJECT;
		Type typeInterface = Type.getType(interf);
		String strInterface = typeInterface.getInternalName();
		String className = createClassName("udf", null, ((DirectoryProvider) pcl).getDirectory(), Object.class, interf);

		Resource classFile = ((DirectoryProvider) pcl).getDirectory().getRealResource(className.concat(".class"));

		// check if already exists, if yes return
		if (classFile.exists()) {
			try {
				Object obj = newInstance(pcl, className, pc.getConfig(), udf);
				if (obj != null) return obj;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		ClassWriter cw = ASMUtil.getClassWriter();

		cw.visit(ASMUtil.getJavaVersionForBytecodeGeneration(), Opcodes.ACC_PUBLIC, className, null, typeExtends.getInternalName(), new String[] { strInterface });

		// field Component
		FieldVisitor _fv = cw.visitField(Opcodes.ACC_PRIVATE, "udf", UDF_NAME, null, null);
		_fv.visitEnd();
		_fv = cw.visitField(Opcodes.ACC_PRIVATE, "config", CONFIG_WEB_NAME, null, null);
		_fv.visitEnd();

		// Constructor
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_CONFIG_UDF, null, null, cw);
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
		adapter.visitFieldInsn(Opcodes.PUTFIELD, className, "udf", UDF_NAME);

		adapter.visitInsn(Opcodes.RETURN);
		Label end = new Label();
		adapter.visitLabel(end);
		adapter.visitLocalVariable("config", CONFIG_WEB_NAME, null, begin, end, 1);
		adapter.visitLocalVariable("udf", UDF_NAME, null, begin, end, 2);

		// adapter.returnValue();
		adapter.endMethod();

		// create methods
		Set<Class> cDone = new HashSet<Class>();
		Map<String, Class> mDone = new HashMap<String, Class>();
		_createProxy(cw, cDone, mDone, udf, interf, className);
		cw.visitEnd();

		// create class file
		byte[] barr = ASMUtil.verify(cw.toByteArray());

		try {
			ResourceUtil.touch(classFile);
			IOUtil.copy(new ByteArrayInputStream(barr), classFile, true);

			Class<?> clazz = ((ExtendableClassLoader) pcl).loadClass(className, barr);
			return newInstance(clazz, pc.getConfig(), udf);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	public static Object createProxy(Object defaultValue, PageContext pc, Component cfc, Class extendz, Class... interfaces) {
		try {
			return createProxy(pc, cfc, extendz, interfaces);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static Object createProxy(PageContext pc, final Component cfc, Class extendz, Class... interfaces) throws PageException, IOException {
		PageContextImpl pci = (PageContextImpl) pc;
		DirectoryProvider dp;
		ClassLoader cl = getRPCClassLoaderFromClasses(pc, extendz, interfaces);
		if (cl == null) {
			cl = pci.getRPCClassLoader();
		}
		dp = (DirectoryProvider) cl;

		boolean hasTemplates = false;
		if (extendz == null) extendz = Object.class;
		else hasTemplates = true;

		if (interfaces == null) {
			interfaces = new Class[0];
		}
		else {
			for (int i = 0; i < interfaces.length; i++) {
				if (!interfaces[i].isInterface()) throw new IOException("definition [" + interfaces[i].getName() + "] is a class and not an interface");
			}
		}
		if (!hasTemplates && interfaces.length > 0) hasTemplates = true;

		Type typeExtends = Type.getType(extendz);

		String[] strInterfaces;
		{
			Type[] typeInterfaces = ASMUtil.toTypes(interfaces);
			strInterfaces = new String[typeInterfaces.length];
			for (int i = 0; i < typeInterfaces.length; i++) {
				strInterfaces[i] = typeInterfaces[i].getInternalName();
			}
		}

		String className = createClassName("cfc", cfc, dp.getDirectory(), extendz, interfaces);
		String classPath = className.replace('.', '/'); // Ensure classPath is using slashes
		Resource classFile = dp.getDirectory().getRealResource(classPath.concat(".class"));
		if (classFile.exists()) {
			// we already have it in store
			try {
				Object obj = newInstance(cl, className, pc.getConfig(), cfc);
				if (obj != null) return obj;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				classFile.delete();
			}
		}
		// when we get here, it not exist (anymore)
		synchronized (SystemUtil.createToken("JavaProxyFactory.createProxy", className)) {
			// we need to check again if it exist in the lock
			if (!classFile.exists()) {
				ClassWriter cw = ASMUtil.getClassWriter();
				cw.visit(ASMUtil.getJavaVersionForBytecodeGeneration(), Opcodes.ACC_PUBLIC, classPath, null, typeExtends.getInternalName(), strInterfaces);

				// field Component
				FieldVisitor _fv = cw.visitField(Opcodes.ACC_PUBLIC, "cfc", COMPONENT_NAME, null, null);
				_fv.visitEnd();
				_fv = cw.visitField(Opcodes.ACC_PRIVATE, "config", CONFIG_WEB_NAME, null, null);
				_fv.visitEnd();

				// Descriptor for local variables
				String descriptor = 'L' + classPath + ';';

				// CFMLEngineFactory.getInstance().createPageContext(null, null, null, null, null, null, null, null,
				// null, -1, true);

				// Constructor with 0 arguments
				{
					GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_CONFIG_CFC_0, null, null, cw);
					Label begin = new Label();
					Label end = new Label();

					adapter.visitLabel(begin);

					adapter.loadThis();
					adapter.invokeConstructor(Types.OBJECT, SUPER_CONSTRUCTOR);

					// PageContextUtil.createPageContext();
					adapter.invokeStatic(Types.PAGE_CONTEXT_UTIL, CREATE_PAGECONTEXT);
					adapter.visitVarInsn(Opcodes.ASTORE, 1); // Store the PageContext in a local variable (index 1)

					// this.config = pc.getConfig();
					adapter.loadThis(); // Load 'this' onto the stack
					adapter.visitVarInsn(Opcodes.ALOAD, 1); // Load the PageContext (local variable 1)
					adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_CONFIG); // Call getConfig() on PageContext
					adapter.visitFieldInsn(Opcodes.PUTFIELD, classPath, "config", CONFIG_WEB_NAME); // this.config = <result>

					{
						String sub = ((ComponentImpl) cfc).getSubName();

						if (!StringUtil.isEmpty(sub)) {
							// inline
							if (((ComponentImpl) cfc).getInline()) {
								adapter.loadThis(); // Load 'this' onto the stack
								adapter.visitVarInsn(Opcodes.ALOAD, 1); // Load the PageContext (local variable 1)
								adapter.push((((ComponentImpl) cfc).getPageSource().getRealpathWithVirtual()));
								adapter.push(sub);
								adapter.invokeVirtual(Types.PAGE_CONTEXT, LOAD_INLINE);

							}
							// sub
							else {
								// this.cfc = pc.loadComponent("quartz.CFMJob");
								adapter.loadThis(); // Load 'this' onto the stack
								adapter.visitVarInsn(Opcodes.ALOAD, 1); // Load the PageContext (local variable 1)
								adapter.push(cfc.getAbsName() + "$" + sub);
								adapter.invokeVirtual(Types.PAGE_CONTEXT, LOAD_COMPONENT);
							}
						}
						else {
							// this.cfc = pc.loadComponent("quartz.CFMJob");
							adapter.loadThis(); // Load 'this' onto the stack
							adapter.visitVarInsn(Opcodes.ALOAD, 1); // Load the PageContext (local variable 1)
							adapter.push(cfc.getAbsName());
							adapter.invokeVirtual(Types.PAGE_CONTEXT, LOAD_COMPONENT);
						}
					}

					adapter.visitFieldInsn(Opcodes.PUTFIELD, classPath, "cfc", COMPONENT_NAME);

					// End label
					adapter.visitLabel(end);

					adapter.visitInsn(Opcodes.RETURN);
					adapter.visitLabel(end);
					adapter.visitLocalVariable("this", descriptor, null, begin, end, 0); // Correctly define 'this' as local variable 0
					adapter.visitLocalVariable("pc", ASMUtil.getDescriptor(Types.PAGE_CONTEXT), null, begin, end, 1); // Define 'pc' as local variable 1
					adapter.visitLocalVariable("config", CONFIG_WEB_NAME, null, begin, end, 1); // Correctly define 'config' as local variable 1
					adapter.visitLocalVariable("cfc", COMPONENT_NAME, null, begin, end, 2); // Correctly define 'cfc' as local variable 2

					adapter.endMethod();
				}

				// Constructor with 2 arguments
				{
					GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_CONFIG_CFC_2, null, null, cw);
					Label begin = new Label();
					adapter.visitLabel(begin);
					adapter.loadThis();
					adapter.invokeConstructor(Types.OBJECT, SUPER_CONSTRUCTOR);

					adapter.visitVarInsn(Opcodes.ALOAD, 0);
					adapter.visitVarInsn(Opcodes.ALOAD, 1);
					adapter.visitFieldInsn(Opcodes.PUTFIELD, classPath, "config", CONFIG_WEB_NAME);

					adapter.visitVarInsn(Opcodes.ALOAD, 0);
					adapter.visitVarInsn(Opcodes.ALOAD, 2);
					adapter.visitFieldInsn(Opcodes.PUTFIELD, classPath, "cfc", COMPONENT_NAME);

					adapter.visitInsn(Opcodes.RETURN);
					Label end = new Label();
					adapter.visitLabel(end);
					adapter.visitLocalVariable("this", descriptor, null, begin, end, 0); // Correctly define 'this' as local variable 0
					adapter.visitLocalVariable("config", CONFIG_WEB_NAME, null, begin, end, 1); // Correctly define 'config' as local variable 1
					adapter.visitLocalVariable("cfc", COMPONENT_NAME, null, begin, end, 2); // Correctly define 'cfc' as local variable 2

					adapter.endMethod();
				}

				// create toComponent
				{

					// Create a GeneratorAdapter for the toComponent method
					GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC, TO_COMPONENT, null, null, cw);

					Label begin = new Label();
					adapter.visitLabel(begin);

					// Load 'this' onto the stack (for accessing the instance field)
					adapter.loadThis();

					// Get the value of 'cfc' field (ALoad 0, then getField)
					adapter.visitFieldInsn(Opcodes.GETFIELD, classPath, "cfc", Type.getDescriptor(Component.class));

					// Return the value (which is of type Component)
					adapter.returnValue();

					// Define the labels and local variables for debugging purposes
					Label end = new Label();
					adapter.visitLabel(end);
					adapter.visitLocalVariable("this", descriptor, null, begin, end, 0); // 'this' as local variable 0

					// Complete the method definition
					adapter.endMethod();
				}

				// create methods
				Set<Class> cDone = new HashSet<Class>();
				Map<String, Class> mDone = new HashMap<String, Class>();
				for (int i = 0; i < interfaces.length; i++) {
					_createProxy(cw, cDone, mDone, cfc, interfaces[i], classPath);
				}
				// if (!hasTemplates) {
				createProxyFromComponentInterface(cl, cw, cDone, mDone, cfc, classPath);
				// }
				cw.visitEnd();

				// create class file
				byte[] barr = ASMUtil.verify(cw.toByteArray());

				try {
					ResourceUtil.touch(classFile);
					IOUtil.copy(new ByteArrayInputStream(barr), classFile, true);

					Class<?> clazz = ((ExtendableClassLoader) cl).loadClass(className, barr);
					return newInstance(clazz, pc.getConfig(), cfc);
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}

			try {
				return newInstance(cl, className, pc.getConfig(), cfc);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}

		}

	}

	private static ClassLoader getRPCClassLoaderFromClasses(PageContext pc, Class extendz, Class... interfaces) throws IOException {
		// extends and implement need to come from the same parent classloader
		ClassLoader pcl = null;
		if (extendz != null) {
			pcl = getRPCClassLoaderFromClass(pc, extendz);
			if (pcl != null) return pcl;
		}

		if (interfaces != null) {
			for (Class cls: interfaces) {
				pcl = getRPCClassLoaderFromClass(pc, cls);
				if (pcl != null) return pcl;
			}
		}
		return null;
	}

	public static ClassLoader getRPCClassLoaderFromClass(PageContext pc, Class clazz) throws IOException {
		ClassLoader cl = clazz.getClassLoader();
		if (cl != null) {
			if (cl instanceof PhysicalClassLoader) {
				return cl;
			}
			else if (cl instanceof BundleClassLoader) {
				return PhysicalClassLoaderFactory.getRPCClassLoader(pc.getConfig(), (BundleClassLoader) cl, false);
			}
		}
		return null;
	}

	private static void _createProxy(ClassWriter cw, Set<Class> cDone, Map<String, Class> mDone, UDF udf, Class clazz, String className) throws IOException {
		if (cDone.contains(clazz)) return;

		cDone.add(clazz);

		// super class
		Class superClass = clazz.getSuperclass();
		if (superClass != null) _createProxy(cw, cDone, mDone, udf, superClass, className);

		// interfaces
		Class[] interfaces = clazz.getInterfaces();
		if (interfaces != null) for (int i = 0; i < interfaces.length; i++) {
			_createProxy(cw, cDone, mDone, udf, interfaces[i], className);
		}

		Method[] methods = clazz.getMethods();
		if (methods != null) for (int i = 0; i < methods.length; i++) {
			if (methods[i].isDefault() || Modifier.isStatic(methods[i].getModifiers())) continue;
			_createMethod(cw, mDone, new SimpleMethodReflect(methods[i]), className, TYPE_UDF);
		}
	}

	private static void createProxyFromComponentInterface(ClassLoader cl, ClassWriter cw, Set<Class> cDone, Map<String, Class> mDone, Component cfc, String className) {
		if (cDone.contains(cfc.getClass())) return;
		cDone.add(cfc.getClass());

		ComponentImpl cfci = (ComponentImpl) cfc;
		List<SimpleMethod> methods = ComponentImpl.getSimpleMethods(cl, cfci, ComponentImpl.ACCESS_PUBLIC);

		if (methods != null) {
			for (SimpleMethod m: methods) {
				_createMethod(cw, mDone, m, className, TYPE_CFC);
			}
		}
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
			_createMethod(cw, mDone, new SimpleMethodReflect(methods[i]), className, TYPE_CFC);
		}
	}

	private static class SimpleMethodReflect extends SimpleMethodSupport {
		private Method method;

		public SimpleMethodReflect(Method method) {
			this.method = method;
		}

		@Override
		public String getName() {
			return method.getName();
		}

		@Override
		public Class[] getParameterClasses() {
			return method.getParameterTypes();
		}

		@Override
		public Class[] getParameterClasses(Class defaultValue) {
			return method.getParameterTypes();
		}

		@Override
		public Class getReturnClass() {
			return method.getReturnType();
		}

		@Override
		public Class getReturnClass(Class defaultValue) {
			return method.getReturnType();
		}

		@Override
		public String getReturnType() {
			return method.getReturnType().getName();
		}
	}

	private static class SimpleMethodProvided extends SimpleMethodSupport {

		private String name;
		private Class[] parameterTypes;
		private Class returnType;

		public SimpleMethodProvided(String name, Class[] parameterTypes, Class returnType) {
			this.name = name;
			this.parameterTypes = parameterTypes;
			this.returnType = returnType;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class[] getParameterClasses() {
			return parameterTypes;
		}

		@Override
		public Class[] getParameterClasses(Class defaultValue) {
			return parameterTypes;
		}

		@Override
		public Class getReturnClass() {
			return returnType;
		}

		@Override
		public Class getReturnClass(Class defaultValue) {
			return returnType;
		}

		@Override
		public String getReturnType() {
			return returnType.getName();
		}
	}

	private static void _createMethod(ClassWriter cw, Map<String, Class> mDone, SimpleMethod src, String className, short type) {
		// TODO allow return type of the class itself
		final Class<?>[] classArgs = src.getParameterClasses(Object.class);
		final Class<?> classRtn = src.getReturnClass(Object.class);

		String str = src.getName() + "(" + Reflector.getDspMethods(classArgs) + ")";
		Class rtnClass = mDone.get(str);
		if (rtnClass != null) {
			// if (rtnClass != classRtn) throw new IOException("there is a conflict with method [" + str + "],
			// this method is declared more than once with different return types.");
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

		// reference returns other than Object and String are converted inside JavaProxy.call(), while the
		// PageContext is still alive
		final boolean convertRtn = !classRtn.isPrimitive() && classRtn != Object.class && classRtn != String.class;

		// if the result of "call" need castring, we have to do this here
		if (needCastring(classRtn)) {
			adapter.invokeStatic(CFML_ENGINE_FACTORY, GET_INSTANCE);
			adapter.invokeInterface(CFML_ENGINE, GET_JAVA_PROXY);
			adapter.checkCast(JAVA_PROXY_UTIL);
		}

		if (!convertRtn) {
			adapter.invokeStatic(CFML_ENGINE_FACTORY, GET_INSTANCE);
			adapter.invokeInterface(CFML_ENGINE, GET_JAVA_PROXY);
			// adapter.checkCast(type == TYPE_CFC ? JAVA_PROXY_UTIL : JAVA_PROXY_UTIL_IMPL);
			// the UDF call() only exists on JavaProxyUtilImpl, not on the loader interface
			if (type != TYPE_CFC) adapter.checkCast(JAVA_PROXY_UTIL_IMPL);
		}

		// Java Proxy.call(cfc,"add",new Object[]{arg0})
		// config (first argument)
		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		adapter.visitFieldInsn(Opcodes.GETFIELD, className, "config", CONFIG_WEB_NAME);

		// cfc (second argument)
		adapter.visitVarInsn(Opcodes.ALOAD, 0);
		if (type == TYPE_CFC) adapter.visitFieldInsn(Opcodes.GETFIELD, className, "cfc", COMPONENT_NAME);
		else adapter.visitFieldInsn(Opcodes.GETFIELD, className, "udf", UDF_NAME);

		// name (3th argument)
		adapter.push(src.getName());

		// declared return type
		if (convertRtn) adapter.push(typeRtn);

		// arguments (4th argument)
		ArrayVisitor av = new ArrayVisitor();
		av.visitBegin(adapter, Types.OBJECT, typeArgs.length);
		for (int y = 0; y < typeArgs.length; y++) {
			av.visitBeginItem(adapter, y);

			adapter.invokeStatic(CFML_ENGINE_FACTORY, GET_INSTANCE);
			adapter.invokeInterface(CFML_ENGINE, GET_JAVA_PROXY);
			// adapter.checkCast(JAVA_PROXY_UTIL);

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
		if (convertRtn) adapter.invokeStatic(JAVA_PROXY, type == TYPE_CFC ? CALL_CFC_RTN : CALL_UDF_RTN);
		else if (type == TYPE_CFC) adapter.invokeInterface(JAVA_PROXY_UTIL, CALL_CFC);
		else adapter.invokeVirtual(JAVA_PROXY_UTIL_IMPL, CALL_UDF);

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

	private static Object newInstance(ClassLoader cl, String className, ConfigWeb config, UDF udf) throws IllegalArgumentException, InstantiationException, IllegalAccessException,
			InvocationTargetException, SecurityException, NoSuchMethodException, ClassNotFoundException {
		return newInstance(cl.loadClass(className), config, udf);
	}

	private static Object newInstance(Class<?> _clazz, ConfigWeb config, UDF udf)
			throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		Constructor<?> constr = _clazz.getConstructor(new Class[] { ConfigWeb.class, UDF.class });
		return constr.newInstance(new Object[] { config, udf });
	}

	private static Object newInstance(ClassLoader cl, String className, ConfigWeb config, Component cfc) throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, ClassNotFoundException {
		return newInstance(cl.loadClass(className), config, cfc);
	}

	private static Object newInstance(Class<?> _clazz, ConfigWeb config, Component cfc)
			throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		Constructor<?> constr = _clazz.getConstructor(new Class[] { ConfigWeb.class, Component.class });
		return constr.newInstance(new Object[] { config, cfc });
	}

	private static String createClassName(String appendix, Component cfc, Resource resource, Class extendz, Class... interfaces) {
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

		sb.append(appendix).append(';').append(VERSION);
		StringBuilder name = new StringBuilder().append(appendix.charAt(0)).append(HashUtil.create64BitHashAsString(sb.toString(), Character.MAX_RADIX).toLowerCase());
		if (cfc != null && !StringUtil.isEmpty(cfc.getAbsName())) {
			name.append('.').append(cfc.getAbsName());

			String sub = ((ComponentImpl) cfc).getSubName();
			if (!StringUtil.isEmpty(sub)) {
				name.append('$').append(sub);
			}
		}
		return name.toString();
	}
}