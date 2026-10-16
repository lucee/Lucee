/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
 */
package lucee.runtime.type.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.commons.digest.MD5;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.DirectoryProvider;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.ExtendableClassLoader;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.runtime.Component;
import lucee.runtime.ComponentImpl;
import lucee.runtime.ComponentScope;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.Mapping;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.component.AbstractFinal.UDFB;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.component.Property;
import lucee.runtime.component.PropertyImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.listener.JavaSettings;
import lucee.runtime.net.rpc.server.WSServer;
import lucee.runtime.net.rpc.server.WSUtil;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Pojo;
import lucee.runtime.type.SimpleValue;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFGSProperty;
import lucee.runtime.type.UDFPropertiesBase;
import lucee.runtime.type.dt.DateTime;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.ConstrBytecodeContext;
import lucee.transformer.bytecode.PageImpl;
import lucee.transformer.bytecode.util.ASMProperty;
import lucee.transformer.bytecode.util.ASMPropertyImpl;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.ArrayVisitor;
import lucee.transformer.expression.literal.LitString;

public final class ComponentUtil {

	private final static Method CONSTRUCTOR_OBJECT = Method.getMethod("void <init> ()");
	private static final Method INVOKE = new Method("invoke", Types.OBJECT, new Type[] { Types.STRING, Types.OBJECT_ARRAY });

	public static final Type SERVER_WSUTIL = Type.getType(WSUtil.class);
	public static final short HAS_INIT_UNDEFINED = (short) 0;
	public static final short HAS_INIT_TRUE = (short) 1;
	public static final short HAS_INIT_FALSE = (short) 2;

	/**
	 * generate a ComponentJavaAccess (CJA) class from a component a CJA is a dynamic genarted java
	 * class that has all method defined inside a component as java methods.
	 * 
	 * This is used to generated server side Webservices.
	 * 
	 * @param component
	 * @param isNew
	 * @return
	 * @throws PageException
	 */
	public static Class getComponentJavaAccess(PageContext pc, Component component, RefBoolean isNew, boolean create, boolean writeLog, boolean suppressWSbeforeArg, boolean output,
			boolean returnValue) throws PageException {
		isNew.setValue(false);
		String classNameOriginal = component.getPageSource().getClassName();
		String className = getClassname(component, null).concat("_wrap");
		String real = className.replace('.', '/');
		String realOriginal = classNameOriginal.replace('.', '/');
		Mapping mapping = component.getPageSource().getMapping();
		ClassLoader cl = null;
		try {
			cl = ((PageContextImpl) pc).getRPCClassLoader();
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		Resource classFile = ((DirectoryProvider) cl).getDirectory().getRealResource(real.concat(".class"));
		Resource classFileOriginal = mapping.getClassRootDirectory().getRealResource(realOriginal.concat(".class"));

		// LOAD CLASS
		// print.out(className);
		// check last Mod
		if (classFile.lastModified() >= classFileOriginal.lastModified()) {
			try {
				Class clazz = cl.loadClass(className);
				if (clazz != null && !hasChangesOfChildren(classFile.lastModified(), clazz)) return registerTypeMapping(clazz);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		if (!create) return null;
		isNew.setValue(true);
		// print.out("new");
		// CREATE CLASS
		ClassWriter cw = ASMUtil.getClassWriter();
		cw.visit(ASMUtil.getJavaVersionForBytecodeGeneration(), Opcodes.ACC_PUBLIC, real, null, "java/lang/Object", null);

		// GeneratorAdapter ga = new
		// GeneratorAdapter(Opcodes.ACC_PUBLIC,Page.STATIC_CONSTRUCTOR,null,null,cw);
		// StaticConstrBytecodeContext statConstr = null;//new
		// BytecodeContext(null,null,null,cw,real,ga,Page.STATIC_CONSTRUCTOR);

		/// ga = new GeneratorAdapter(Opcodes.ACC_PUBLIC,Page.CONSTRUCTOR,null,null,cw);
		ConstrBytecodeContext constr = null;// new BytecodeContext(null,null,null,cw,real,ga,Page.CONSTRUCTOR);

		// field component
		// FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, "c", "Llucee/runtime/ComponentImpl;", null,
		// null);
		// fv.visitEnd();

		Map<LitString, Integer> _keys = new LinkedHashMap<LitString, Integer>();

		// remote methods
		Collection.Key[] keys = component.keys(Component.ACCESS_REMOTE);
		int max;
		for (int i = 0; i < keys.length; i++) {
			max = -1;
			while ((max = createMethod(pc, constr, _keys, cw, real, component.get(keys[i]), max, writeLog, suppressWSbeforeArg, output, returnValue)) != -1) {
				break;// for overload remove this
			}
		}

		// Constructor
		GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_OBJECT, null, null, cw);
		adapter.loadThis();
		adapter.invokeConstructor(Types.OBJECT, CONSTRUCTOR_OBJECT);
		PageImpl.registerFields(new BytecodeContext(ThreadLocalPageContext.getConfig(pc), null, constr, getPage(constr), _keys, cw, real, adapter, CONSTRUCTOR_OBJECT, writeLog,
				suppressWSbeforeArg, output, returnValue, 0), _keys);
		adapter.returnValue();
		adapter.endMethod();

		cw.visitEnd();
		byte[] barr = cw.toByteArray();

		try {
			ResourceUtil.touch(classFile);
			IOUtil.copy(new ByteArrayInputStream(barr), classFile, true);

			cl = ((PageContextImpl) pc).getRPCClassLoader(true);

			return registerTypeMapping(((ExtendableClassLoader) cl).loadClass(className, barr));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	private static PageImpl getPage(BytecodeContext bc2) {
		PageImpl page = null;
		// if(bc1!=null)page=bc1.getPage();
		if (bc2 != null) page = (PageImpl) bc2.getPage();
		return page;
	}

	/**
	 * check if one of the children is changed
	 * 
	 * @param component
	 * @param clazz
	 * @return return true if children has changed
	 */
	private static boolean hasChangesOfChildren(long last, Class clazz) {
		return hasChangesOfChildren(last, ThreadLocalPageContext.get(), clazz);
	}

	/**
	 * check if one of the children is changed
	 * 
	 * @param component
	 * @param pc
	 * @param clazz
	 * @return return true if children has changed
	 */
	private static boolean hasChangesOfChildren(long last, PageContext pc, Class clazz) {

		java.lang.reflect.Method[] methods = clazz.getMethods();
		java.lang.reflect.Method method;
		Class[] params;
		for (int i = 0; i < methods.length; i++) {
			method = methods[i];
			if (method.getDeclaringClass() == clazz) {
				if (_hasChangesOfChildren(pc, last, method.getReturnType())) return true;
				params = method.getParameterTypes();
				for (int y = 0; y < params.length; y++) {
					if (_hasChangesOfChildren(pc, last, params[y])) return true;
				}
			}
		}
		return false;
	}

	private static boolean _hasChangesOfChildren(PageContext pc, long last, Class clazz) {
		clazz = ClassUtil.toComponentType(clazz);
		java.lang.reflect.Method m = getComplexTypeMethod(clazz);
		if (m == null) return false;
		try {
			String path = Caster.toString(m.invoke(null, new Object[0]));
			Resource res = ResourceUtil.toResourceExisting(pc, path);
			if (last < res.lastModified()) {
				return true;
			}
		}
		catch (Exception e) {
			return true;
		}
		// possible that a child of the Cmplex Object is also a complex object
		return hasChangesOfChildren(last, pc, clazz);
	}

	private static boolean isComplexType(Class clazz) {
		return getComplexTypeMethod(clazz) != null;

	}

	private static java.lang.reflect.Method getComplexTypeMethod(Class clazz) {
		try {
			return clazz.getMethod("_srcName", new Class[0]);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * search in methods of a class for complex types
	 * 
	 * @param clazz
	 * @return
	 * @throws PageException
	 */
	private static Class registerTypeMapping(Class clazz) throws PageException {
		PageContext pc = ThreadLocalPageContext.get();
		WSServer server = ((ConfigWebPro) ThreadLocalPageContext.getConfig(pc)).getWSHandler().getWSServer(pc);
		return registerTypeMapping(server, clazz);
	}

	/**
	 * search in methods of a class for complex types
	 * 
	 * @param server
	 * @param clazz
	 * @return
	 */
	private static Class registerTypeMapping(WSServer server, Class clazz) {
		java.lang.reflect.Method[] methods = clazz.getMethods();
		java.lang.reflect.Method method;
		Class[] params;
		for (int i = 0; i < methods.length; i++) {
			method = methods[i];
			if (method.getDeclaringClass() == clazz) {
				_registerTypeMapping(server, method.getReturnType());
				params = method.getParameterTypes();
				for (int y = 0; y < params.length; y++) {
					_registerTypeMapping(server, params[y]);
				}
			}
		}
		return clazz;
	}

	/**
	 * register ComplexType
	 * 
	 * @param server
	 * @param clazz
	 */
	private static void _registerTypeMapping(WSServer server, Class clazz) {
		if (clazz == null) return;

		if (!isComplexType(clazz)) {
			if (clazz.isArray()) {
				_registerTypeMapping(server, clazz.getComponentType());
			}
			return;
		}
		server.registerTypeMapping(clazz);
		registerTypeMapping(server, clazz);
	}

	public static String getClassname(Component component, ASMProperty[] props) {

		String prefix = "";
		/*
		 * if(props!=null) { StringBuilder sb=new StringBuilder();
		 * 
		 * for(int i=0;i<props.length;i++){ sb.append(props[i].toString()).append(';'); }
		 * 
		 * 
		 * prefix = Long.toString(HashUtil.create64BitHash(sb),Character.MAX_RADIX); char
		 * c=prefix.charAt(0); if(c>='0' && c<='9') prefix="a"+prefix; prefix=prefix+"."; }
		 */

		PageSource ps = component.getPageSource();
		return prefix + ps.getComponentName();
	}

	/*
	 * includes the application context javasettings
	 * 
	 * @param pc
	 * 
	 * @param className
	 * 
	 * @param properties
	 * 
	 * @return
	 * 
	 * @throws PageException
	 */
	// TO NOT DELETE, USED IN AXIS
	public static Class getClientComponentPropertiesClass(PageContext pc, String className, ASMProperty[] properties, Class extendsClass) throws PageException {
		try {
			return _getComponentPropertiesClass(pc, pc.getConfig(), className, properties, extendsClass, true);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/*
	 * does not include the application context javasettings
	 * 
	 * @param pc
	 * 
	 * @param className
	 * 
	 * @param properties
	 * 
	 * @return
	 * 
	 * @throws PageException
	 */
	// TO NOT DELETE, USED IN AXIS
	public static Class getComponentPropertiesClass(Config config, String className, ASMProperty[] properties, Class extendsClass) throws PageException {
		try {
			return _getComponentPropertiesClass(null, config, className, properties, extendsClass, true);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static Class _getComponentPropertiesClass(PageContext pc, Config secondChanceConfig, String className, ASMProperty[] properties, Class extendsClass, boolean axisType)
			throws PageException, IOException, ClassNotFoundException {
		String real = className.replace('.', '/');

		ClassLoader cl;
		if (pc == null) cl = secondChanceConfig.getRPCClassLoader(false);
		else cl = ((PageContextImpl) pc).getRPCClassLoader();

		Resource rootDir = ((DirectoryProvider) cl).getDirectory();
		Resource classFile = rootDir.getRealResource(real.concat(".class"));

		if (classFile.exists()) {
			try {
				Class clazz = cl.loadClass(className);
				Field field = clazz.getField("_md5_");
				if (ASMUtil.createMD5(properties).equals(field.get(null))) {
					// if(equalInterface(properties,clazz)) {
					return clazz;
				}
			}
			catch (Exception e) {

			}
		}
		// create file
		if (extendsClass == null) extendsClass = Object.class;
		byte[] barr = ASMUtil.createPojo(real, properties, extendsClass, new Class[] { Pojo.class }, null, axisType);
		boolean exist = classFile.exists();
		ResourceUtil.touch(classFile);
		IOUtil.copy(new ByteArrayInputStream(barr), classFile, true);

		if (pc == null) cl = secondChanceConfig.getRPCClassLoader(exist);
		else cl = ((PageContextImpl) pc).getRPCClassLoader(exist);

		return cl.loadClass(className);

	}

	// TO NOT DELETE, USED IN AXIS
	public static Class getComponentPropertiesClass(PageContext pc, Component component) throws PageException {
		try {
			return _getComponentPropertiesClass(pc, null, component, true);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public static Class getComponentPropertiesClass(PageContext pc, ClassLoader cl, Component component, boolean axisType) throws PageException {
		try {
			return _getComponentPropertiesClass(pc, cl, component, axisType);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static Class _getComponentPropertiesClass(PageContext pc, ClassLoader cl, Component component, boolean axisType)
			throws PageException, IOException, ClassNotFoundException {
		ASMProperty[] props = ASMUtil.toASMProperties(component.getProperties(false, true, false, false));

		final String className = getClassname(component, props);
		String real = className.replace('.', '/');

		Mapping mapping = component.getPageSource().getMapping();
		ClassLoader rpc = ((PageContextImpl) pc).getRPCClassLoader();

		Resource classFile = ((DirectoryProvider) rpc).getDirectory().getRealResource(real.concat(".class"));
		// get component class information
		String classNameOriginal = component.getPageSource().getClassName();
		String realOriginal = classNameOriginal.replace('.', '/');
		Resource classFileOriginal = mapping.getClassRootDirectory().getRealResource(realOriginal.concat(".class"));

		long classFileLastModified = classFile.lastModified();
		// load existing class when pojo is still newer than component class file
		if (classFileLastModified >= classFileOriginal.lastModified()) {
			try {
				Class clazz = null;
				if (cl != null) clazz = ClassUtil.loadClass(cl, className, null);
				if (clazz == null) clazz = rpc.loadClass(className);
				if (clazz != null && !hasChangesOfChildren(classFile.lastModified(), clazz)) return clazz;// ClassUtil.loadInstance(clazz);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		// extends
		String strExt = component.getExtends();
		Class<?> ext = Object.class;
		if (!StringUtil.isEmpty(strExt, true)) {
			ext = Caster.cfTypeToClass(pc, cl, strExt);
		}
		//
		// create file
		byte[] barr = ASMUtil.createPojo(real, props, ext, new Class[] { Pojo.class }, component.getPageSource().getDisplayPath(), axisType);
		ResourceUtil.touch(classFile);
		IOUtil.copy(new ByteArrayInputStream(barr), classFile, true);

		Class clazz = null;
		if (cl != null) {
			clazz = ClassUtil.loadClass(cl, className, null);
			if (clazz != null) return clazz;
		}
		cl = ((PageContextImpl) pc).getRPCClassLoader(classFileLastModified > 0);
		return cl.loadClass(className); // ClassUtil.loadInstance(cl.loadClass(className));
	}

	public static Class getStructPropertiesClass(PageContext pc, Struct sct, PhysicalClassLoader cl) throws PageException {
		try {
			return _getStructPropertiesClass(pc, sct, cl, true);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public static Class getStructPropertiesClass(PageContext pc, Struct sct, PhysicalClassLoader cl, boolean axisType) throws PageException {
		try {
			return _getStructPropertiesClass(pc, sct, cl, axisType);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static Class _getStructPropertiesClass(PageContext pc, Struct sct, PhysicalClassLoader cl, boolean axisType) throws PageException, IOException, ClassNotFoundException {
		// create hash based on the keys of the struct
		String hash = StructUtil.keyHash(sct);
		char c = hash.charAt(0);
		if (c >= '0' && c <= '9') hash = "a" + hash;

		// create class name (struct class name + hash)
		String className = sct.getClass().getName() + "." + hash;

		// create physcal location for the file
		String real = className.replace('.', '/');
		Resource classFile = cl.getDirectory().getRealResource(real.concat(".class"));

		// load existing class
		if (classFile.exists()) {
			try {
				Class clazz = cl.loadClass(className);
				if (clazz != null) return clazz;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		// Properties
		List<ASMProperty> props = new ArrayList<ASMProperty>();
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			props.add(new ASMPropertyImpl(ASMUtil.toType(e.getValue() == null ? Object.class : Object.class/* e.getValue().getClass() */, axisType), e.getKey().getString()));
		}

		// create file
		byte[] barr = ASMUtil.createPojo(real, props.toArray(new ASMProperty[props.size()]), Object.class, new Class[] { Pojo.class }, null, axisType);

		// create class file from bytecode
		ResourceUtil.touch(classFile);
		IOUtil.copy(new ByteArrayInputStream(barr), classFile, true);
		cl = (PhysicalClassLoader) ((PageContextImpl) pc).getRPCClassLoader(true);
		return cl.loadClass(className);
	}

	private static int createMethod(PageContext pc, ConstrBytecodeContext constr, Map<LitString, Integer> keys, ClassWriter cw, String className, Object member, int max,
			boolean writeLog, boolean suppressWSbeforeArg, boolean output, boolean returnValue) throws PageException {

		boolean hasOptionalArgs = false;

		if (member instanceof UDF) {
			UDF udf = (UDF) member;
			FunctionArgument[] args = udf.getFunctionArguments();
			Type[] types = new Type[max < 0 ? args.length : max];
			for (int y = 0; y < types.length; y++) {
				types[y] = toType(pc, args[y].getTypeAsString(), true);
				if (!args[y].isRequired()) hasOptionalArgs = true;
			}
			Type rtnType = toType(pc, udf.getReturnTypeAsString(), true);
			Method method = new Method(udf.getFunctionName(), rtnType, types);
			GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, method, null, null, cw);
			BytecodeContext bc = new BytecodeContext(ThreadLocalPageContext.getConfig(pc), null, constr, getPage(constr), keys, cw, className, adapter, method, writeLog,
					suppressWSbeforeArg, output, returnValue, 0);
			Label start = adapter.newLabel();
			adapter.visitLabel(start);

			// ComponentController.invoke(name, args);
			// name
			adapter.push(udf.getFunctionName());

			// args
			ArrayVisitor av = new ArrayVisitor();
			av.visitBegin(adapter, Types.OBJECT, types.length);
			for (int y = 0; y < types.length; y++) {
				av.visitBeginItem(adapter, y);
				adapter.loadArg(y);
				av.visitEndItem(bc.getAdapter());
			}
			av.visitEnd();
			adapter.invokeStatic(SERVER_WSUTIL, INVOKE);
			adapter.checkCast(rtnType);

			// ASMConstants.NULL(adapter);
			adapter.returnValue();
			Label end = adapter.newLabel();
			adapter.visitLabel(end);

			for (int y = 0; y < types.length; y++) {
				adapter.visitLocalVariable(args[y].getName().getString(), types[y].getDescriptor(), null, start, end, y + 1);
			}
			adapter.endMethod();

			if (hasOptionalArgs) {
				if (max == -1) max = args.length - 1;
				else max--;
				return max;
			}
		}
		return -1;
	}

	private static Type toType(PageContext pc, String cfType, boolean axistype) throws PageException {
		Class clazz;
		try {
			clazz = Caster.cfTypeToClass(pc, cfType);
		}
		catch (ClassException e) {
			throw Caster.toPageException(e);
		}
		if (axistype) clazz = ((ConfigWebPro) ThreadLocalPageContext.getConfig()).getWSHandler().toWSTypeClass(clazz);
		return Type.getType(clazz);

	}

	public static String md5(Component c) throws IOException {
		return md5(ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, c));
	}

	public static String md5(ComponentSpecificAccess cw) throws IOException {
		Key[] keys = cw.keys();
		Arrays.sort(keys);

		StringBuilder _interface = new StringBuilder();

		Object member;
		UDF udf;
		FunctionArgument[] args;
		FunctionArgument arg;
		for (int y = 0; y < keys.length; y++) {
			member = cw.get(keys[y], null);
			if (member instanceof UDF) {
				udf = (UDF) member;
				// print.out(udf.);
				_interface.append(udf.getAccess());
				_interface.append(udf.getOutput());
				_interface.append(udf.getFunctionName());
				_interface.append(udf.getReturnTypeAsString());
				args = udf.getFunctionArguments();
				for (int i = 0; i < args.length; i++) {
					arg = args[i];
					_interface.append(arg.isRequired());
					_interface.append(arg.getName());
					_interface.append(arg.getTypeAsString());
				}
			}
		}
		return MD5.getDigestAsString(_interface.toString().toLowerCase());
	}

	/**
	 * cast a strong access definition to the int type
	 * 
	 * @param access access type
	 * @return int access type
	 * @throws ApplicationException
	 */
	public static int toIntAccess(String access) throws ApplicationException {
		access = StringUtil.toLowerCase(access.trim());
		if (access.equals("package")) return Component.ACCESS_PACKAGE;
		else if (access.equals("private")) return Component.ACCESS_PRIVATE;
		else if (access.equals("public")) return Component.ACCESS_PUBLIC;
		else if (access.equals("remote")) return Component.ACCESS_REMOTE;
		throw new ApplicationException("Invalid function access type [" + access + "], access types are [remote, public, package, private]");

	}

	public static int toIntAccess(String access, int defaultValue) {
		access = StringUtil.toLowerCase(access.trim());
		if (access.equals("package")) return Component.ACCESS_PACKAGE;
		else if (access.equals("private")) return Component.ACCESS_PRIVATE;
		else if (access.equals("public")) return Component.ACCESS_PUBLIC;
		else if (access.equals("remote")) return Component.ACCESS_REMOTE;
		return defaultValue;
	}

	/**
	 * cast int type to string type
	 * 
	 * @param access
	 * @return String access type
	 * @throws ApplicationException
	 */
	public static String toStringAccess(int access) throws ApplicationException {
		String res = toStringAccess(access, null);
		if (res != null) return res;
		throw new ApplicationException("Invalid function access type [" + access
				+ "], access types are [Component.ACCESS_PACKAGE, Component.ACCESS_PRIVATE, Component.ACCESS_PUBLIC, Component.ACCESS_REMOTE]");
	}

	public static String toStringAccess(int access, String defaultValue) {
		switch (access) {
		case Component.ACCESS_PACKAGE:
			return "package";
		case Component.ACCESS_PRIVATE:
			return "private";
		case Component.ACCESS_PUBLIC:
			return "public";
		case Component.ACCESS_REMOTE:
			return "remote";
		}
		return defaultValue;
	}

	public static ExpressionException notFunction(Component c, Collection.Key key, Object member, int access) {
		if (member == null) {
			String strAccess = toStringAccess(access, "");

			Collection.Key[] other = c.keys(access);

			if (other.length == 0) return new ExpressionException("Component [" + c.getCallName() + "] has no " + strAccess + " function with name [" + key + "]");

			return new ExpressionException("Component [" + c.getCallName() + "] has no " + strAccess + " function with name [" + key + "]",
					"Accessible functions are [" + ListUtil.arrayToList(other, ", ") + "]");
		}
		return new ExpressionException("Member [" + key + "] of component [" + c.getCallName() + "] is not a function", "Member is of type [" + Caster.toTypeName(member) + "]");
	}

	public static Property[] getProperties(Component c, boolean onlyPeristent, boolean includeBaseProperties, boolean preferBaseProperties, boolean inheritedMappedSuperClassOnly) {
		return c.getProperties(onlyPeristent, includeBaseProperties, preferBaseProperties, preferBaseProperties);
	}

	/*
	 * public static ComponentAccess toComponentAccess(Component comp) throws ExpressionException {
	 * ComponentAccess ca = toComponentAccess(comp, null); if(ca!=null) return ca; throw new
	 * ExpressionException("can't cast class ["+Caster.toClassName(comp)
	 * +"] to a class of type ComponentAccess"); }
	 */

	/*
	 * public static Component toComponentAccess(Component comp, Component defaultValue) { if(comp
	 * instanceof ComponentAccess) return (ComponentAccess) comp; if(comp instanceof
	 * ComponentSpecificAccess) return ((ComponentSpecificAccess) comp).getComponentAccess(); return
	 * defaultValue; }
	 */

	public static Component toComponent(Object obj) throws ExpressionException {
		if (obj instanceof Component) return (Component) obj;
		throw new ExpressionException("Can't cast class [" + Caster.toClassName(obj) + "] to a class of type [Component]");
	}

	public static PageSource getPageSource(Component cfc) {
		// TODO Auto-generated method stub
		try {
			return toComponent(cfc).getPageSource();
		}
		catch (ExpressionException e) {
			return null;
		}
	}

	public static Component getCurrentComponent(PageContext pc, Component current) {

		// where are we (getCurrentPageSource) and use the matching component
		PageSource currPS = pc.getCurrentPageSource(null);
		if (currPS != null) {

			// current component
			Component curr = current;
			while (curr != null) {
				if (currPS.equals(((ComponentImpl) curr)._getPageSource())) {
					return curr;
				}
				curr = curr.getBaseComponent();
			}

			// active component
			curr = pc.getActiveComponent();
			while (curr != null) {
				if (currPS.equals(((ComponentImpl) curr)._getPageSource())) {
					return curr;
				}
				curr = curr.getBaseComponent();
			}

			// owner component
			if (pc.getActiveUDF() != null) {
				curr = pc.getActiveUDF().getOwnerComponent();
				while (curr != null) {
					if (currPS.equals(((ComponentImpl) curr)._getPageSource())) {
						return curr;
					}
					curr = curr.getBaseComponent();
				}
			}
		}

		if (pc.getActiveComponent() != null) return pc.getActiveComponent();
		return current;
	}

	public static long getCompileTime(PageContext pc, PageSource ps, long defaultValue) {
		try {
			return getCompileTime(pc, ps);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static long getCompileTime(PageContext pc, PageSource ps) throws PageException {
		return getPage(pc, ps).getCompileTime();
	}

	public static Page getPage(PageContext pc, PageSource ps) throws PageException {
		PageSourceImpl psi = (PageSourceImpl) ps;

		Page p = psi.getPage();
		if (p != null) {
			// print.o("getPage(existing):"+ps.getDisplayPath()+":"+psi.hashCode()+":"+p.hashCode());
			return p;
		}
		pc = ThreadLocalPageContext.get(pc);
		return psi.loadPage(pc, false);
	}

	public static Struct getPropertiesAsStruct(Component c, boolean onlyPersistent) {
		Property[] props = c.getProperties(onlyPersistent, false, false, false);
		Struct sct = new StructImpl();
		if (props != null) for (int i = 0; i < props.length; i++) {
			sct.setEL(KeyImpl.init(props[i].getName()), props[i]);
		}
		return sct;
	}

	public static Struct getMetaData(PageContext pc, UDFPropertiesBase udf) throws PageException {
		return getMetaData(pc, udf, false);
	}

	public static Struct getMetaData(PageContext pc, UDFPropertiesBase udf, Boolean isStatic) throws PageException {
		return getMetaData(pc, null, udf, isStatic);
	}

	public static Struct getMetaData(PageContext pc, UDF udf, UDFPropertiesBase udfProps, Boolean isStatic) throws PageException {
		StructImpl func = new StructImpl();
		pc = ThreadLocalPageContext.get(pc);
		// TODO func.set("roles", value);
		// TODO func.set("userMetadata", value); neo unterstuetzt irgendwelche a
		// meta data
		Struct meta = udfProps.getMeta();
		if (meta != null) StructUtil.copy(meta, func, true);

		boolean isJava = false;
		boolean isJavaLambda = false;
		String lavaLambda = null;

		if (udf != null) {
			isJava = udf instanceof lucee.runtime.JF;
			Class<? extends UDF> clazz = udf.getClass();

			Class<?>[] interfaces = clazz.getInterfaces();
			if (interfaces != null) {
				for (Class<?> interf: interfaces) {
					if (interf.getName().startsWith("java.util.function.")) {
						isJavaLambda = true;
						lavaLambda = interf.getName();
					}
				}
			}
		}
		if (isJavaLambda) func.setEL("javaLambdaInterface", lavaLambda);
		func.setEL(KeyConstants._java, (isJava ? Boolean.TRUE : Boolean.FALSE));
		func.setEL(KeyConstants._closure, Boolean.FALSE);

		func.set(KeyConstants._access, ComponentUtil.toStringAccess(udfProps.getAccess()));
		String hint = udfProps.getHint();
		if (!StringUtil.isEmpty(hint)) func.set(KeyConstants._hint, hint);
		String displayname = udfProps.getDisplayName();
		if (!StringUtil.isEmpty(displayname)) func.set(KeyConstants._displayname, displayname);
		func.set(KeyConstants._name, udfProps.getFunctionName());
		func.set(KeyConstants._output, Caster.toBoolean(udfProps.getOutput()));
		func.set(KeyConstants._returntype, udfProps.getReturnTypeAsString());
		func.set(KeyConstants._modifier, udfProps.getModifier() == Component.MODIFIER_NONE ? "" : ComponentUtil.toModifier(udfProps.getModifier(), ""));
		func.set(KeyConstants._description, udfProps.getDescription());
		if (isStatic != null) func.set(KeyConstants._static, isStatic);

		if (udfProps.getLocalMode() != null) func.set("localMode", AppListenerUtil.toLocalMode(udfProps.getLocalMode().intValue(), ""));

		if (udf instanceof UDFGSProperty) {
			UDFGSProperty gsProp = (UDFGSProperty) udf;
			PageSource ps = null;

			// LDEV-3335: For inherited accessors, use the property's original owner
			Property prop = gsProp.getProperty();
			if (prop instanceof PropertyImpl) {
				ps = ((PropertyImpl) prop).getOwnerPageSource();
			}

			// Fallback to the UDF's PageSource if property owner not available
			if (ps == null) {
				ps = gsProp.getPageSource();
			}

			if (ps != null) {
				func.set(KeyConstants._owner, ps.getDisplayPath());
			}
		}
		else if (udfProps.getPageSource() != null) {
			func.set(KeyConstants._owner, udfProps.getPageSource().getDisplayPath());
		}

		if (udfProps.getStartLine() > 0 && udfProps.getEndLine() > 0) {
			Struct pos = new StructImpl();
			pos.set(KeyConstants._start, udfProps.getStartLine());
			pos.set(KeyConstants._end, udfProps.getEndLine());
			func.setEL(KeyConstants._position, pos);
		}

		int format = udfProps.getReturnFormat();
		if (format == UDF.RETURN_FORMAT_JSON) func.set(KeyConstants._returnFormat, "json");
		else if (format == UDF.RETURN_FORMAT_PLAIN) func.set(KeyConstants._returnFormat, "plain");
		else if (format == UDF.RETURN_FORMAT_WDDX) func.set(KeyConstants._returnFormat, "wddx");
		else if (format == UDF.RETURN_FORMAT_SERIALIZE) func.set(KeyConstants._returnFormat, "cfml");
		else if (format == UDF.RETURN_FORMAT_XML) func.set(KeyConstants._returnFormat, "xml");
		else func.set(KeyConstants._returnFormat, new ReturnFormatValue()); // we use this reference because this value data get cached and can change independent of the cache

		FunctionArgument[] args = udfProps.getFunctionArguments();
		Array params = new ArrayImpl();
		// Object defaultValue;
		Struct m;
		// Object defaultValue;
		for (int y = 0; y < args.length; y++) {
			StructImpl param = new StructImpl();
			param.set(KeyConstants._name, args[y].getName().getString());
			param.set(KeyConstants._required, Caster.toBoolean(args[y].isRequired()));
			param.set(KeyConstants._type, args[y].getTypeAsString());
			displayname = args[y].getDisplayName();
			if (!StringUtil.isEmpty(displayname)) param.set(KeyConstants._displayname, displayname);

			int defType = args[y].getDefaultType();
			if (defType == FunctionArgument.DEFAULT_TYPE_RUNTIME_EXPRESSION) {
				param.set(KeyConstants._default, "[runtime expression]");
			}
			else if (defType == FunctionArgument.DEFAULT_TYPE_LITERAL) {
				Page p = udfProps.getPage(pc);
				param.set(KeyConstants._default, p.udfDefaultValue(pc, udfProps.getIndex(), y, null));
			}

			hint = args[y].getHint();
			if (!StringUtil.isEmpty(hint)) param.set(KeyConstants._hint, hint);
			// TODO func.set("userMetadata", value); neo unterstuetzt irgendwelche attr, die dann hier
			// ausgebenen werden bloedsinn

			// meta data
			m = args[y].getMetaData();
			if (m != null) StructUtil.copy(m, param, true);

			params.append(param);
		}
		func.set(KeyConstants._parameters, params);
		return func;
	}

	public static int toModifier(String str, int emptyValue, int defaultValue) {
		if (StringUtil.isEmpty(str, true)) return emptyValue;
		str = str.trim();
		if ("abstract".equalsIgnoreCase(str)) return Component.MODIFIER_ABSTRACT;
		if ("final".equalsIgnoreCase(str)) return Component.MODIFIER_FINAL;
		if ("none".equalsIgnoreCase(str)) return Component.MODIFIER_NONE;
		return defaultValue;
	}

	public static String toModifier(int modifier, String defaultValue) {
		if (Component.MODIFIER_ABSTRACT == modifier) return "abstract";
		if (Component.MODIFIER_FINAL == modifier) return "final";
		if (Component.MODIFIER_NONE == modifier) return "none";

		return defaultValue;
	}

	public static void add(Map<String, ImportDefintion> map, ImportDefintion[] importDefintions) {
		if (importDefintions != null) {
			for (ImportDefintion id: importDefintions) {
				map.put(id.toString(), id);
			}
		}
	}

	public static java.util.Collection<UDF> toUDFs(java.util.Collection<UDFB> udfbs, boolean onlyUnused) {
		List<UDF> list = new ArrayList<UDF>();
		Iterator<UDFB> it = udfbs.iterator();
		UDFB udfb;
		while (it.hasNext()) {
			udfb = it.next();
			if (!onlyUnused || !udfb.used) list.add(udfb.udf);
		}
		return list;
	}

	private static class ReturnFormatValue implements Castable, SimpleValue, CharSequence, Dumpable {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Boolean castToBoolean(Boolean defaultValue) {
			return Caster.toBoolean(getReturnFormat(ThreadLocalPageContext.get()), defaultValue);

		}

		@Override
		public boolean castToBooleanValue() throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return Caster.toBooleanValue(getReturnFormat(pc));
		}

		@Override
		public DateTime castToDateTime() throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return Caster.toDatetime(getReturnFormat(pc), ThreadLocalPageContext.getTimeZone(pc));
		}

		@Override
		public DateTime castToDateTime(DateTime defaultValue) {
			PageContext pc = ThreadLocalPageContext.get();
			try {
				return Caster.toDatetime(getReturnFormat(pc), ThreadLocalPageContext.getTimeZone(pc));
			}
			catch (PageException e) {
				return defaultValue;
			}
		}

		@Override
		public double castToDoubleValue() throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return Caster.toDoubleValue(getReturnFormat(pc));
		}

		@Override
		public double castToDoubleValue(double defaultValue) {
			return Caster.toDoubleValue(getReturnFormat(ThreadLocalPageContext.get()), false, defaultValue);
		}

		@Override
		public String castToString() throws PageException {
			return getReturnFormat(ThreadLocalPageContext.get());
		}

		@Override
		public String castToString(String defaultValue) {
			return getReturnFormat(ThreadLocalPageContext.get());
		}

		@Override
		public int compareTo(String other) throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return OpUtil.compare(pc, getReturnFormat(pc), other);
		}

		@Override
		public int compareTo(boolean other) throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return OpUtil.compare(pc, getReturnFormat(pc), other);
		}

		@Override
		public int compareTo(double other) throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return OpUtil.compare(pc, getReturnFormat(pc), other);
		}

		@Override
		public int compareTo(DateTime other) throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return OpUtil.compare(pc, getReturnFormat(pc), (Date) other);
		}

		@Override
		public String toString() {
			PageContext pc = ThreadLocalPageContext.get();
			return getReturnFormat(pc);

		}

		private String getReturnFormat(PageContext pc) {
			if (pc != null) {
				ApplicationContext ac = pc.getApplicationContext();
				if (ac instanceof ApplicationContextSupport) {
					return UDFUtil.toReturnFormat(ac.getReturnFormat(), "wddx");
				}
			}
			Config c = ThreadLocalPageContext.getConfig();
			if (c instanceof ConfigPro) {
				return UDFUtil.toReturnFormat(((ConfigPro) c).getReturnFormat(), "wddx");
			}

			return "wddx";
		}

		@Override
		public int length() {
			return toString().length();
		}

		@Override
		public char charAt(int index) {
			return toString().charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return toString().subSequence(start, end);
		}

		@Override
		public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
			return DumpUtil.toDumpData(toString(), pageContext, maxlevel, properties);
		}
	}

	public static ClassLoader getClassLoader(PageContext pc, Component cfc, ClassLoader defaultValue) {
		if (cfc instanceof ComponentImpl) {
			try {
				JavaSettings js = ((ComponentImpl) cfc).getJavaSettings(pc);
				if (js != null) {
					return ((ConfigPro) pc.getConfig()).getRPCClassLoader(false, js);
				}
			}
			catch (IOException e) {
				return defaultValue;
			}

		}
		return defaultValue;
	}

	/**
	 * Optimized property registration that bypasses tag lifecycle overhead
	 *
	 * @param pc PageContext
	 * @param name property name
	 * @param type property type
	 * @param defaultValue default value
	 * @param access access level
	 * @param hint property hint
	 * @param displayname display name
	 * @param required is required
	 * @param setter has setter
	 * @param getter has getter
	 * @throws PageException
	 */
	public static void registerProperty(PageContext pc, String name, String type, Object defaultValue, String access, String hint, String displayname, boolean required,
			boolean setter, boolean getter) throws PageException {
		registerProperty(pc, name, type, defaultValue, access, hint, displayname, required, setter, getter, null);
	}

	public static void registerProperty(PageContext pc, String name, String type, Object defaultValue, String access, String hint, String displayname, boolean required,
			boolean setter, boolean getter, Struct dynamicAttributes) throws PageException {
		if (pc.variablesScope() instanceof ComponentScope) {
			Component comp = ((ComponentScope) pc.variablesScope()).getComponent();
			PropertyImpl property = new PropertyImpl();

			property.setName(name);
			if (type != null) property.setType(type);
			if (defaultValue != null) property.setDefault(defaultValue);
			if (access != null) property.setAccess(access);
			if (hint != null) property.setHint(hint);
			if (displayname != null) property.setDisplayname(displayname);
			property.setRequired(required);
			property.setSetter(setter);
			property.setGetter(getter);

			// Handle dynamic attributes
			if (dynamicAttributes != null) {
				StructUtil.copy(dynamicAttributes, property.getDynamicAttributes(), true);
			}

			// LDEV-3335: Set owner BEFORE setProperty() so inherited properties have the correct owner
			// Only set owner for properties defined in this component, not inherited ones
			// If already set (inherited from parent), don't overwrite
			if (property.getOwnerPageSource() == null) {
				property.setOwnerName(comp.getAbsName(), comp.getPageSource());
			}

			comp.setProperty(property);
		}
	}

}
