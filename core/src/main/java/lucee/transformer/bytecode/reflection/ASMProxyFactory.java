/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.transformer.bytecode.reflection;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExtendableClassLoader;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.bytecode.util.ASMConstants;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.Types;


public class ASMProxyFactory {

	public static final Type ASM_METHOD=Type.getType(ASMMethod.class);
	public static final Type CLASS404=Type.getType(ClassNotFoundException.class);
	
	
	
	//private static final org.objectweb.asm.commons.Method CONSTRUCTOR = 
    //	new org.objectweb.asm.commons.Method("<init>",Types.VOID,new Type[]{Types.CLASS_LOADER,Types.CLASS});
	private static final org.objectweb.asm.commons.Method CONSTRUCTOR = 
    	new org.objectweb.asm.commons.Method("<init>",Types.VOID,new Type[]{Types.CLASS,Types.CLASS_ARRAY});

	private static final org.objectweb.asm.commons.Method LOAD_CLASS = new org.objectweb.asm.commons.Method(
			"loadClass",
			Types.CLASS,
			new Type[]{Types.STRING});
	

	// public static Class loadClass(String className, Class defaultValue) {
	private static final org.objectweb.asm.commons.Method LOAD_CLASS_EL = new org.objectweb.asm.commons.Method(
			"loadClass",
			Types.CLASS,
			new Type[]{Types.STRING,Types.CLASS});
	

	// public String getName();
	private static final org.objectweb.asm.commons.Method GET_NAME = new org.objectweb.asm.commons.Method(
			"getName",
			Types.STRING,
			new Type[]{});
	
	// public int getModifiers();
	private static final org.objectweb.asm.commons.Method GET_MODIFIERS = new org.objectweb.asm.commons.Method(
			"getModifiers",
			Types.INT_VALUE,
			new Type[]{});

	// public Class getReturnType();
	private static final org.objectweb.asm.commons.Method GET_RETURN_TYPE_AS_STRING = new org.objectweb.asm.commons.Method(
			"getReturnTypeAsString",
			Types.STRING,
			new Type[]{});

	
	
	
	private static final org.objectweb.asm.commons.Method INVOKE = new org.objectweb.asm.commons.Method(
			"invoke",
			Types.OBJECT,
			new Type[]{Types.OBJECT,Types.OBJECT_ARRAY});
	
	
	// primitive to reference type
	private static final org.objectweb.asm.commons.Method BOOL_VALUE_OF = new org.objectweb.asm.commons.Method("valueOf",Types.BOOLEAN,new Type[]{Types.BOOLEAN_VALUE});
	private static final org.objectweb.asm.commons.Method SHORT_VALUE_OF = new org.objectweb.asm.commons.Method("valueOf",Types.SHORT,new Type[]{Types.SHORT_VALUE});
	private static final org.objectweb.asm.commons.Method INT_VALUE_OF = new org.objectweb.asm.commons.Method("valueOf",Types.INTEGER,new Type[]{Types.INT_VALUE});
	private static final org.objectweb.asm.commons.Method LONG_VALUE_OF = new org.objectweb.asm.commons.Method("valueOf",Types.LONG,new Type[]{Types.LONG_VALUE});
	private static final org.objectweb.asm.commons.Method FLT_VALUE_OF = new org.objectweb.asm.commons.Method("valueOf",Types.FLOAT,new Type[]{Types.FLOAT_VALUE});
	private static final org.objectweb.asm.commons.Method DBL_VALUE_OF = new org.objectweb.asm.commons.Method("valueOf",Types.DOUBLE,new Type[]{Types.DOUBLE_VALUE});
	private static final org.objectweb.asm.commons.Method CHR_VALUE_OF = new org.objectweb.asm.commons.Method("valueOf",Types.CHARACTER,new Type[]{Types.CHARACTER});
	private static final org.objectweb.asm.commons.Method BYT_VALUE_OF = new org.objectweb.asm.commons.Method("valueOf",Types.BYTE,new Type[]{Types.BYTE_VALUE});
	
	// reference type to primitive
	private static final org.objectweb.asm.commons.Method BOOL_VALUE = new org.objectweb.asm.commons.Method("booleanValue",Types.BOOLEAN_VALUE,new Type[]{});
	private static final org.objectweb.asm.commons.Method SHORT_VALUE = new org.objectweb.asm.commons.Method("shortValue",Types.SHORT_VALUE,new Type[]{});
	private static final org.objectweb.asm.commons.Method INT_VALUE = new org.objectweb.asm.commons.Method("intValue",Types.INT_VALUE,new Type[]{});
	private static final org.objectweb.asm.commons.Method LONG_VALUE = new org.objectweb.asm.commons.Method("longValue",Types.LONG_VALUE,new Type[]{});
	private static final org.objectweb.asm.commons.Method FLT_VALUE = new org.objectweb.asm.commons.Method("floatValue",Types.FLOAT_VALUE,new Type[]{});
	private static final org.objectweb.asm.commons.Method DBL_VALUE = new org.objectweb.asm.commons.Method("doubleValue",Types.DOUBLE_VALUE,new Type[]{});
	private static final org.objectweb.asm.commons.Method CHR_VALUE = new org.objectweb.asm.commons.Method("charValue",Types.CHAR,new Type[]{});
	private static final org.objectweb.asm.commons.Method BYT_VALUE = new org.objectweb.asm.commons.Method("byteValue",Types.BYTE_VALUE,new Type[]{});
	
	private static final org.objectweb.asm.commons.Method ASM_METHOD_CONSTRUCTOR = new org.objectweb.asm.commons.Method(
			"<init>",
			Types.VOID,
			new Type[]{Types.CLASS,Types.CLASS_ARRAY}
    		);
	
	
	private static final Map<String, SoftReference<ASMMethod>> methods = new ConcurrentHashMap<String, SoftReference<ASMMethod>>();

	
	public static ASMClass getClass(ExtendableClassLoader pcl,Resource classRoot,Class clazz) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException, UnmodifiableClassException{
		Type type = Type.getType(clazz); 

	    // Fields
	    Field[] fields = clazz.getFields();
	    for(int i=0;i<fields.length;i++){
	    	if(Modifier.isPrivate(fields[i].getModifiers())) continue;
	    	createField(type,fields[i]);
	    }
	    
	    // Methods
	    Method[] methods = clazz.getMethods();
	    Map<String,ASMMethod> amethods=new HashMap<String, ASMMethod>();
	    for(int i=0;i<methods.length;i++){
	    	if(Modifier.isPrivate(methods[i].getModifiers())) continue;
	    	amethods.put(methods[i].getName(), getMethod(pcl,classRoot,type,clazz,methods[i]));
	    }
	    
	    return new ASMClass(clazz.getName(),amethods);
	    
	}
	
	private static void createField(Type type, Field field) {
		// TODO Auto-generated method stub
		
	}

	public static ASMMethod getMethod(ExtendableClassLoader pcl, Resource classRoot, Class clazz, String methodName, Class[] parameters) throws IOException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, UnmodifiableClassException {
		String className = createMethodName(clazz,methodName,parameters);
		
		// check if already in memory cache
		SoftReference<ASMMethod> tmp = methods.get(className);
		ASMMethod asmm = tmp == null ? null : tmp.get();
		if(asmm!=null){
			//print.e("use loaded from memory");
			return asmm;
		}
		
		// try to load existing ASM Class
		Class<?> asmClass;
		try {
			asmClass = pcl.loadClass(className);
			//print.e("use existing class");
		}
		catch (ClassNotFoundException cnfe) {
			Type type = Type.getType(clazz);
			Method method = clazz.getMethod(methodName, parameters);
			byte[] barr = _createMethod(type, clazz, method, classRoot, className);
			asmClass=pcl.loadClass(className, barr);
			//print.e("create class");
		}
		asmm = newInstance(asmClass,clazz,parameters);
		//methods.put(className, asmm);
		return asmm;
	}

	private static ASMMethod getMethod(ExtendableClassLoader pcl, Resource classRoot, Type type,Class clazz, Method method) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException, UnmodifiableClassException {
		String className = createMethodName(clazz,method.getName(),method.getParameterTypes());
		
		// check if already in memory cache
		SoftReference<ASMMethod> tmp = methods.get(className);
		ASMMethod asmm = tmp == null ? null : tmp.get();
		if(asmm!=null)return asmm;
		
		// try to load existing ASM Class
		Class<?> asmClass;
		try {
			asmClass = pcl.loadClass(className);
		}
		catch (ClassNotFoundException cnfe) {
			byte[] barr = _createMethod(type, clazz, method, classRoot, className);
			asmClass=pcl.loadClass(className, barr);
		}
		
		asmm = newInstance(asmClass,clazz,method.getParameterTypes());
		methods.put(className, new SoftReference<ASMMethod>(asmm));
		return asmm;
	}

	private static ASMMethod newInstance(Class<?> asmClass, Class<?> decClass, Class[] params) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
		Constructor<ASMMethod> constr = (Constructor<ASMMethod>) asmClass.getConstructor(
				new Class[]{
					Class.class,
					Class[].class
				}
		);
		return constr.newInstance(new Object[]{
				decClass,
				params
			});
		 
		//return (ASMMethod) asmClass.newInstance();
	}
	
	
	private static String createMethodName(Class clazz,String methodName,Class[] paramTypes) {
		StringBuilder sb = new StringBuilder("")
		.append(clazz.getName())
		.append('$')
		.append(methodName);
		
		paramNames(sb,paramTypes);
		
		return sb.toString();
	}

	private static byte[] _createMethod(Type type,Class clazz, Method method,Resource classRoot, String className) throws IOException {
		Class<?> rtn = method.getReturnType();
	    Type rtnType = Type.getType(rtn);
	    
		className=className.replace('.',File.separatorChar);
		ClassWriter cw = ASMUtil.getClassWriter();
	    cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, className, null, ASM_METHOD.getInternalName(), null);
		

// CONSTRUCTOR

	    GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC,CONSTRUCTOR,null,null,cw);
	    
	    Label begin = new Label();
        adapter.visitLabel(begin);
		adapter.loadThis();
        
	    adapter.visitVarInsn(Opcodes.ALOAD, 1);
		adapter.visitVarInsn(Opcodes.ALOAD, 2);
        
		adapter.invokeConstructor(ASM_METHOD, CONSTRUCTOR);
		adapter.visitInsn(Opcodes.RETURN);
		
		Label end = new Label();
		adapter.visitLabel(end);
		
		adapter.endMethod();
		
	/*
	 
	    GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC,CONSTRUCTOR,null,null,cw);
	    
	    Label begin = new Label();
        adapter.visitLabel(begin);
		adapter.loadThis();
        
	    // clazz
        adapter.visitVarInsn(Opcodes.ALOAD, 2);
        
        // parameterTypes 
        Class<?>[] params = method.getParameterTypes();
        Type[] paramTypes = new Type[params.length];
	    ArrayVisitor av=new ArrayVisitor();
	    av.visitBegin(adapter, Types.CLASS, params.length);
	    for(int i=0;i<params.length;i++){
	    	paramTypes[i]=Type.getType(params[i]);
	    	av.visitBeginItem(adapter, i);
	    		loadClass(adapter,params[i]);
	    	av.visitEndItem(adapter);
	    }
	    av.visitEnd();
	    
		adapter.invokeConstructor(ASM_METHOD, ASM_METHOD_CONSTRUCTOR);
		adapter.visitInsn(Opcodes.RETURN);
		
		Label end = new Label();
		adapter.visitLabel(end);
		
		adapter.endMethod();
	 */
		
		
		
	// METHOD getName();
		adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC , GET_NAME, null, null, cw);
		adapter.push(method.getName());
		adapter.visitInsn(Opcodes.ARETURN);
        adapter.endMethod();
		
    // METHOD getModifiers();
		adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC , GET_MODIFIERS, null, null, cw);
		adapter.push(method.getModifiers());
		adapter.visitInsn(Opcodes.IRETURN);
        adapter.endMethod();
        

		
    // METHOD getReturnType();
        adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC , GET_RETURN_TYPE_AS_STRING, null, null, cw);
		
		adapter.push(method.getReturnType().getName());
		adapter.visitInsn(Opcodes.ARETURN);
        
		adapter.endMethod();
        
		
        
        
	// METHOD INVOKE
		boolean isStatic = Modifier.isStatic(method.getModifiers());
		adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC , INVOKE, null, null, cw);
        Label start=adapter.newLabel();
        adapter.visitLabel(start);
        
        // load Object
        if(!isStatic) {
        	adapter.visitVarInsn(Opcodes.ALOAD, 1);
        	adapter.checkCast(type);
		}
        	
        // load params
        Class<?>[] params = method.getParameterTypes();

        Type[] paramTypes = new Type[params.length];
	    for(int i=0;i<params.length;i++){
	    	paramTypes[i]=Type.getType(params[i]);
	    }

        for(int i=0;i<params.length;i++){
        	adapter.visitVarInsn(Opcodes.ALOAD, 2);
        	adapter.push(i);
        	//adapter.visitInsn(Opcodes.ICONST_0);
        	adapter.visitInsn(Opcodes.AALOAD);
        	
        	adapter.checkCast(toReferenceType(params[i],paramTypes[i]));
        	
        	// cast
        	if(params[i]==boolean.class) adapter.invokeVirtual(Types.BOOLEAN, BOOL_VALUE);
        	else if(params[i]==short.class) adapter.invokeVirtual(Types.SHORT, SHORT_VALUE);
        	else if(params[i]==int.class) adapter.invokeVirtual(Types.INTEGER, INT_VALUE);
        	else if(params[i]==float.class) adapter.invokeVirtual(Types.FLOAT, FLT_VALUE);
        	else if(params[i]==long.class) adapter.invokeVirtual(Types.LONG, LONG_VALUE);
        	else if(params[i]==double.class) adapter.invokeVirtual(Types.DOUBLE, DBL_VALUE);
        	else if(params[i]==char.class) adapter.invokeVirtual(Types.CHARACTER, CHR_VALUE);
        	else if(params[i]==byte.class) adapter.invokeVirtual(Types.BYTE, BYT_VALUE);
        	//else adapter.checkCast(paramTypes[i]);
        	
        }
        
        
        // call method
    	final org.objectweb.asm.commons.Method m = new org.objectweb.asm.commons.Method(method.getName(),rtnType,paramTypes);
    	if(isStatic)adapter.invokeStatic(type, m);
    	else adapter.invokeVirtual(type, m);
         
    	
    	// return
    	if(rtn==void.class) ASMConstants.NULL(adapter);
    	
    	
    	// cast result to object
    	if(rtn==boolean.class) adapter.invokeStatic(Types.BOOLEAN, BOOL_VALUE_OF);
    	else if(rtn==short.class) adapter.invokeStatic(Types.SHORT, SHORT_VALUE_OF);
    	else if(rtn==int.class) adapter.invokeStatic(Types.INTEGER, INT_VALUE_OF);
    	else if(rtn==long.class) adapter.invokeStatic(Types.LONG, LONG_VALUE_OF);
    	else if(rtn==float.class) adapter.invokeStatic(Types.FLOAT, FLT_VALUE_OF);
    	else if(rtn==double.class) adapter.invokeStatic(Types.DOUBLE, DBL_VALUE_OF);
    	else if(rtn==char.class) adapter.invokeStatic(Types.CHARACTER, CHR_VALUE_OF);
    	else if(rtn==byte.class) adapter.invokeStatic(Types.BYTE, BYT_VALUE_OF);
    	
    	adapter.visitInsn(Opcodes.ARETURN);
        
        adapter.endMethod();
		
        
		
        if(classRoot!=null) {
        	Resource classFile=classRoot.getRealResource(className+".class");
        	return store(cw.toByteArray(),classFile);
        }
        return cw.toByteArray();
	}
	

	private static Type toReferenceType(Class<?> clazz, Type defaultValue) {
		if(int.class==clazz) return Types.INTEGER;
		else if(long.class==clazz) return Types.LONG;
		else if(char.class==clazz) return Types.CHARACTER;
		else if(byte.class==clazz) return Types.BYTE;
		else if(float.class==clazz) return Types.FLOAT;
		else if(double.class==clazz) return Types.DOUBLE;
		else if(boolean.class==clazz) return Types.BOOLEAN;
		else if(short.class==clazz) return Types.SHORT;
		return defaultValue;
	}

	private static void loadClass(GeneratorAdapter adapter, Class<?> clazz) {
		if(void.class==clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Void", "TYPE", "Ljava/lang/Class;");
		
		// primitive types
		else if(int.class==clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
		else if(long.class==clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
		else if(char.class==clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
		else if(byte.class==clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
		else if(float.class==clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
		else if(double.class==clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
		else if(boolean.class==clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
		else if(short.class==clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
		
		// TODO ref types
		
		else {
		    adapter.visitVarInsn(Opcodes.ALOAD, 1);
	        adapter.push(clazz.getName());
			adapter.invokeVirtual(Types.CLASS_LOADER,LOAD_CLASS );
		}
	}

	private static void paramNames(StringBuilder sb, Class<?>[] params) {
		if(ArrayUtil.isEmpty(params)) return;
		
		for(int i=0;i<params.length;i++){
			sb.append('$');
			if(params[i].isArray())
				sb.append(StringUtil.replace(Caster.toClassName(params[i]).replace('.', '_'),"[]","_arr",false));
			else
				sb.append(params[i].getName().replace('.', '_'));
		}
	}

	private static byte[] store(byte[] barr,Resource classFile) throws IOException {
		// create class file
        ResourceUtil.touch(classFile);
        //print.e(classFile);
        IOUtil.copy(new ByteArrayInputStream(barr), classFile,true);
       return barr;
	}
	/*private void store(ClassWriter cw) {
		// create class file
        byte[] barr = cw.toByteArray();
    	
        try {
        	ResourceUtil.touch(classFile);
	        IOUtil.copy(new ByteArrayInputStream(barr), classFile,true);
	        
	        cl = (PhysicalClassLoader) mapping.getConfig().getRPCClassLoader(true);
	        Class<?> clazz = cl.loadClass(className, barr);
	        return newInstance(clazz, config,cfc);
        }
        catch(Throwable t) {
        	throw Caster.toPageException(t);
        }
	}*/
	
}