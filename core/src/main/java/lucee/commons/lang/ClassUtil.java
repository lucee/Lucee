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
package lucee.commons.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.commons.collection.MapFactory;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceClassLoader;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.Identification;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ListUtil;

public final class ClassUtil {

	/**
	 * @param className
	 * @return
	 * @throws ClassException
	 * @throws PageException
	 */
	public static Class toClass(String className) throws ClassException {
		return loadClass(className);
	}

	private static Class checkPrimaryTypesBytecodeDef(String className, Class defaultValue) {
		if (className.charAt(0) == '[') {
			if (className.equals("[V")) return void.class;
			if (className.equals("[Z")) return boolean.class;
			if (className.equals("[B")) return byte.class;
			if (className.equals("[I")) return int.class;
			if (className.equals("[J")) return long.class;
			if (className.equals("[F")) return float.class;
			if (className.equals("[D")) return double.class;
			if (className.equals("[C")) return char.class;
			if (className.equals("[S")) return short.class;
		}
		return defaultValue;
	}

	private static Class checkPrimaryTypes(String className, Class defaultValue) {
		Class res = checkPrimaryTypesBytecodeDef(className, null);
		if (res != null) return res;

		String lcClassName = className.toLowerCase();
		boolean isRef = false;
		if (lcClassName.startsWith("java.lang.")) {
			lcClassName = lcClassName.substring(10);
			isRef = true;
		}

		if (lcClassName.equals("void")) {
			return void.class;
		}
		if (lcClassName.equals("boolean")) {
			if (isRef) return Boolean.class;
			return boolean.class;
		}
		if (lcClassName.equals("byte")) {
			if (isRef) return Byte.class;
			return byte.class;
		}
		if (lcClassName.equals("int")) {
			return int.class;
		}
		if (lcClassName.equals("long")) {
			if (isRef) return Long.class;
			return long.class;
		}
		if (lcClassName.equals("float")) {
			if (isRef) return Float.class;
			return float.class;
		}
		if (lcClassName.equals("double")) {
			if (isRef) return Double.class;
			return double.class;
		}
		if (lcClassName.equals("char")) {
			return char.class;
		}
		if (lcClassName.equals("short")) {
			if (isRef) return Short.class;
			return short.class;
		}

		if (lcClassName.equals("integer")) return Integer.class;
		if (lcClassName.equals("character")) return Character.class;
		if (lcClassName.equals("object")) return Object.class;
		if (lcClassName.equals("string")) return String.class;
		if (lcClassName.equals("null")) return Object.class;
		if (lcClassName.equals("numeric")) return Double.class;

		return defaultValue;
	}

	public static Class<?> loadClassByBundle(String className, String name, String strVersion, Identification id, List<Resource> addional) throws ClassException, BundleException {
		// version
		Version version = null;
		if (!StringUtil.isEmpty(strVersion, true)) {
			version = OSGiUtil.toVersion(strVersion.trim(), null);
			if (version == null) throw new ClassException("Version definition [" + strVersion + "] is invalid.");
		}
		return loadClassByBundle(className, new BundleDefinition(name, version), null, id, addional);
	}

	public static Class loadClassByBundle(String className, String name, Version version, Identification id, List<Resource> addional) throws BundleException, ClassException {
		return loadClassByBundle(className, new BundleDefinition(name, version), null, id, addional);
	}

	public static Class<?> loadClassByBundle(String className, BundleDefinition bundle, BundleDefinition[] relatedBundles, Identification id, List<Resource> addional)
			throws BundleException, ClassException {
		try {
			if (relatedBundles != null) {
				for (BundleDefinition rb: relatedBundles) {
					rb.getBundle(id, addional, true);
				}
			}
			return bundle.getBundle(id, addional, true).loadClass(className);
		}
		catch (ClassNotFoundException e) {
			String appendix = "";
			if (!StringUtil.isEmpty(e.getMessage(), true)) appendix = " " + e.getMessage();
			if (bundle.getVersion() == null)
				throw new ClassException("In the OSGi Bundle with the name [" + bundle.getName() + "] was no class with name [" + className + "] found." + appendix);
			throw new ClassException("In the OSGi Bundle with the name [" + bundle.getName() + "] and the version [" + bundle.getVersion() + "] was no class with name ["
					+ className + "] found." + appendix);
		}
	}

	/**
	 * loads a class from a String classname
	 * 
	 * @param className
	 * @param defaultValue
	 * @return matching Class
	 */
	public static Class loadClass(String className, Class defaultValue) {
		// OSGI env
		Class clazz = _loadClass(new OSGiBasedClassLoading(), className, null, null);
		if (clazz != null) return clazz;

		// core classloader
		clazz = _loadClass(new ClassLoaderBasedClassLoading(SystemUtil.getCoreClassLoader()), className, null, null);
		if (clazz != null) return clazz;

		// loader classloader
		clazz = _loadClass(new ClassLoaderBasedClassLoading(SystemUtil.getLoaderClassLoader()), className, null, null);
		if (clazz != null) return clazz;

		return defaultValue;
	}

	/**
	 * loads a class from a String classname
	 * 
	 * @param className
	 * @return matching Class
	 * @throws ClassException
	 */
	public static Class loadClass(String className) throws ClassException {
		Set<Throwable> exceptions = new HashSet<Throwable>();
		// OSGI env
		Class clazz = _loadClass(new OSGiBasedClassLoading(), className, null, exceptions);
		if (clazz != null) {
			return clazz;
		}

		// core classloader
		clazz = _loadClass(new ClassLoaderBasedClassLoading(SystemUtil.getCoreClassLoader()), className, null, exceptions);
		if (clazz != null) {
			return clazz;
		}

		// loader classloader
		clazz = _loadClass(new ClassLoaderBasedClassLoading(SystemUtil.getLoaderClassLoader()), className, null, exceptions);
		if (clazz != null) {
			return clazz;
		}

		String msg = "cannot load class through its string name, because no definition for the class with the specified name [" + className + "] could be found";
		if (exceptions.size() > 0) {
			StringBuilder detail = new StringBuilder();
			Iterator<Throwable> it = exceptions.iterator();
			Throwable t;
			while (it.hasNext()) {
				t = it.next();
				detail.append(t.getClass().getName()).append(':').append(t.getMessage()).append(';');
			}
			throw new ClassException(msg + " caused by (" + detail.toString() + ")");
		}
		throw new ClassException(msg);
	}

	public static Class loadClass(ClassLoader cl, String className, Class defaultValue) {
		return loadClass(cl, className, defaultValue, null);
	}

	private static Class loadClass(ClassLoader cl, String className, Class defaultValue, Set<Throwable> exceptions) {

		if (cl != null) {
			// TODO do not produce a resource classloader in the first place if there are no resources
			if (cl instanceof ResourceClassLoader && ((ResourceClassLoader) cl).isEmpty()) {
				ClassLoader p = ((ResourceClassLoader) cl).getParent();
				if (p != null) cl = p;
			}
			Class clazz = _loadClass(new ClassLoaderBasedClassLoading(cl), className, defaultValue, exceptions);
			if (clazz != null) return clazz;
		}

		// OSGI env
		Class clazz = _loadClass(new OSGiBasedClassLoading(), className, null, exceptions);
		if (clazz != null) return clazz;

		// core classloader
		if (cl != SystemUtil.getCoreClassLoader()) {
			clazz = _loadClass(new ClassLoaderBasedClassLoading(SystemUtil.getCoreClassLoader()), className, null, exceptions);
			if (clazz != null) return clazz;
		}

		// loader classloader
		if (cl != SystemUtil.getLoaderClassLoader()) {
			clazz = _loadClass(new ClassLoaderBasedClassLoading(SystemUtil.getLoaderClassLoader()), className, null, exceptions);
			if (clazz != null) return clazz;
		}

		return defaultValue;
	}

	/**
	 * loads a class from a specified Classloader with given classname
	 * 
	 * @param className
	 * @param cl
	 * @return matching Class
	 * @throws ClassException
	 */
	public static Class loadClass(ClassLoader cl, String className) throws ClassException {

		Set<Throwable> exceptions = new HashSet<Throwable>();
		Class clazz = loadClass(cl, className, null, exceptions);

		if (clazz != null) return clazz;

		String msg = "cannot load class through its string name, because no definition for the class with the specified name [" + className + "] could be found";

		if (!exceptions.isEmpty()) {

			StringBuilder detail = new StringBuilder();
			Iterator<Throwable> it = exceptions.iterator();
			Throwable t;
			while (it.hasNext()) {
				t = it.next();
				detail.append(t.getClass().getName()).append(':').append(t.getMessage()).append(';');
			}
			throw new ClassException(msg + " caused by (" + detail.toString() + ")");
		}
		throw new ClassException(msg);
	}

	/**
	 * loads a class from a specified Classloader with given classname
	 * 
	 * @param className
	 * @param cl
	 * @return matching Class
	 */
	private static Class _loadClass(ClassLoading cl, String className, Class defaultValue, Set<Throwable> exceptions) {
		className = className.trim();
		if (StringUtil.isEmpty(className)) return defaultValue;

		Class clazz = checkPrimaryTypesBytecodeDef(className, null);
		if (clazz != null) return clazz;

		// array in the format boolean[] or java.lang.String[]
		if (className.endsWith("[]")) {

			StringBuilder pureCN = new StringBuilder(className);
			int dimensions = 0;
			do {
				pureCN.delete(pureCN.length() - 2, pureCN.length());
				dimensions++;
			}
			while (pureCN.lastIndexOf("[]") == pureCN.length() - 2);

			clazz = __loadClass(cl, pureCN.toString(), null, exceptions);

			if (clazz != null) {

				for (int i = 0; i < dimensions; i++)
					clazz = toArrayClass(clazz);
				return clazz;
			}
		}

		// array in the format [C or [Ljava.lang.String;
		else if (className.charAt(0) == '[') {

			StringBuilder pureCN = new StringBuilder(className);
			int dimensions = 0;

			do {
				pureCN.delete(0, 1);
				dimensions++;
			}
			while (pureCN.charAt(0) == '[');

			clazz = __loadClass(cl, pureCN.toString(), null, exceptions);
			if (clazz != null) {
				for (int i = 0; i < dimensions; i++)
					clazz = toArrayClass(clazz);
				return clazz;
			}
		}
		return __loadClass(cl, className, defaultValue, exceptions);
	}

	private static Class<?> __loadClass(ClassLoading cl, String className, Class<?> defaultValue, Set<Throwable> exceptions) {

		Class<?> clazz = checkPrimaryTypes(className, null);
		if (clazz != null) return clazz;

		// class in format Ljava.lang.String;
		if (className.charAt(0) == 'L' && className.endsWith(";")) {
			className = className.substring(1, className.length() - 1).replace('/', '.');
			clazz = cl.loadClass(className, null, exceptions);
			if (clazz != null) return clazz;
			return defaultValue;
		}

		clazz = cl.loadClass(className, null, exceptions);
		if (clazz != null) return clazz;
		return defaultValue;
	}

	/**
	 * loads a class from a String classname
	 * 
	 * @param clazz class to load
	 * @return matching Class
	 * @throws ClassException
	 */
	public static Object loadInstance(Class clazz) throws ClassException {
		try {
			return newInstance(clazz);
		}
		catch (InstantiationException e) {
			throw new ClassException("the specified class object [" + clazz.getName() + "()] cannot be instantiated");
		}
		catch (IllegalAccessException e) {
			throw new ClassException(
					"can't load class [" + clazz.getName() + "] because the currently executing method does not have access to the definition of the specified class");
		}
		catch (Exception e) {
			String message = "";
			if (e.getMessage() != null) {
				message = e.getMessage() + " ";
			}
			message += e.getClass().getName() + " while creating an instance of " + clazz.getName();
			ClassException ce = new ClassException(message);
			ce.setStackTrace(e.getStackTrace());
			throw ce;
		}
	}

	public static Object loadInstance(String className) throws ClassException {
		return loadInstance(loadClass(className));
	}

	public static Object loadInstance(ClassLoader cl, String className) throws ClassException {
		return loadInstance(loadClass(cl, className));
	}

	/**
	 * loads a class from a String classname
	 * 
	 * @param clazz class to load
	 * @return matching Class
	 */
	public static Object loadInstance(Class clazz, Object defaultValue) {
		try {
			return newInstance(clazz);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static Object loadInstance(String className, Object defaultValue) {
		Class clazz = loadClass(className, null);
		if (clazz == null) return defaultValue;
		return loadInstance(clazz, defaultValue);
	}

	public static Object loadInstance(ClassLoader cl, String className, Object defaultValue) {
		Class clazz = loadClass(cl, className, null);
		if (clazz == null) return defaultValue;
		return loadInstance(clazz, defaultValue);
	}

	/**
	 * loads a class from a String classname
	 * 
	 * @param clazz class to load
	 * @param args
	 * @return matching Class
	 * @throws ClassException
	 * @throws ClassException
	 * @throws InvocationTargetException
	 */
	public static Object loadInstance(Class clazz, Object[] args) throws ClassException, InvocationTargetException {
		if (args == null || args.length == 0) return loadInstance(clazz);

		Class[] cArgs = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			cArgs[i] = args[i].getClass();
		}

		try {
			Constructor c = clazz.getConstructor(cArgs);
			return c.newInstance(args);

		}
		catch (SecurityException e) {
			throw new ClassException("there is a security violation (thrown by security manager)");
		}
		catch (NoSuchMethodException e) {

			StringBuilder sb = new StringBuilder(clazz.getName());
			char del = '(';
			for (int i = 0; i < cArgs.length; i++) {
				sb.append(del);
				sb.append(cArgs[i].getName());
				del = ',';
			}
			sb.append(')');

			throw new ClassException("there is no constructor with the [" + sb + "] signature for the class [" + clazz.getName() + "]");
		}
		catch (IllegalArgumentException e) {
			throw new ClassException("has been passed an illegal or inappropriate argument");
		}
		catch (InstantiationException e) {
			throw new ClassException("the specified class object [" + clazz.getName() + "] cannot be instantiated because it is an interface or is an abstract class");
		}
		catch (IllegalAccessException e) {
			throw new ClassException("can't load class because the currently executing method does not have access to the definition of the specified class");
		}
	}

	public static Object loadInstance(String className, Object[] args) throws ClassException, InvocationTargetException {
		return loadInstance(loadClass(className), args);
	}

	public static Object loadInstance(ClassLoader cl, String className, Object[] args) throws ClassException, InvocationTargetException {
		return loadInstance(loadClass(cl, className), args);
	}

	/**
	 * loads a class from a String classname
	 * 
	 * @param clazz class to load
	 * @param args
	 * @return matching Class
	 */
	public static Object loadInstance(Class clazz, Object[] args, Object defaultValue) {
		if (args == null || args.length == 0) return loadInstance(clazz, defaultValue);
		try {
			Class[] cArgs = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				if (args[i] == null) cArgs[i] = Object.class;
				else cArgs[i] = args[i].getClass();
			}
			Constructor c = clazz.getConstructor(cArgs);
			return c.newInstance(args);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}

	}

	public static Object loadInstance(String className, Object[] args, Object defaultValue) {
		Class clazz = loadClass(className, null);
		if (clazz == null) return defaultValue;
		return loadInstance(clazz, args, defaultValue);
	}

	public static Object loadInstance(ClassLoader cl, String className, Object[] args, Object defaultValue) {
		Class clazz = loadClass(cl, className, null);
		if (clazz == null) return defaultValue;
		return loadInstance(clazz, args, defaultValue);
	}

	/**
	 * @return returns a string array of all pathes in classpath
	 */
	public static String[] getClassPath(Config config) {

		Map<String, String> pathes = MapFactory.<String, String>getConcurrentMap();
		String pathSeperator = System.getProperty("path.separator");
		if (pathSeperator == null) pathSeperator = ";";

		// pathes from system properties
		String strPathes = System.getProperty("java.class.path");
		if (strPathes != null) {
			Array arr = ListUtil.listToArrayRemoveEmpty(strPathes, pathSeperator);
			int len = arr.size();
			for (int i = 1; i <= len; i++) {
				File file = FileUtil.toFile(Caster.toString(arr.get(i, ""), "").trim());
				if (file.exists()) try {
					pathes.put(file.getCanonicalPath(), "");
				}
				catch (IOException e) {
				}
			}
		}

		// pathes from url class Loader (dynamic loaded classes)
		getClassPathesFromLoader(new ClassUtil().getClass().getClassLoader(), pathes);
		getClassPathesFromLoader(config.getClassLoader(), pathes);

		Set<String> set = pathes.keySet();
		return set.toArray(new String[set.size()]);
	}

	/**
	 * get class pathes from all url ClassLoaders
	 * 
	 * @param cl URL Class Loader
	 * @param pathes Hashmap with allpathes
	 */
	private static void getClassPathesFromLoader(ClassLoader cl, Map pathes) {
		if (cl instanceof URLClassLoader) _getClassPathesFromLoader((URLClassLoader) cl, pathes);
	}

	private static void _getClassPathesFromLoader(URLClassLoader ucl, Map pathes) {
		getClassPathesFromLoader(ucl.getParent(), pathes);

		// get all pathes
		URL[] urls = ucl.getURLs();

		for (int i = 0; i < urls.length; i++) {
			File file = FileUtil.toFile(urls[i].getPath());
			if (file.exists()) try {
				pathes.put(file.getCanonicalPath(), "");
			}
			catch (IOException e) {
			}
		}
	}

	// CafeBabe (Java Magic Number)
	private static final int ICA = 202;// CA
	private static final int IFE = 254;// FE
	private static final int IBA = 186;// BA
	private static final int IBE = 190;// BE

	// CF33 (Lucee Magic Number)
	private static final int ICF = 207;// CF
	private static final int I33 = 51;// 33

	private static final byte BCA = (byte) ICA;// CA
	private static final byte BFE = (byte) IFE;// FE
	private static final byte BBA = (byte) IBA;// BA
	private static final byte BBE = (byte) IBE;// BE

	private static final byte BCF = (byte) ICF;// CF
	private static final byte B33 = (byte) I33;// 33
	private static final Class[] EMPTY_CLASS = new Class[0];
	private static final Object[] EMPTY_OBJ = new Object[0];

	/**
	 * check if given stream is a bytecode stream, if yes remove bytecode mark
	 * 
	 * @param is
	 * @return is bytecode stream
	 * @throws IOException
	 */
	public static boolean isBytecode(InputStream is) throws IOException {
		if (!is.markSupported()) throw new IOException("can only read input streams that support mark/reset");
		is.mark(-1);
		// print(bytes);
		int first = is.read();
		int second = is.read();
		boolean rtn = (first == ICA && second == IFE && is.read() == IBA && is.read() == IBE);

		is.reset();
		return rtn;
	}

	public static boolean isEncryptedBytecode(InputStream is) throws IOException {
		if (!is.markSupported()) throw new IOException("can only read input streams that support mark/reset");
		is.mark(-1);
		// print(bytes);
		int first = is.read();
		int second = is.read();
		boolean rtn = (first == ICF && second == I33);

		is.reset();
		return rtn;
	}

	public static boolean isBytecode(byte[] barr) {
		if (barr.length < 4) return false;
		return (barr[0] == BCF && barr[1] == B33) || (barr[0] == BCA && barr[1] == BFE && barr[2] == BBA && barr[3] == BBE);
	}

	public static boolean isRawBytecode(byte[] barr) {
		if (barr.length < 4) return false;
		return (barr[0] == BCA && barr[1] == BFE && barr[2] == BBA && barr[3] == BBE);
	}

	public static boolean hasCF33Prefix(byte[] barr) {
		if (barr.length < 4) return false;
		return (barr[0] == BCF && barr[1] == B33);
	}

	public static byte[] removeCF33Prefix(byte[] barr) {
		if (!hasCF33Prefix(barr)) return barr;

		byte[] dest = new byte[barr.length - 10];
		System.arraycopy(barr, 10, dest, 0, 10);
		return dest;
	}

	public static String getName(Class clazz) {
		if (clazz.isArray()) {
			return getName(clazz.getComponentType()) + "[]";
		}

		return clazz.getName();
	}

	public static Method getMethodIgnoreCase(Class clazz, String methodName, Class[] args, Method defaultValue) {
		Method[] methods = clazz.getMethods();
		Method method;
		Class[] params;
		outer: for (int i = 0; i < methods.length; i++) {
			method = methods[i];
			if (method.getName().equalsIgnoreCase(methodName)) {
				params = method.getParameterTypes();
				if (params.length == args.length) {
					for (int y = 0; y < params.length; y++) {
						if (!params[y].equals(args[y])) {
							continue outer;
						}
					}
					return method;
				}
			}
		}

		return defaultValue;
	}

	public static Method getMethodIgnoreCase(Class clazz, String methodName, Class[] args) throws ClassException {
		Method res = getMethodIgnoreCase(clazz, methodName, args, null);
		if (res != null) return res;
		throw new ClassException("class " + clazz.getName() + " has no method with name " + methodName);
	}

	/**
	 * return all field names as String array
	 * 
	 * @param clazz class to get field names from
	 * @return field names
	 */
	public static String[] getFieldNames(Class clazz) {
		Field[] fields = clazz.getFields();
		String[] names = new String[fields.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = fields[i].getName();
		}
		return names;
	}

	public static byte[] toBytes(Class clazz) throws IOException {
		return IOUtil.toBytes(clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class"), true);
	}

	/**
	 * return an array class based on the given class (opposite from Class.getComponentType())
	 * 
	 * @param clazz
	 * @return
	 */
	public static Class toArrayClass(Class clazz) {
		return java.lang.reflect.Array.newInstance(clazz, 0).getClass();
	}

	public static Class<?> toComponentType(Class<?> clazz) {
		Class<?> tmp;
		while (true) {
			tmp = clazz.getComponentType();
			if (tmp == null) break;
			clazz = tmp;
		}
		return clazz;
	}

	/**
	 * returns the path to the directory or jar file that the class was loaded from
	 *
	 * @param clazz - the Class object to check, for a live object pass obj.getClass();
	 * @param defaultValue - a value to return in case the source could not be determined
	 * @return
	 */
	public static String getSourcePathForClass(Class clazz, String defaultValue) {

		try {

			String result = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
			result = URLDecoder.decode(result, CharsetUtil.UTF8.name());
			result = SystemUtil.fixWindowsPath(result);
			return result;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		return defaultValue;
	}

	public static byte[] getBytesForClass(Class clazz, byte[] defaultValue) {
		InputStream is = null;
		try {
			ClassLoader cl = clazz.getClassLoader();
			if (cl == null) cl = ClassLoader.getSystemClassLoader();
			is = cl.getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
			return IOUtil.toBytes(is);
		}
		catch (Exception e) {
		}

		is = null;
		ZipFile zf = null;
		try {
			String path = getSourcePathForClass(clazz, null);
			if (path == null) return defaultValue;

			File file = new File(path);
			// zip
			if (file.isFile()) {
				zf = new ZipFile(file);
				ZipEntry ze = zf.getEntry(clazz.getName().replace('.', '/') + ".class");
				if (ze == null) ze = zf.getEntry(clazz.getName().replace('.', '\\') + ".class");
				is = zf.getInputStream(ze);
				return IOUtil.toBytes(is);
			}
			// directory
			else if (file.isDirectory()) {
				File f = new File(file, clazz.getName().replace('.', '/') + ".class");
				if (!f.isFile()) f = new File(file, clazz.getName().replace('.', '\\') + ".class");
				if (f.isFile()) return IOUtil.toBytes(f);
			}
		}
		catch (Exception e) {
		}
		finally {
			IOUtil.closeEL(is);
			IOUtil.closeELL(zf);
		}

		return defaultValue;
	}

	/**
	 * tries to load the class and returns the path that it was loaded from
	 *
	 * @param className - the name of the class to check
	 * @param defaultValue - a value to return in case the source could not be determined
	 * @return
	 */
	public static String getSourcePathForClass(String className, String defaultValue) {

		try {

			return getSourcePathForClass(ClassUtil.loadClass(className), defaultValue);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		return defaultValue;
	}

	/**
	 * extracts the package from a className, return null, if there is none.
	 * 
	 * @param className
	 * @return
	 */
	public static String extractPackage(String className) {
		if (className == null) return null;
		int index = className.lastIndexOf('.');
		if (index != -1) return className.substring(0, index);
		return null;
	}

	/**
	 * extracts the class name of a classname with package
	 * 
	 * @param className
	 * @return
	 */
	public static String extractName(String className) {
		if (className == null) return null;
		int index = className.lastIndexOf('.');
		if (index != -1) return className.substring(index + 1);
		return className;
	}

	/**
	 * if no bundle is defined it is loaded the old way
	 * 
	 * @param className
	 * @param bundleName
	 * @param bundleVersion
	 * @param id
	 * @return
	 * @throws ClassException
	 * @throws BundleException
	 */
	public static Class loadClass(String className, String bundleName, String bundleVersion, Identification id, List<Resource> addional) throws ClassException, BundleException {
		if (StringUtil.isEmpty(bundleName)) return loadClass(className);
		return loadClassByBundle(className, bundleName, bundleVersion, id, addional);
	}

	private static interface ClassLoading {
		public Class<?> loadClass(String className, Class defaultValue);

		public Class<?> loadClass(String className, Class defaultValue, Set<Throwable> exceptions);
	}

	private static class ClassLoaderBasedClassLoading implements ClassLoading {
		private ClassLoader cl;

		public ClassLoaderBasedClassLoading(ClassLoader cl) {
			this.cl = cl;
		}

		@Override
		public Class<?> loadClass(String className, Class defaultValue) {
			return loadClass(className, defaultValue, null);
		}

		@Override
		public Class<?> loadClass(String className, Class defaultValue, Set<Throwable> exceptions) {
			className = className.trim();
			try {
				return cl.loadClass(className);
			}
			catch (Exception e) {
				try {
					return Class.forName(className, false, cl);
				}
				catch (Exception e2) {
					if (exceptions != null) {
						exceptions.add(e2);
					}
					return defaultValue;
				}
			}
		}

		/*
		 * @Override public Class<?> loadClass(String className) throws ClassException {
		 * className=className.trim(); try { return cl.loadClass(className); } catch(Throwable t)
		 * {ExceptionUtil.rethrowIfNecessary(t); try { return Class.forName(className, false, cl); } catch
		 * (Throwable t2) {ExceptionUtil.rethrowIfNecessary(t2); String msg=null; if(t2 instanceof
		 * ClassNotFoundException || t2 instanceof NoClassDefFoundError) {
		 * msg="["+t2.getClass().getName()+"] "+t2.getMessage(); } if(StringUtil.isEmpty(msg))
		 * msg="cannot load class through its string name, because no definition for the class with the specified name "
		 * + "["+className+"] could be found";
		 * 
		 * throw new ClassException(msg); } } }
		 */
	}

	private static class OSGiBasedClassLoading implements ClassLoading {
		@Override
		public Class<?> loadClass(String className, Class defaultValue) {
			return OSGiUtil.loadClass(className, defaultValue);
		}

		@Override
		public Class<?> loadClass(String className, Class defaultValue, Set<Throwable> exceptions) {
			return loadClass(className, defaultValue);
		}
	}

	public static ClassLoader getClassLoader(Class clazz) {
		ClassLoader cl = clazz.getClassLoader();
		if (cl != null) return cl;

		Config config = ThreadLocalPageContext.getConfig();
		if (config instanceof ConfigPro) {
			return ((ConfigPro) config).getClassLoaderCore();
		}
		return new lucee.commons.lang.ClassLoaderHelper().getClass().getClassLoader();
	}

	public static Object newInstance(Class clazz)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return clazz.getConstructor(EMPTY_CLASS).newInstance(EMPTY_OBJ);
	}
}