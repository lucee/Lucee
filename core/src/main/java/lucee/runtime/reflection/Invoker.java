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
package lucee.runtime.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.pairs.ConstructorParameterPair;
import lucee.runtime.reflection.pairs.MethodParameterPair;
import lucee.runtime.type.ObjectWrap;

/**
 * To invoke an Object in different ways
 */
public final class Invoker {

	private static Method[] lastMethods;
	private static Class lastClass;

	/**
	 * @param clazz
	 * @param parameters
	 * @return new Instance
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Object newInstance(Class clazz, Object[] parameters)
			throws NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		ConstructorParameterPair pair = getConstructorParameterPairIgnoreCase(clazz, parameters);
		return pair.getConstructor().newInstance(pair.getParameters());
	}

	/**
	 * search the matching constructor to defined parameter list, also translate parameters for matching
	 * 
	 * @param clazz class to get constructo from
	 * @param parameters parameter for the constructor
	 * @return Constructor parameter pair
	 * @throws NoSuchMethodException
	 */
	public static ConstructorParameterPair getConstructorParameterPairIgnoreCase(Class clazz, Object[] parameters) throws NoSuchMethodException {
		// set all values
		// Class objectClass=object.getClass();
		if (parameters == null) parameters = new Object[0];

		// set parameter classes
		Class[] parameterClasses = new Class[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			parameterClasses[i] = parameters[i].getClass();
		}

		// search right method
		Constructor[] constructor = clazz.getConstructors();
		for (int mode = 0; mode < 2; mode++) {
			outer: for (int i = 0; i < constructor.length; i++) {
				Constructor c = constructor[i];

				Class[] paramTrg = c.getParameterTypes();
				// Same Parameter count
				if (parameterClasses.length == paramTrg.length) {
					for (int y = 0; y < parameterClasses.length; y++) {
						if (mode == 0 && parameterClasses[y] != primitiveToWrapperType(paramTrg[y])) {
							continue outer;
						}
						else if (mode == 1) {
							Object o = compareClasses(parameters[y], paramTrg[y]);
							if (o == null) continue outer;

							parameters[y] = o;
							parameterClasses[y] = o.getClass();

						}
					}
					return new ConstructorParameterPair(c, parameters);
				}

			}
		}

		// Exeception
		String parameter = "";
		for (int i = 0; i < parameterClasses.length; i++) {
			if (i != 0) parameter += ", ";
			parameter += parameterClasses[i].getName();
		}
		throw new NoSuchMethodException("class constructor " + clazz.getName() + "(" + parameter + ") doesn't exist");
	}

	/**
	 * call of a method from given object
	 * 
	 * @param object object to call method from
	 * @param methodName name of the method to call
	 * @param parameters parameter for method
	 * @return return value of the method
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Object callMethod(Object object, String methodName, Object[] parameters)
			throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		MethodParameterPair pair = getMethodParameterPairIgnoreCase(object.getClass(), methodName, parameters);

		return pair.getMethod().invoke(object, pair.getParameters());
	}

	/**
	 * search the matching method to defined Method Name, also translate parameters for matching
	 * 
	 * @param objectClass class object where searching method from
	 * @param methodName name of the method to search
	 * @param parameters whished parameter list
	 * @return pair with method matching and parameterlist matching
	 * @throws NoSuchMethodException
	 */
	public static MethodParameterPair getMethodParameterPairIgnoreCase(Class objectClass, String methodName, Object[] parameters) throws NoSuchMethodException {
		// set all values
		if (parameters == null) parameters = new Object[0];

		// set parameter classes
		Class[] parameterClasses = new Class[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			parameterClasses[i] = parameters[i].getClass();
		}

		// search right method
		Method[] methods = null;

		if (lastClass != null && lastClass.equals(objectClass)) {
			methods = lastMethods;
		}
		else {
			methods = objectClass.getDeclaredMethods();
		}

		lastClass = objectClass;
		lastMethods = methods;
		// Method[] methods=objectClass.getMethods();
		// Method[] methods=objectClass.getDeclaredMethods();

		// methods=objectClass.getDeclaredMethods();
		for (int mode = 0; mode < 2; mode++) {
			outer: for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				// Same Name
				if (method.getName().equalsIgnoreCase(methodName)) {
					Class[] paramTrg = method.getParameterTypes();
					// Same Parameter count
					if (parameterClasses.length == paramTrg.length) {
						// if(parameterClasses.length==0)return m;
						for (int y = 0; y < parameterClasses.length; y++) {

							if (mode == 0 && parameterClasses[y] != primitiveToWrapperType(paramTrg[y])) {
								continue outer;
							}
							else if (mode == 1) {
								Object o = compareClasses(parameters[y], paramTrg[y]);

								if (o == null) {
									continue outer;
								}
								parameters[y] = o;
								parameterClasses[y] = o.getClass();

							}
							// if(parameterClasses.length-1==y) return m;
						}

						return new MethodParameterPair(method, parameters);
					}
				}
			}
		}

		// Exeception
		String parameter = "";
		for (int i = 0; i < parameterClasses.length; i++) {
			if (i != 0) parameter += ", ";
			parameter += parameterClasses[i].getName();
		}
		throw new NoSuchMethodException("method " + methodName + "(" + parameter + ") doesn't exist in class " + objectClass.getName());

	}

	/**
	 * compare parameter with whished parameter class and convert parameter to whished type
	 * 
	 * @param parameter parameter to compare
	 * @param trgClass whished type of the parameter
	 * @return converted parameter (to whished type) or null
	 */
	private static Object compareClasses(Object parameter, Class trgClass) {
		Class srcClass = parameter.getClass();
		trgClass = primitiveToWrapperType(trgClass);
		try {
			if (parameter instanceof ObjectWrap) parameter = ((ObjectWrap) parameter).getEmbededObject();

			// parameter is already ok

			if (srcClass == trgClass) return parameter;

			else if (instaceOf(srcClass, trgClass)) {
				return parameter;
			}
			else if (trgClass.getName().equals("java.lang.String")) {
				return Caster.toString(parameter);
			}
			else if (trgClass.getName().equals("java.lang.Boolean")) {
				return Caster.toBoolean(parameter);
			}
			else if (trgClass.getName().equals("java.lang.Byte")) {
				return new Byte(Caster.toString(parameter));
			}
			else if (trgClass.getName().equals("java.lang.Character")) {
				String str = Caster.toString(parameter);
				if (str.length() == 1) return new Character(str.toCharArray()[0]);
				return null;
			}
			else if (trgClass.getName().equals("java.lang.Short")) {
				return Short.valueOf((short) Caster.toIntValue(parameter));
			}
			else if (trgClass.getName().equals("java.lang.Integer")) {
				return Integer.valueOf(Caster.toIntValue(parameter));
			}
			else if (trgClass.getName().equals("java.lang.Long")) {
				return Long.valueOf((long) Caster.toDoubleValue(parameter));
			}
			else if (trgClass.getName().equals("java.lang.Float")) {
				return Float.valueOf((float) Caster.toDoubleValue(parameter));
			}
			else if (trgClass.getName().equals("java.lang.Double")) {
				return Caster.toDouble(parameter);
			}
		}
		catch (PageException e) {
			return null;
		}

		return null;
	}

	/**
	 * @param srcClass
	 * @param trgClass
	 * @return is instance of or not
	 */
	private static boolean instaceOf(Class srcClass, Class trgClass) {
		while (srcClass != null) {
			if (srcClass == trgClass) return true;
			srcClass = primitiveToWrapperType(srcClass.getSuperclass());
		}
		return false;
	}

	/**
	 * cast a primitive type class definition to his object reference type
	 * 
	 * @param clazz class object to check and convert if it is of primitive type
	 * @return object reference class object
	 */
	private static Class primitiveToWrapperType(Class clazz) {
		// boolean, byte, char, short, int, long, float, and double
		if (clazz == null) return null;
		else if (clazz.isPrimitive()) {
			if (clazz.getName().equals("boolean")) return Boolean.class;
			else if (clazz.getName().equals("byte")) return Byte.class;
			else if (clazz.getName().equals("char")) return Character.class;
			else if (clazz.getName().equals("short")) return Short.class;
			else if (clazz.getName().equals("int")) return Integer.class;
			else if (clazz.getName().equals("long")) return Long.class;
			else if (clazz.getName().equals("float")) return Float.class;
			else if (clazz.getName().equals("double")) return Double.class;
		}
		return clazz;
	}

	/**
	 * to invoke a getter Method of an Object
	 * 
	 * @param o Object to invoke method from
	 * @param prop Name of the Method without get
	 * @return return Value of the getter Method
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Object callGetter(Object o, String prop)
			throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		prop = "get" + prop;
		Class c = o.getClass();
		Method m = getMethodParameterPairIgnoreCase(c, prop, null).getMethod();

		// Method m=getMethodIgnoreCase(c,prop,null);
		if (m.getReturnType().getName().equals("void")) throw new NoSuchMethodException("invalid return Type, method [" + m.getName() + "] can't have return type void");
		return m.invoke(o, new Object[0]);
	}

	/**
	 * to invoke a setter Method of an Object
	 * 
	 * @param o Object to invoke method from
	 * @param prop Name of the Method without get
	 * @param value Value to set to the Method
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void callSetter(Object o, String prop, Object value)
			throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		prop = "set" + StringUtil.ucFirst(prop);
		Class c = o.getClass();
		// Class[] cArg=new Class[]{value.getClass()};
		Object[] oArg = new Object[] { value };
		MethodParameterPair mp = getMethodParameterPairIgnoreCase(c, prop, oArg);
		// Method m=getMethodIgnoreCase(c,prop,cArg);
		if (!mp.getMethod().getReturnType().getName().equals("void")) throw new NoSuchMethodException(
				"invalid return Type, method [" + mp.getMethod().getName() + "] must have return type void, now [" + mp.getMethod().getReturnType().getName() + "]");
		mp.getMethod().invoke(o, mp.getParameters());
	}

	/**
	 * to get a visible Property of an object
	 * 
	 * @param o Object to invoke
	 * @param prop property to call
	 * @return property value
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Object getProperty(Object o, String prop) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field f = getFieldIgnoreCase(o.getClass(), prop);
		return f.get(o);
	}

	/**
	 * assign a value to a visible property of an object
	 * 
	 * @param o Object to assign value to his property
	 * @param prop name of property
	 * @param value Value to assign
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static void setProperty(Object o, String prop, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		getFieldIgnoreCase(o.getClass(), prop).set(o, value);
	}

	/**
	 * same like method getField from Class but ignore case from field name
	 * 
	 * @param c class to search the field
	 * @param name name to search
	 * @return Matching Field
	 * @throws NoSuchFieldException
	 */
	public static Field getFieldIgnoreCase(Class c, String name) throws NoSuchFieldException {
		Field[] fields = c.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			// Same Name
			if (f.getName().equalsIgnoreCase(name)) {
				return f;
			}
		}
		throw new NoSuchFieldException("Field doesn't exist");
	}

	/**
	 * call of a static method of a Class
	 * 
	 * @param staticClass class how contains method to invoke
	 * @param methodName method name to invoke
	 * @param values Arguments for the Method
	 * @return return value from method
	 * @throws PageException
	 */
	public static Object callStaticMethod(Class staticClass, String methodName, Object[] values) throws PageException {
		if (values == null) values = new Object[0];

		MethodParameterPair mp;
		try {
			mp = getMethodParameterPairIgnoreCase(staticClass, methodName, values);
		}
		catch (NoSuchMethodException e) {
			throw Caster.toPageException(e);
		}

		try {
			return mp.getMethod().invoke(null, mp.getParameters());
		}
		catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof PageException) throw (PageException) target;
			throw Caster.toPageException(e.getTargetException());
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
}