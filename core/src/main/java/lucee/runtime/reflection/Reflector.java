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
package lucee.runtime.reflection;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefInteger;
import lucee.runtime.Component;
import lucee.runtime.JF;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.functions.conversion.DeserializeJSON;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.OpUtil;
import lucee.runtime.reflection.pairs.ConstructorInstance;
import lucee.runtime.reflection.pairs.MethodInstance;
import lucee.runtime.reflection.storage.WeakFieldStorage;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Pojo;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.Type;
import lucee.runtime.util.ObjectIdentityHashSet;
import lucee.transformer.bytecode.util.JavaProxyFactory;
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.Constructor;
import lucee.transformer.dynamic.meta.Method;

/**
 * Class to reflect on Objects and classes
 */
public final class Reflector {

	private static WeakFieldStorage fStorage = new WeakFieldStorage();

	/**
	 * check if Class is instanceof another Class
	 * 
	 * @param srcClassName Class name to check
	 * @param trg is Class of?
	 * @return is Class Class of...
	 */
	public static boolean isInstaneOf(String srcClassName, Class trg) {
		Class clazz = ClassUtil.loadClass(srcClassName, null);
		if (clazz == null) return false;
		return isInstaneOf(clazz, trg, false);
	}

	/**
	 * check if Class is instanceof another Class
	 * 
	 * @param srcClassName Class name to check
	 * @param trgClassName is Class of?
	 * @return is Class Class of...
	 */
	public static boolean isInstaneOf(String srcClassName, String trgClassName) {
		Class clazz = ClassUtil.loadClass(srcClassName, null);
		if (clazz == null) return false;
		return isInstaneOf(clazz, trgClassName);
	}

	/**
	 * check if Class is instanceof another Class
	 * 
	 * @param src is Class of?
	 * @param trgClassName Class name to check
	 * @return is Class Class of...
	 */
	public static boolean isInstaneOfOld(Class src, String trgClassName) {
		Class clazz = ClassUtil.loadClass(trgClassName, null);
		if (clazz == null) return false;
		return isInstaneOf(src, clazz, false);
	}

	public static boolean isInstaneOf(ClassLoader cl, Class src, String trgClassName) {
		Class clazz = ClassUtil.loadClass(cl, trgClassName, null);
		if (clazz == null) return false;
		return isInstaneOf(src, clazz, false);
	}

	public static boolean isInstaneOfIgnoreCase(Class src, String trg) {
		if (src.isArray()) {
			return isInstaneOfIgnoreCase(src.getComponentType(), trg);
		}

		if (src.getName().equalsIgnoreCase(trg)) return true;

		// Interface
		if (_checkInterfaces(src, trg, false)) {
			return true;
		}
		// Extends
		src = src.getSuperclass();
		if (src != null) return isInstaneOfIgnoreCase(src, trg);
		return false;
	}

	public static boolean isInstaneOf(Class src, String trg) {
		if (src.isArray()) {
			return isInstaneOf(src.getComponentType(), trg);
		}

		if (src.getName().equals(trg)) return true;

		// Interface
		if (_checkInterfaces(src, trg, false)) {
			return true;
		}
		// Extends
		src = src.getSuperclass();
		if (src != null) return isInstaneOf(src, trg);
		return false;
	}

	/**
	 * check if Class is instanceof another Class
	 * 
	 * @param src Class to check
	 * @param trg is Class of?
	 * @param exatctMatch if false a valid match is when thy have the same full name, if true they also
	 *            need to have the same classloader
	 * @return is Class Class of...
	 * 
	 */
	public static boolean isInstaneOf(Class src, Class trg, boolean exatctMatch) {
		if (exatctMatch) return trg.isAssignableFrom(src);

		if (src.isArray() && trg.isArray()) {
			return isInstaneOf(src.getComponentType(), trg.getComponentType(), false);
		}

		if (src == trg || (src.getName().equals(trg.getName()))) return true;

		// Interface
		if (trg.isInterface()) {
			return _checkInterfaces(src, trg, false);
		}
		// Extends

		while (src != null) {
			if (src == trg || (src.getName().equals(trg.getName()))) return true;
			src = src.getSuperclass();
		}
		return trg == Object.class;
	}

	private static boolean _checkInterfaces(Class src, String trg, boolean caseSensitive) {
		Class[] interfaces = src.getInterfaces();
		if (interfaces == null) return false;
		for (int i = 0; i < interfaces.length; i++) {
			if (caseSensitive) {
				if (interfaces[i].getName().equalsIgnoreCase(trg)) return true;
			}
			else {
				if (interfaces[i].getName().equals(trg)) return true;
			}

			if (_checkInterfaces(interfaces[i], trg, caseSensitive)) return true;
		}
		return false;
	}

	private static boolean _checkInterfaces(Class src, Class trg, boolean exatctMatch) {
		Class[] interfaces = src.getInterfaces();
		if (interfaces == null) return false;
		for (int i = 0; i < interfaces.length; i++) {
			if (interfaces[i] == trg || (!exatctMatch && interfaces[i].getName().equals(trg.getName()))) return true;
			if (_checkInterfaces(interfaces[i], trg, exatctMatch)) return true;
		}
		src = src.getSuperclass();
		if (src != null) return _checkInterfaces(src, trg, exatctMatch);
		return false;
	}

	/**
	 * get all Classes from an Object Array
	 * 
	 * @param objs Objects to get
	 * @return classes from Objects
	 */
	public static Class[] getClasses(Object[] objs) {
		Class[] cls = new Class[objs.length];
		for (int i = 0; i < objs.length; i++) {
			if (objs[i] == null) cls[i] = Object.class;
			else cls[i] = objs[i].getClass();
		}
		return cls;
	}

	/**
	 * convert a primitive class Type to a Reference Type (Example: int -> java.lang.Integer)
	 * 
	 * @param c Class to convert
	 * @return converted Class (if primitive)
	 */
	public static Class toReferenceClass(Class c) {
		if (c != null && c.isPrimitive()) {
			if (c == boolean.class) return Boolean.class;
			if (c == int.class) return Integer.class;
			if (c == long.class) return Long.class;
			if (c == double.class) return Double.class;
			if (c == byte.class) return Byte.class;
			if (c == short.class) return Short.class;
			if (c == char.class) return Character.class;
			if (c == float.class) return Float.class;
		}
		return c;
	}

	/**
	 * creates a string list with class arguments in a displable form
	 * 
	 * @param clazzArgs arguments to display
	 * @return list
	 */
	public static String getDspMethods(Class... clazzArgs) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < clazzArgs.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(Caster.toTypeName(clazzArgs[i]));
		}
		return sb.toString();
	}

	/**
	 * checks if src Class is "like" trg class
	 * 
	 * @param src Source Class
	 * @param trg Target Class
	 * @return is similar
	 */
	public static boolean like(Class src, Class trg) {
		if (src == trg) return true;
		if (trg.isAssignableFrom(src)) return true;
		return isInstaneOf(src, trg, false);
	}

	public static Object convert(Object src, Class trgClass, RefInteger rating, Object defaultValue) {
		try {
			return convert(src, trgClass, rating);
		}
		catch (PageException e) {// MUST handle this better
			return defaultValue;
		}
	}

	/**
	 * convert Object from src to trg Type, if possible
	 * 
	 * @param src Object to convert
	 * @param trgClass Target Class
	 * @param rating
	 * @return converted Object
	 * @throws PageException
	 */
	public static Object convert(Object src, Class trgClass, RefInteger rating) throws PageException {
		if (rating != null) {
			Object trg = _convert(src, trgClass, rating);
			if (src == trg) {
				rating.plus(10);
				return trg;
			}
			if (src == null || trg == null) {
				rating.plus(0);
				return trg;
			}
			if (isInstaneOf(src.getClass(), trg.getClass(), true)) {
				rating.plus(9);
				return trg;
			}
			if (src.equals(trg)) {
				rating.plus(8);
				return trg;
			}

			// different number
			boolean bothNumbers = src instanceof Number && trg instanceof Number;
			if (bothNumbers && ((Number) src).doubleValue() == ((Number) trg).doubleValue()) {
				rating.plus(7);
				return trg;
			}

			String sSrc = Caster.toString(src, null);
			String sTrg = Caster.toString(trg, null);
			if (sSrc != null && sTrg != null) {

				// different number types
				if (src instanceof Number && trg instanceof Number && sSrc.equals(sTrg)) {
					rating.plus(6);
					return trg;
				}

				// looks the same
				if (sSrc.equals(sTrg)) {
					rating.plus(5);
					return trg;
				}
				if (sSrc.equalsIgnoreCase(sTrg)) {
					rating.plus(4);
					return trg;
				}
			}

			// CF Equal
			try {
				if (OpUtil.equals(ThreadLocalPageContext.get(), src, trg, false, true)) {
					rating.plus(3);
					return trg;
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}

			return trg;
		}
		return _convert(src, trgClass, rating);
	}

	public static Object _convert(Object src, final Class trgClass, RefInteger rating) throws PageException {
		if (src == null) {
			if (trgClass.isPrimitive()) throw new ApplicationException("can't convert [null] to [" + trgClass.getName() + "]");
			return null;
		}
		if (like(src.getClass(), trgClass)) return src;
		String className = trgClass.getName();

		if (src instanceof ObjectWrap) {
			src = ((ObjectWrap) src).getEmbededObject();
			return _convert(src, trgClass, rating);
		}

		// component as class
		PageContext pc = null;
		if (src instanceof Component && trgClass.isInterface() && (pc = ThreadLocalPageContext.get()) != null) {
			return componentToClass(pc, rating, (Component) src, null, trgClass);
		}

		// UDF as @FunctionalInterface
		if (src instanceof UDF && (pc = ThreadLocalPageContext.get()) != null) {
			FunctionalInterface fi = (FunctionalInterface) trgClass.getAnnotation(FunctionalInterface.class);
			if (fi != null) {
				try {
					return JavaProxyFactory.createProxy(pc, (UDF) src, trgClass);
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}
		}

		// java.lang.String
		if (className.startsWith("java.")) {
			if (trgClass == Boolean.class) return Caster.toBoolean(src);
			if (trgClass == Integer.class) return Caster.toInteger(src);
			if (trgClass == String.class) return Caster.toString(src);
			if (trgClass == Byte.class) return Caster.toByte(src);
			if (trgClass == Short.class) return Caster.toShort(src);
			if (trgClass == Long.class) return Caster.toLong(src);
			if (trgClass == Float.class) return Caster.toFloat(src);
			if (trgClass == Double.class) return Caster.toDouble(src);
			if (trgClass == BigDecimal.class) return Caster.toBigDecimal(src);
			if (trgClass == BigInteger.class) return Caster.toBigInteger(src);
			if (trgClass == Character.class) {
				String str = Caster.toString(src, null);
				if (str != null && str.length() == 1) return Character.valueOf(str.charAt(0));
			}
		}

		if (Decision.isArray(src)) {
			if (trgClass.isArray()) {
				return toNativeArray(trgClass, src);
			}
			else if (isInstaneOf(trgClass, List.class, true)) {
				return Caster.toList(src);
			}
			else if (isInstaneOf(trgClass, Array.class, true)) {
				return Caster.toArray(src);
			}
		}

		if (trgClass == Calendar.class && Decision.isDate(src, true)) {
			TimeZone tz = ThreadLocalPageContext.getTimeZone(pc);
			return Caster.toCalendar(Caster.toDate(src, tz), tz, Locale.US);
		}

		if (trgClass == Date.class) return Caster.toDate(src, true, null);
		else if (trgClass == Query.class) return Caster.toQuery(src);
		else if (trgClass == Map.class) return Caster.toMap(src);
		else if (trgClass == Struct.class) return Caster.toStruct(src);
		else if (trgClass == Resource.class) return Caster.toResource(ThreadLocalPageContext.get(), src, false);
		// this 2 method are used to support conversion that match neo src types
		else if (trgClass == Hashtable.class) return Caster.toHashtable(src);
		else if (trgClass == Vector.class) return Caster.toVetor(src);
		else if (trgClass == java.util.Collection.class) return Caster.toJavaCollection(src);
		else if (trgClass == TimeZone.class && Decision.isString(src)) return Caster.toTimeZone(Caster.toString(src));
		else if (trgClass == Collection.Key.class) return KeyImpl.toKey(src);
		else if (trgClass == Locale.class && Decision.isString(src)) return Caster.toLocale(Caster.toString(src));
		else if (Reflector.isInstaneOf(trgClass, Pojo.class, true) && src instanceof Map) {
			Struct sct = Caster.toStruct(src);
			try {
				Pojo pojo = (Pojo) ClassUtil.newInstance(trgClass);
				if (sct instanceof Component) return Caster.toPojo(pojo, (Component) sct, new HashSet<Object>(), false);
				return Caster.toPojo(pojo, sct, new HashSet<Object>(), false);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		if (trgClass.isPrimitive()) {
			// return convert(src,srcClass,toReferenceClass(trgClass));
			return _convert(src, toReferenceClass(trgClass), rating);
		}
		throw new ApplicationException("can't convert [" + Caster.toClassName(src) + "] to [" + Caster.toClassName(trgClass) + "]");
	}

	public static Object componentToClass(PageContext pc, Component src) throws PageException {
		return componentToClass(pc, null, src, null);
	}

	public static Object componentToClass(PageContext pc, Component src, Class trgClass) throws PageException {
		return componentToClass(pc, null, src, trgClass);
	}

	public static Object componentToClass(PageContext pc, Component src, Class trgClass, Class... interfaces) throws PageException {
		return componentToClass(pc, null, src, trgClass, interfaces);
	}

	private static Object componentToClass(PageContext pc, RefInteger rating, Component src, Class trgClass, Class... interfaces) throws PageException {
		if (pc == null) throw new ApplicationException("cannot convert component [" + (src == null ? "" : src.getName())
				+ "] to a Java class, because there is no PageContext available for the current thread");
		try {
			JavaAnnotation ja = getJavaAnnotation(pc, trgClass != null ? trgClass.getClassLoader() : SystemUtil.getCoreClassLoader(), src);
			Class<?> _extends = ja != null && ja.extend != null ? ja.extend : null;
			return JavaProxyFactory.createProxy(pc, src, _extends, merge(extractImplements(pc, src, ja, trgClass, rating), interfaces));
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static Class<?>[] extractImplements(PageContext pc, Component cfc, JavaAnnotation ja, Class<?> trgClass, RefInteger ratings) throws PageException {
		Struct md = cfc.getMetaData(pc);
		Object implementsjavaObj = md.get(KeyConstants._implementsjava, null);
		Class<?>[] implementsjava = null;
		if (implementsjavaObj != null) {
			Object[] arrImplements = null;
			if (Decision.isArray(implementsjavaObj)) {
				arrImplements = Caster.toNativeArray(implementsjavaObj);
			}
			else if (Decision.isCastableToString(implementsjavaObj)) {
				arrImplements = ListUtil.listToStringArray(Caster.toString(implementsjavaObj, null), ',');
			}

			if (ratings != null) ratings.plus(0);
			if (arrImplements != null) {
				List<Class<?>> list = new ArrayList<>();

				ClassLoader cl = ComponentUtil.getClassLoader(pc, cfc, SystemUtil.getCoreClassLoader());
				Class clazz;
				for (Object o: arrImplements) {
					String str = Caster.toString(o, null);
					if (StringUtil.isEmpty(str)) continue;
					clazz = ClassUtil.loadClass(cl, str, null);
					if (clazz != null) {
						list.add(clazz);
						if (ratings != null && trgClass != null) {
							if (isInstaneOf(clazz, trgClass, true)) ratings.plus(6);
							else if (isInstaneOf(clazz, trgClass, false)) ratings.plus(5);
						}
					}
				}
				implementsjava = list.toArray(new Class<?>[list.size()]);
			}
		}

		Class<?>[] _implements;
		if (ja != null && ja.interfaces != null) {
			_implements = ja.interfaces;
		}
		else {
			if (trgClass != null && trgClass.isInterface()) _implements = new Class<?>[] { trgClass };
			else _implements = new Class<?>[] {};
		}

		if (implementsjava != null) {
			_implements = merge(_implements, implementsjava);
		}
		return _implements;
	}

	private static Class<?>[] merge(Class<?>[] left, Class<?>[] right) {
		Map<String, Class<?>> map = new HashMap<>();
		if (left != null) {
			for (Class tmp: left) {
				map.put(tmp.getName(), tmp);
			}
		}
		if (right != null) {
			for (Class tmp: right) {
				map.put(tmp.getName(), tmp);
			}
		}
		return map.values().toArray(new Class<?>[map.size()]);
	}

	public static Object udfToClass(PageContext pc, UDF src, Class trgClass) throws PageException {
		// it already is a java Function
		if (src instanceof JF && Reflector.isInstaneOf(src.getClass(), trgClass, true)) {
			return src;
		}
		try {

			// JavaAnnotation ja = getJavaAnnotation(pc, trgClass.getClassLoader(), src);
			// return JavaProxyFactory.createProxy(pc, src, ja != null && ja.extend != null ? ja.extend :
			// null,ja != null && ja.interfaces != null ? ja.interfaces : new Class[] { trgClass });
			return JavaProxyFactory.createProxy(pc, src, trgClass);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static JavaAnnotation getJavaAnnotation(PageContext pc, ClassLoader cl, Component src) {
		Struct md = null;
		try {
			md = src.getMetaData(pc);
		}
		catch (PageException pe) {}

		String str;
		JavaAnnotation ja = null;
		if (md != null && (str = Caster.toString(md.get(KeyConstants._java, null), null)) != null) {
			Struct sct = null;
			if (StringUtil.isEmpty(str, true)) return null;
			try {
				sct = Caster.toStruct(DeserializeJSON.call(pc, str), null);
			}
			catch (Exception e) {}
			if (sct == null) return null;

			// interfaces
			Object o = sct.get(KeyConstants._interface, null);
			if (o == null) o = sct.get(KeyConstants._interfaces, null);
			if (o != null) {
				Array arr = null;
				if (Decision.isArray(o)) {
					arr = Caster.toArray(o, null);
				}
				else {
					str = Caster.toString(o, null);
					if (!StringUtil.isEmpty(str)) arr = ListUtil.listToArray(str, ",");
				}
				if (arr != null && arr.size() > 0) {
					List<Class<?>> _interfaces = new ArrayList<>();
					Iterator<?> it = arr.getIterator();
					while (it.hasNext()) {
						str = Caster.toString(it.next(), null);
						if (!StringUtil.isEmpty(str, true)) _interfaces.add(ClassUtil.loadClass(cl, str, null));
					}
					if (ja == null) ja = new JavaAnnotation();
					ja.interfaces = _interfaces.toArray(new Class<?>[_interfaces.size()]);
				}
			}

			// extends
			o = sct.get(KeyConstants._extends, null);
			if (o != null) {
				if (ja == null) ja = new JavaAnnotation();
				str = Caster.toString(o, null);
				if (!StringUtil.isEmpty(str, true)) ja.extend = ClassUtil.loadClass(cl, str, null);
			}
		}

		return ja;
	}

	public static class JavaAnnotation {
		private Class<?> extend;
		private Class<?>[] interfaces;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (interfaces != null && interfaces.length > 0) {
				for (Class<?> clazz: interfaces) {
					if (sb.length() > 0) sb.append(',');
					sb.append(clazz.getName());
				}
			}
			else sb.append("null");

			return "extends:" + (extend == null ? "null" : extend.getName()) + "; interfaces:" + sb;
		}
	}

	public static ConstructorInstance getConstructorInstance(Class clazz, Object[] args, boolean exactMatchOnly) {
		return new ConstructorInstance(clazz, args, !exactMatchOnly);
	}

	static int count = 0;

	public static MethodInstance getMethodInstance(Class clazz, final Collection.Key methodName, Object[] args, boolean nameCaseSensitive, boolean exactMatchOnly) {
		return new MethodInstance(clazz, methodName, args, nameCaseSensitive, !exactMatchOnly);
	}

	public static String pos(int index) {
		if (index == 1) return "first";
		if (index == 2) return "second";
		if (index == 3) return "third";

		return index + "th";
	}

	/**
	 * same like method getField from Class but ignore case from field name
	 * 
	 * @param clazz class to search the field
	 * @param name name to search
	 * @return Matching Field
	 * @throws NoSuchFieldException
	 * 
	 */
	public static Field[] getFieldsIgnoreCase(Class clazz, String name) throws NoSuchFieldException {
		Field[] fields = fStorage.getFields(clazz, name);
		if (fields != null) return fields;
		throw new NoSuchFieldException("there is no field with name " + name + " in object [" + Type.getName(clazz) + "]");
	}

	public static Field[] getFieldsIgnoreCase(Class clazz, String name, Field[] defaultValue) {
		Field[] fields = fStorage.getFields(clazz, name);
		if (fields != null) return fields;
		return defaultValue;

	}

	public static String[] getPropertyKeys(Class clazz) {
		Set<String> keys = new HashSet<String>();
		Field[] fields = clazz.getFields();
		Field field;
		String name;

		for (int i = 0; i < fields.length; i++) {
			field = fields[i];
			if (Modifier.isPublic(field.getModifiers())) keys.add(field.getName());
		}

		// dynamic
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		try {
			_getPropertyKeys(clazz, di, keys, false);
		}
		// reflection
		catch (IOException e) {
			// fallback
			LogUtil.log("dynamic", e);
			try {
				_getPropertyKeys(clazz, di, keys, false);
			}
			catch (IOException e1) {
				LogUtil.log("dynamic", e1);
			}
		}
		return keys.toArray(new String[keys.size()]);
	}

	private static void _getPropertyKeys(Class clazz, DynamicInvoker di, Set<String> keys, boolean doReflection) throws IOException {
		Clazz clazzz = di.getClazz(clazz, doReflection);
		List<lucee.transformer.dynamic.meta.Method> methods = clazzz.getMethods(null, true, -1);
		String name;
		for (lucee.transformer.dynamic.meta.Method method: methods) {
			if (method.isPublic()) {
				if (isGetter(method)) {
					name = method.getName();
					if (name.startsWith("get")) keys.add(StringUtil.lcFirst(method.getName().substring(3)));
					else keys.add(StringUtil.lcFirst(method.getName().substring(2)));
				}
				else if (isSetter(method)) keys.add(StringUtil.lcFirst(method.getName().substring(3)));
			}
		}
	}

	public static boolean hasPropertyIgnoreCase(Class clazz, String name) {
		if (hasFieldIgnoreCase(clazz, name)) return true;

		// dynamic
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		try {
			if (_hasPropertyIgnoreCase(clazz, di, name, false)) return true;
		}
		// reflection
		catch (IOException e) {
			LogUtil.log("dynamic", e);
			try {
				if (_hasPropertyIgnoreCase(clazz, di, name, true)) return true;
			}
			catch (IOException e1) {
				LogUtil.log("dynamic", e1);

			}
		}
		return false;
	}

	public static boolean _hasPropertyIgnoreCase(Class clazz, DynamicInvoker di, String name, boolean doReflection) throws IOException {
		String n;
		Clazz clazzz = di.getClazz(clazz, doReflection);
		List<lucee.transformer.dynamic.meta.Method> methods = clazzz.getMethods(null, true, -1);
		for (lucee.transformer.dynamic.meta.Method method: methods) {
			if (method.isPublic() && StringUtil.endsWithIgnoreCase(method.getName(), name)) {
				n = null;
				if (isGetter(method)) {
					n = method.getName();
					if (n.startsWith("get")) n = StringUtil.lcFirst(method.getName().substring(3));
					else n = StringUtil.lcFirst(method.getName().substring(2));
				}
				else if (isSetter(method)) n = method.getName().substring(3);
				if (n != null && n.equalsIgnoreCase(name)) return true;
			}
		}
		return false;
	}

	public static boolean hasFieldIgnoreCase(Class clazz, String name) {
		return !ArrayUtil.isEmpty(getFieldsIgnoreCase(clazz, name, null));
		// getFieldIgnoreCaseEL(clazz, name)!=null;
	}

	/**
	 * call constructor of a class with matching arguments
	 * 
	 * @param clazz Class to get Instance
	 * @param args Arguments for the Class
	 * @return invoked Instance
	 * @throws PageException
	 */
	public static Object callConstructor(Class clazz, Object[] args) throws PageException {
		try {
			return getConstructorInstance(clazz, args, false).invoke();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public static Object callConstructor(Class clazz, Object[] args, Object defaultValue) {
		try {
			ConstructorInstance ci = getConstructorInstance(clazz, args, false);
			if (ci.getConstructor(null) == null) return defaultValue;
			return ci.invoke();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static Object callMethodRE(Object obj, String methodName, Object[] args) {
		try {
			return callMethod(obj, KeyImpl.init(methodName), args, false);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	/**
	 * calls a Method of an Object
	 * 
	 * @param obj Object to call Method on it
	 * @param methodName Name of the Method to get
	 * @param args Arguments of the Method to get
	 * @return return return value of the called Method
	 * @throws PageException
	 */
	public static Object callMethod(Object obj, String methodName, Object[] args) throws PageException {
		return callMethod(obj, KeyImpl.init(methodName), args, false);
	}

	public static Object callMethod(Object obj, String methodName, Object[] args, boolean nameCaseSensitive) throws PageException {
		return callMethod(obj, KeyImpl.init(methodName), args, nameCaseSensitive);
	}

	public static Object callMethod(final Object obj, Collection.Key methodName, Object[] args, boolean nameCaseSensitive) throws PageException {
		return MethodInstance.invoke(obj, methodName, args, nameCaseSensitive, true);
	}

	public static boolean hasMethod(Class<?> clazz, String methodName, Object[] args, boolean nameCaseSensitive) {
		MethodInstance mi = getMethodInstance(clazz, KeyImpl.init(methodName), args, nameCaseSensitive, false);
		return mi.hasMethod();
	}

	public static boolean hasMethod(Class<?> clazz, Collection.Key methodName, Object[] args, boolean nameCaseSensitive) {
		MethodInstance mi = getMethodInstance(clazz, methodName, args, nameCaseSensitive, false);
		return mi.hasMethod();
	}

	public static void checkAccessibility(Class clazz, Key methodName) {
		if (methodName.equals(KeyConstants._exit) && (clazz == System.class || clazz == Runtime.class)) { // TODO better implementation
			throw new PageRuntimeException(new SecurityException("Calling the exit method is not allowed"));
		}
	}

	public static void checkAccessibility(Clazz clazz, Key methodName) {
		if (methodName.equals(KeyConstants._exit)) { // TODO better implementation
			Class cl = clazz.getDeclaringClass();
			if ((cl == System.class || cl == Runtime.class)) { // TODO better implementation
				throw new PageRuntimeException(new SecurityException("Calling the exit method is not allowed"));
			}
		}
	}

	/*
	 * private static void checkAccesibilityx(Object obj, Key methodName) {
	 * if(methodName.equals(SET_ACCESSIBLE) && obj instanceof Member) { if(true) return; Member
	 * member=(Member) obj; Class<?> cls = member.getDeclaringClass();
	 * if(cls.getPackage().getName().startsWith("lucee.")) { throw new PageRuntimeException(new
	 * SecurityException("Changing the accesibility of an object's members in the Lucee.* package is not allowed"
	 * )); } } }
	 */

	public static Object callMethod(Object obj, Collection.Key methodName, Object[] args, boolean nameCaseSensitive, Object defaultValue) {
		if (obj == null) {
			return defaultValue;
		}
		// checkAccesibility(obj,methodName);

		MethodInstance mi = getMethodInstance(obj.getClass(), methodName, args, nameCaseSensitive, false);
		if (!mi.hasMethod()) return defaultValue;
		try {
			return mi.invoke(obj);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static ExpressionException throwCall(String methodName, Object[] args, Clazz clazz) {
		return new ExpressionException("No matching Method/Function for " + Type.getName(clazz) + "." + methodName + "(" + getDspMethods(getClasses(args)) + ") found");
	}

	public static ExpressionException throwCall(Object obj, String methodName, Object[] args) {
		return new ExpressionException("No matching Method/Function for " + Type.getName(obj) + "." + methodName + "(" + getDspMethods(getClasses(args)) + ") found");
	}

	public static Object callStaticMethod(Class clazz, String methodName, Object[] args) throws PageException {
		return callStaticMethod(clazz, KeyImpl.init(methodName), args, false);
	}

	public static Object callStaticMethod(Class clazz, String methodName, Object[] args, boolean nameCaseSensitive) throws PageException {
		return callStaticMethod(clazz, KeyImpl.init(methodName), args, nameCaseSensitive);
	}

	/**
	 * calls a Static Method on the given CLass
	 * 
	 * @param clazz Class to call Method on it
	 * @param methodName Name of the Method to get
	 * @param args Arguments of the Method to get
	 * @return return return value of the called Method
	 * @throws PageException
	 */

	public static Object callStaticMethod(Class clazz, Collection.Key methodName, Object[] args) throws PageException {
		try {
			return getMethodInstance(clazz, methodName, args, false, false).invoke(null);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public static Object callStaticMethod(Class clazz, Collection.Key methodName, Object[] args, boolean nameCaseSensitive) throws PageException {
		try {
			return getMethodInstance(clazz, methodName, args, nameCaseSensitive, false).invoke(null);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * to get a Getter Method of an Object
	 * 
	 * @param clazz Class to invoke method from
	 * @param prop Name of the Method without get
	 * @return return Value of the getter Method
	 * @throws NoSuchMethodException
	 * @throws PageException
	 */

	public static MethodInstance getGetter(Class clazz, String prop, boolean nameCaseSensitive) throws PageException, NoSuchMethodException {
		String getterName = "get" + StringUtil.ucFirst(prop);
		MethodInstance mi = getMethodInstance(clazz, KeyImpl.init(getterName), ArrayUtil.OBJECT_EMPTY, nameCaseSensitive, false);

		if (!mi.hasMethod()) {
			String isName = "is" + StringUtil.ucFirst(prop);
			mi = getMethodInstance(clazz, KeyImpl.init(isName), ArrayUtil.OBJECT_EMPTY, nameCaseSensitive, false);
			if (mi.hasMethod()) {
				lucee.transformer.dynamic.meta.Method m = mi.getMethod();
				Class rtn = m.getReturnClass();
				if (rtn != Boolean.class && rtn != boolean.class) mi = null;
			}
		}

		if (mi == null) throw new ExpressionException("No matching property [" + prop + "] found in [" + Caster.toTypeName(clazz) + "]");
		lucee.transformer.dynamic.meta.Method m = mi.getMethod();

		if (m.getReturnClass() == void.class)
			throw new NoSuchMethodException("invalid return Type, method [" + m.getName() + "] for Property [" + getterName + "] must have return type not void");

		return mi;
	}

	/**
	 * to get a Getter Method of an Object
	 *
	 * @param clazz Class to invoke method from
	 * @param prop Name of the Method without get
	 * @return return Value of the getter Method
	 */
	public static MethodInstance getGetterEL(Class clazz, String prop, boolean nameCaseSensitive) {
		String getterName = "get" + StringUtil.ucFirst(prop);
		MethodInstance mi = getMethodInstance(clazz, KeyImpl.init(getterName), ArrayUtil.OBJECT_EMPTY, nameCaseSensitive, false);

		if (!mi.hasMethod()) {
			String isName = "is" + StringUtil.ucFirst(prop);
			mi = getMethodInstance(clazz, KeyImpl.init(isName), ArrayUtil.OBJECT_EMPTY, nameCaseSensitive, false);
			if (mi.hasMethod()) {
				try {
					lucee.transformer.dynamic.meta.Method m = mi.getMethod();
					Class rtn = m.getReturnClass();
					if (rtn != Boolean.class && rtn != boolean.class) return null;
				}
				catch (PageException e) {
					return null;
				}
			}
			else {
				return null;
			}
		}

		try {
			if (mi.getMethod().getReturnClass() == void.class) return null;
		}
		catch (PageException e) {
			return null;
		}
		return mi;
	}

	public static MethodInstance getGetter(Class clazz, final String prop, boolean nameCaseSensitive, boolean inclueIs, boolean includeEmpty, MethodInstance defaultValue) {
		MethodInstance mi = getMethodInstance(clazz, KeyImpl.init("get" + StringUtil.ucFirst(prop)), ArrayUtil.OBJECT_EMPTY, nameCaseSensitive, false);
		boolean isIs = false;
		if (!mi.hasMethod()) {
			if (!inclueIs) return defaultValue;
			mi = getMethodInstance(clazz, KeyImpl.init("is" + StringUtil.ucFirst(prop)), ArrayUtil.OBJECT_EMPTY, nameCaseSensitive, false);
			if (!mi.hasMethod()) {
				if (!includeEmpty) return defaultValue;
				mi = getMethodInstance(clazz, KeyImpl.init(prop), ArrayUtil.OBJECT_EMPTY, nameCaseSensitive, false);
				if (!mi.hasMethod()) {
					return defaultValue;
				}
			}
			else {
				isIs = true;
			}
		}

		try {
			Class rtn = mi.getMethod().getReturnClass();
			if (rtn == void.class || (isIs && rtn != boolean.class)) return defaultValue;
		}
		catch (PageException e) {
			return defaultValue;
		}
		return mi;
	}

	/**
	 * to invoke a getter Method of an Object
	 * 
	 * @param obj Object to invoke method from
	 * @param prop Name of the Method without get
	 * @return return Value of the getter Method
	 * @throws PageException
	 */
	public static Object callGetter(Object obj, String prop, boolean nameCaseSensitive) throws PageException {
		try {
			return getGetter(obj.getClass(), prop, nameCaseSensitive).invoke(obj);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * to invoke a setter Method of an Object
	 * 
	 * @param obj Object to invoke method from
	 * @param prop Name of the Method without get
	 * @param value Value to set to the Method
	 * @return MethodInstance
	 * @throws PageException
	 */
	public static MethodInstance getSetter(Object obj, String prop, Object value, boolean nameCaseSensitive) throws PageException {
		prop = "set" + StringUtil.ucFirst(prop);
		MethodInstance mi = getMethodInstance(obj.getClass(), KeyImpl.init(prop), new Object[] { value }, nameCaseSensitive, false);
		lucee.transformer.dynamic.meta.Method m = mi.getMethod();

		if (m.getReturnClass() != void.class)
			throw Caster.toPageException(new NoSuchMethodException("invalid return Type, method [" + m.getName() + "] must have return type void, now [" + m.getReturn() + "]"));
		return mi;
	}

	/**
	 * to invoke a setter Method of an Object
	 * 
	 * @param obj Object to invoke method from
	 * @param prop Name of the Method without get
	 * @param value Value to set to the Method
	 * @return MethodInstance
	 */
	public static MethodInstance getSetter(Object obj, String prop, Object value, MethodInstance defaultValue) {
		return getSetter(obj, prop, value, false, defaultValue);
	}

	public static MethodInstance getSetter(Object obj, String prop, Object value, boolean nameCaseSensitive, MethodInstance defaultValue) {
		prop = "set" + StringUtil.ucFirst(prop);
		MethodInstance mi = getMethodInstance(obj.getClass(), KeyImpl.init(prop), new Object[] { value }, nameCaseSensitive, false);
		lucee.transformer.dynamic.meta.Method m;
		if ((m = mi.getMethod(null)) == null) return defaultValue;

		if (m.getReturnClass() != void.class) return defaultValue;
		return mi;
	}

	/**
	 * to invoke a setter Method of an Object
	 * 
	 * @param obj Object to invoke method from
	 * @param prop Name of the Method without get
	 * @param value Value to set to the Method
	 * @throws PageException
	 */
	public static void callSetter(Object obj, String prop, Object value) throws PageException {
		try {
			getSetter(obj, prop, value, false).invoke(obj);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public static void callSetter(Object obj, String prop, Object value, boolean nameCaseSensitive) throws PageException {
		try {
			getSetter(obj, prop, value, nameCaseSensitive).invoke(obj);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * do nothing when not exist
	 * 
	 * @param obj
	 * @param prop
	 * @param value
	 * @throws PageException
	 */
	public static void callSetterEL(Object obj, String prop, Object value, boolean nameCaseSensitive) throws PageException {
		try {
			MethodInstance setter = getSetter(obj, prop, value, nameCaseSensitive, null);
			if (setter != null) setter.invoke(obj);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * to get a visible Field of an object
	 * 
	 * @param obj Object to invoke
	 * @param prop property to call
	 * @return property value
	 * @throws PageException
	 */
	public static Object getField(Object obj, String prop) throws PageException {
		try {
			return getFieldsIgnoreCase(obj.getClass(), prop)[0].get(obj);
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
			throw Caster.toPageException(e);
		}
	}

	public static Object getField(Object obj, String prop, Object defaultValue) {
		return getField(obj, prop, false, defaultValue);
	}

	public static Object getField(Object obj, String prop, boolean accessible, Object defaultValue) {
		if (obj == null) return defaultValue;
		Field[] fields = getFieldsIgnoreCase(obj.getClass(), prop, null);
		if (ArrayUtil.isEmpty(fields)) return defaultValue;

		try {
			if (accessible) fields[0].setAccessible(true);
			return fields[0].get(obj);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	/**
	 * assign a value to a visible Field of an object
	 * 
	 * @param obj Object to assign value to his property
	 * @param prop name of property
	 * @param value Value to assign
	 * @throws PageException
	 */
	public static boolean setField(Object obj, String prop, Object value) throws PageException {
		Class clazz = value.getClass();
		try {
			Field[] fields = getFieldsIgnoreCase(obj.getClass(), prop);
			// exact comparsion
			for (int i = 0; i < fields.length; i++) {
				if (toReferenceClass(fields[i].getType()) == clazz) {
					fields[i].set(obj, value);
					return true;
				}
			}
			// like comparsion
			for (int i = 0; i < fields.length; i++) {
				if (like(fields[i].getType(), clazz)) {
					fields[i].set(obj, value);
					return true;
				}
			}
			// convert comparsion
			for (int i = 0; i < fields.length; i++) {
				try {
					fields[i].set(obj, convert(value, toReferenceClass(fields[i].getType()), null));
					return true;
				}
				catch (PageException e) {}
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		return false;
	}

	/**
	 * to get a visible Propety (Field or Getter) of an object
	 * 
	 * @param obj Object to invoke
	 * @param prop property to call
	 * @return property value
	 * @throws PageException
	 */
	public static Object getProperty(Object obj, String prop, boolean nameCaseSensitive) throws PageException {
		Object rtn = getField(obj, prop, CollectionUtil.NULL);// NULL is used because the field can contain null as well
		if (rtn != CollectionUtil.NULL) return rtn;

		char first = prop.charAt(0);
		MethodInstance getter = (first >= '0' && first <= '9') ? null : getGetterEL(obj.getClass(), prop, nameCaseSensitive);
		if (getter == null) throw new ApplicationException("there is no property with name [" + prop + "]  found in [" + Caster.toTypeName(obj) + "]");
		try {
			return getter.invoke(obj);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * to get a visible Propety (Field or Getter) of an object
	 * 
	 * @param obj Object to invoke
	 * @param prop property to call
	 * @return property value
	 */

	public static Object getProperty(Object obj, String prop, Object defaultValue) {
		return getProperty(obj, prop, false, defaultValue);
	}

	public static Object getProperty(Object obj, String prop, boolean nameCaseSensitive, Object defaultValue) {

		// first try field
		Field[] fields = getFieldsIgnoreCase(obj.getClass(), prop, null);
		if (!ArrayUtil.isEmpty(fields)) {
			try {
				return fields[0].get(obj);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		// then getter
		try {
			char first = prop.charAt(0);
			if (first >= '0' && first <= '9') return defaultValue;
			return getGetter(obj.getClass(), prop, nameCaseSensitive).invoke(obj);
		}
		catch (Throwable e1) {
			ExceptionUtil.rethrowIfNecessary(e1);
			return defaultValue;
		}
	}

	/**
	 * assign a value to a visible Property (Field or Setter) of an object
	 * 
	 * @param obj Object to assign value to his property
	 * @param prop name of property
	 * @param value Value to assign
	 * @throws PageException
	 */

	public static void setProperty(Object obj, String prop, Object value) throws PageException {
		setProperty(obj, prop, value, false);
	}

	public static void setProperty(Object obj, String prop, Object value, boolean nameCaseSensitive) throws PageException {
		boolean done = false;
		try {
			if (setField(obj, prop, value)) done = true;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		if (!done) callSetter(obj, prop, value, nameCaseSensitive);
	}

	/**
	 * assign a value to a visible Property (Field or Setter) of an object
	 * 
	 * @param obj Object to assign value to his property
	 * @param prop name of property
	 * @param value Value to assign
	 */
	public static void setPropertyEL(Object obj, String prop, Object value, boolean nameCaseSensitive) {

		// first check for field
		Field[] fields = getFieldsIgnoreCase(obj.getClass(), prop, null);
		if (!ArrayUtil.isEmpty(fields)) {
			try {
				fields[0].set(obj, value);
				return;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		// then check for setter
		try {
			getSetter(obj, prop, value, nameCaseSensitive).invoke(obj);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

	}

	private static Object toNativeArray(Class clazz, Object obj) throws PageException {
		// if(obj.getClass()==clazz) return obj;
		Object[] objs = null;
		if (obj instanceof Array) objs = toRefArray((Array) obj);
		else if (obj instanceof List) objs = toRefArray((List) obj);
		else if (Decision.isNativeArray(obj)) {
			if (obj.getClass() == boolean[].class) objs = toRefArray((boolean[]) obj);
			else if (obj.getClass() == byte[].class) objs = toRefArray((byte[]) obj);
			else if (obj.getClass() == char[].class) objs = toRefArray((char[]) obj);
			else if (obj.getClass() == short[].class) objs = toRefArray((short[]) obj);
			else if (obj.getClass() == int[].class) objs = toRefArray((int[]) obj);
			else if (obj.getClass() == long[].class) objs = toRefArray((long[]) obj);
			else if (obj.getClass() == float[].class) objs = toRefArray((float[]) obj);
			else if (obj.getClass() == double[].class) objs = toRefArray((double[]) obj);
			else objs = (Object[]) obj;// toRefArray((Object[])obj);

		}
		if (clazz == objs.getClass()) {
			return objs;
		}

		// if(objs==null) return defaultValue;
		// Class srcClass = objs.getClass().getComponentType();
		Class compClass = clazz.getComponentType();
		Object rtn = java.lang.reflect.Array.newInstance(compClass, objs.length);
		// try{
		for (int i = 0; i < objs.length; i++) {
			java.lang.reflect.Array.set(rtn, i, convert(objs[i], compClass, null));
		}
		// }catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);}

		return rtn;
	}

	private static Object[] toRefArray(boolean[] src) {
		Boolean[] trg = new Boolean[src.length];
		for (int i = 0; i < trg.length; i++) {
			trg[i] = src[i] ? Boolean.TRUE : Boolean.FALSE;
		}
		return trg;
	}

	private static Byte[] toRefArray(byte[] src) {
		Byte[] trg = new Byte[src.length];
		for (int i = 0; i < trg.length; i++) {
			trg[i] = Byte.valueOf(src[i]);
		}
		return trg;
	}

	private static Character[] toRefArray(char[] src) {
		Character[] trg = new Character[src.length];
		for (int i = 0; i < trg.length; i++) {
			trg[i] = Character.valueOf(src[i]);
		}
		return trg;
	}

	private static Short[] toRefArray(short[] src) {
		Short[] trg = new Short[src.length];
		for (int i = 0; i < trg.length; i++) {
			trg[i] = Short.valueOf(src[i]);
		}
		return trg;
	}

	private static Integer[] toRefArray(int[] src) {
		Integer[] trg = new Integer[src.length];
		for (int i = 0; i < trg.length; i++) {
			trg[i] = Integer.valueOf(src[i]);
		}
		return trg;
	}

	private static Long[] toRefArray(long[] src) {
		Long[] trg = new Long[src.length];
		for (int i = 0; i < trg.length; i++) {
			trg[i] = Long.valueOf(src[i]);
		}
		return trg;
	}

	private static Float[] toRefArray(float[] src) {
		Float[] trg = new Float[src.length];
		for (int i = 0; i < trg.length; i++) {
			trg[i] = Float.valueOf(src[i]);
		}
		return trg;
	}

	private static Double[] toRefArray(double[] src) {
		Double[] trg = new Double[src.length];
		for (int i = 0; i < trg.length; i++) {
			trg[i] = Double.valueOf(src[i]);
		}
		return trg;
	}

	private static Object[] toRefArray(Array array) throws PageException {
		Object[] objs = new Object[array.size()];
		for (int i = 0; i < objs.length; i++) {
			objs[i] = array.getE(i + 1);
		}
		return objs;
	}

	private static Object[] toRefArray(List list) {
		Object[] objs = new Object[list.size()];
		for (int i = 0; i < objs.length; i++) {
			objs[i] = list.get(i);
		}
		return objs;
	}

	public static boolean isGetter(lucee.transformer.dynamic.meta.Method method) {
		if (method.getArgumentCount() > 0) return false;
		if (method.getReturnClass() == void.class) return false;
		if (!method.getName().startsWith("get") && !method.getName().startsWith("is")) return false;
		if (method.getDeclaringClass() == Object.class) return false;
		return true;
	}

	/*
	 * public static boolean isGetter(Method method) { if (method.getParameterTypes().length > 0) return
	 * false; if (method.getReturnType() == void.class) return false; if
	 * (!method.getName().startsWith("get") && !method.getName().startsWith("is")) return false; if
	 * (method.getDeclaringClass() == Object.class) return false; return true; }
	 * 
	 * public static boolean isSetter(Method method) { if (method.getParameterTypes().length != 1)
	 * return false; if (method.getReturnType() != void.class) return false; if
	 * (!method.getName().startsWith("set")) return false; if (method.getDeclaringClass() ==
	 * Object.class) return false; return true; }
	 */

	public static boolean isSetter(lucee.transformer.dynamic.meta.Method method) {
		if (method.getArgumentCount() != 1) return false;
		if (method.getReturnClass() != void.class) return false;
		if (!method.getName().startsWith("set")) return false;
		if (method.getDeclaringClass() == Object.class) return false;
		return true;
	}

	public static List<lucee.transformer.dynamic.meta.Method> getMethods(Class clazz) throws IOException {
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		try {
			return di.getClazz(clazz, false).getMethods(null, true, -1);
		}
		catch (IOException e) {
			return di.getClazz(clazz, true).getMethods(null, true, -1);
		}
	}

	/**
	 * return all methods that are defined by the class itself (not extended)
	 * 
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	public static List<lucee.transformer.dynamic.meta.Method> getDeclaredMethods(Class clazz) throws IOException {
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		try {
			return di.getClazz(clazz, false).getDeclaredMethods(null, true, -1);
		}
		catch (IOException e) {
			return di.getClazz(clazz, true).getDeclaredMethods(null, true, -1);
		}
	}

	public static List<lucee.transformer.dynamic.meta.Method> getSetters(Class clazz) {
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		List<lucee.transformer.dynamic.meta.Method> list = new ArrayList<lucee.transformer.dynamic.meta.Method>();
		try {
			_getSetters(clazz, di, list, false);
			return list;
		}
		catch (IOException e) {
			LogUtil.log("dynamic", e);
			try {
				_getSetters(clazz, di, list, true);
			}
			catch (IOException e1) {
				LogUtil.log("dynamic", e1);
			}
			return list;
		}
	}

	private static void _getSetters(Class clazz, DynamicInvoker di, List<lucee.transformer.dynamic.meta.Method> list, boolean doReflection) throws IOException {
		Clazz clazzz = di.getClazz(clazz, doReflection);
		List<lucee.transformer.dynamic.meta.Method> methods = clazzz.getMethods(null, true, -1);
		for (lucee.transformer.dynamic.meta.Method method: methods) {
			if (isSetter(method)) list.add(method);
		}
	}

	public static List<lucee.transformer.dynamic.meta.Method> getGetters(Class clazz) {
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		List<lucee.transformer.dynamic.meta.Method> list = new ArrayList<lucee.transformer.dynamic.meta.Method>();
		try {
			_getGetters(clazz, di, list, false);
			return list;
		}
		catch (IOException e) {
			LogUtil.log("dynamic", e);
			try {
				_getGetters(clazz, di, list, true);
			}
			catch (IOException e1) {
				LogUtil.log("dynamic", e1);
			}
			return list;
		}
	}

	private static void _getGetters(Class clazz, DynamicInvoker di, List<lucee.transformer.dynamic.meta.Method> list, boolean doReflection) throws IOException {
		Clazz clazzz = di.getClazz(clazz, doReflection);
		List<lucee.transformer.dynamic.meta.Method> methods = clazzz.getMethods(null, true, -1);
		for (lucee.transformer.dynamic.meta.Method method: methods) {
			if (isGetter(method)) list.add(method);
		}
	}

	/**
	 * check if given class "from" can be converted to class "to" without explicit casting
	 * 
	 * @param from source class
	 * @param to target class
	 * @return is it possible to convert from "from" to "to"
	 */
	public static boolean canConvert(Class from, Class to) {
		// Identity Conversions
		if (from == to) return true;

		// Widening Primitive Conversion
		if (from == byte.class) {
			return to == short.class || to == int.class || to == long.class || to == float.class || to == double.class;
		}
		if (from == short.class) {
			return to == int.class || to == long.class || to == float.class || to == double.class;
		}
		if (from == char.class) {
			return to == int.class || to == long.class || to == float.class || to == double.class;
		}
		if (from == int.class) {
			return to == long.class || to == float.class || to == double.class;
		}
		if (from == long.class) {
			return to == float.class || to == double.class;
		}
		if (from == float.class) {
			return to == double.class;
		}
		return false;
	}

	public static String removeGetterPrefix(String name) {
		if (name.startsWith("get")) return name.substring(3);
		if (name.startsWith("is")) return name.substring(2);
		return name;
	}

	public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class[] args) throws NoSuchMethodException, IOException {
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		try {
			return di.getClazz(clazz, false).getDeclaredMethod(methodName, args, true);
		}
		catch (IOException e) {
			return di.getClazz(clazz, false).getDeclaredMethod(methodName, args, false);
		}
	}

	public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class[] args, Method defaultValue) {
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		try {
			return di.getClazz(clazz, false).getDeclaredMethod(methodName, args, true);
		}
		catch (IOException e) {
			try {
				return di.getClazz(clazz, false).getDeclaredMethod(methodName, args, false);
			}
			catch (Exception e1) {
				return defaultValue;
			}
		}
		catch (NoSuchMethodException e) {
			return defaultValue;
		}
	}

	public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] args, boolean nameCaseSensitive) throws NoSuchMethodException, IOException {
		return DynamicInvoker.getExistingInstance().getClazz(clazz, false).getMethod(methodName, args, nameCaseSensitive);
	}

	public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] args, boolean nameCaseSensitive, Method defaultValue) {
		return DynamicInvoker.getExistingInstance().getClazz(clazz, false).getMethod(methodName, args, nameCaseSensitive, defaultValue);
	}

	public static Constructor getConstructor(Class clazz, Class[] args, Constructor defaultValue) {
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		Clazz clazzz = di.getClazz(clazz, false);
		try {
			outer: for (Constructor c: clazzz.getConstructors(-1)) {
				Class[] params = c.getArgumentClasses();
				if (params.length != args.length) continue;
				for (int i = 0; i < params.length; i++) {
					if (!isInstaneOf(args[i], params[i], true)) continue outer;
				}
				return c;
			}
		}
		catch (Exception e) {}
		return defaultValue;
	}

	public static Object[] cleanArgs(Object[] args) {
		if (args == null) {
			return new Object[0];
		}

		boolean digg = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof ObjectWrap) {
				args[i] = ((ObjectWrap) args[i]).getEmbededObject(args[i]);
			}
			else if (args[i] instanceof Collection) {
				digg = ((Collection) args[i]).size() > 0;
				if (digg) break;
			}
		}

		if (!digg) return args;

		ObjectIdentityHashSet done = new ObjectIdentityHashSet();
		for (int i = 0; i < args.length; i++) {
			args[i] = _clean(done, args[i]);
		}
		return args;
	}

	private static Object _clean(ObjectIdentityHashSet done, Object obj) {
		if (done.contains(obj)) return obj;
		done.add(obj);
		try {
			if (obj instanceof ObjectWrap) {

				return ((ObjectWrap) obj).getEmbededObject(obj);
			}
			if (obj instanceof Collection) return _clean(done, (Collection) obj);
			// if (obj instanceof Map) return _clean(done, (Map) obj);
			// if (obj instanceof List) return _clean(done, (List) obj);
			// if (obj instanceof Object[]) return _clean(done, (Object[]) obj);
		}
		finally {
			done.remove(obj);
		}
		return obj;
	}

	private static Object _clean(ObjectIdentityHashSet done, Collection coll) {
		Iterator<Object> vit = coll.valueIterator();
		Object v;
		boolean change = false;
		while (vit.hasNext()) {
			v = vit.next();
			if (v != _clean(done, v)) {
				change = true;
				break;
			}
		}
		if (!change) return coll;

		coll = coll.duplicate(false);
		Iterator<Entry<Key, Object>> eit = coll.entryIterator();
		Entry<Key, Object> e;
		while (eit.hasNext()) {
			e = eit.next();
			coll.setEL(e.getKey(), _clean(done, e.getValue()));
		}
		return coll;
	}

}