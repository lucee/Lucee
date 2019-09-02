/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.commons.lang.types.RefInteger;
import lucee.runtime.PageContext;
import lucee.runtime.config.Identification;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Collection;

public interface ClassUtil {

	/**
	 * loads Class that match given classname, this Class can be from the Lucee core as well
	 * 
	 * @param className name of the Class to load
	 * @throws IOException
	 */
	public Class<?> loadClass(String className) throws IOException;

	/**
	 * loads Class that match given classname and the given bundle name and version, this Class can be
	 * from the Lucee core as well
	 * 
	 * @param className name of the Class to load
	 * @param bundleName name of the bundle to load from
	 * @param bundleVersion version of the bundle to load from (if null ignored)
	 * @throws BundleException
	 * @throws IOException
	 */
	public Class<?> loadClass(PageContext pc, String className, String bundleName, String bundleVersion) throws BundleException, IOException;

	public BIF loadBIF(PageContext pc, String name) throws InstantiationException, IllegalAccessException;

	/**
	 * check if Class is instanceof another Class
	 * 
	 * @param srcClassName class name to check
	 * @param trg class to check
	 * @return is instance of
	 */
	public boolean isInstaneOf(String srcClassName, Class<?> trg);

	/**
	 * @param srcClassName class name to check
	 * @param trgClassName class name to check
	 * @return is instance of
	 */
	public boolean isInstaneOf(String srcClassName, String trgClassName);

	/**
	 * check if Class is instanceof another Class
	 * 
	 * @param src is Class of?
	 * @param trgClassName Class name to check
	 * @return is Class Class of...
	 */
	public boolean isInstaneOf(Class<?> src, String trgClassName);

	public boolean isInstaneOfIgnoreCase(Class<?> src, String trg);

	/**
	 * check if Class is instanceof another Class
	 * 
	 * @param src Class to check
	 * @param trg is Class of ?
	 * @return is Class Class of...
	 */
	public boolean isInstaneOf(Class<?> src, Class<?> trg);

	/**
	 * get all Classes from an Object Array
	 * 
	 * @param objs Objects to get
	 * @return classes from Objects
	 */
	public Class<?>[] getClasses(Object[] objs);

	/**
	 * convert a primitive Class Type to a Reference Type (Example: int to java.lang.Integer)
	 * 
	 * @param c Class to convert
	 * @return converted Class (if primitive)
	 */
	public Class<?> toReferenceClass(Class<?> c);

	/**
	 * checks if src Class is "like" trg class
	 * 
	 * @param src Source Class
	 * @param trg Target Class
	 * @return is similar
	 */
	public boolean like(Class<?> src, Class<?> trg);

	/**
	 * convert Object from src to trg Type, if possible
	 * 
	 * @param src Object to convert
	 * @param trgClass Target Class
	 * @param rating
	 * @return converted Object
	 * @throws PageException
	 */
	public Object convert(Object src, Class<?> trgClass, RefInteger rating) throws PageException;

	/**
	 * same like method getField from Class but ignore case from field name
	 * 
	 * @param clazz Class to search the field
	 * @param name name to search
	 * @return Matching Field
	 * @throws NoSuchFieldException
	 */
	public Field[] getFieldsIgnoreCase(Class<?> clazz, String name) throws NoSuchFieldException;

	public Field[] getFieldsIgnoreCase(Class<?> clazz, String name, Field[] defaultValue);

	public String[] getPropertyKeys(Class<?> clazz);

	public boolean hasPropertyIgnoreCase(Class<?> clazz, String name);

	public boolean hasFieldIgnoreCase(Class<?> clazz, String name);

	/**
	 * call constructor of a Class with matching arguments
	 * 
	 * @param clazz Class to get Instance
	 * @param args Arguments for the Class
	 * @return invoked Instance
	 * @throws PageException
	 */
	public Object callConstructor(Class<?> clazz, Object[] args) throws PageException;

	public Object callConstructor(Class<?> clazz, Object[] args, Object defaultValue);

	/**
	 * calls a Method of an Object
	 * 
	 * @param obj Object to call Method on it
	 * @param methodName Name of the Method to get
	 * @param args Arguments of the Method to get
	 * @return return return value of the called Method
	 * @throws PageException
	 */
	public Object callMethod(Object obj, Collection.Key methodName, Object[] args) throws PageException;

	public Object callMethod(Object obj, Collection.Key methodName, Object[] args, Object defaultValue);

	/**
	 * calls a Static Method on the given CLass
	 * 
	 * @param clazz Class to call Method on it
	 * @param methodName Name of the Method to get
	 * @param args Arguments of the Method to get
	 * @return return return value of the called Method
	 * @throws PageException
	 */
	public Object callStaticMethod(Class<?> clazz, String methodName, Object[] args) throws PageException;

	/**
	 * to get a visible Field of an object
	 * 
	 * @param obj Object to invoke
	 * @param prop property to call
	 * @return property value
	 * @throws PageException
	 */
	public Object getField(Object obj, String prop) throws PageException;

	public Object getField(Object obj, String prop, Object defaultValue);

	/**
	 * assign a value to a visible Field of an object
	 * 
	 * @param obj Object to assign value to his property
	 * @param prop name of property
	 * @param value Value to assign
	 * @throws PageException
	 */
	public boolean setField(Object obj, String prop, Object value) throws PageException;

	/**
	 * to get a visible Property (Field or Getter) of an object
	 * 
	 * @param obj Object to invoke
	 * @param prop property to call
	 * @return property value
	 * @throws PageException
	 */
	public Object getProperty(Object obj, String prop) throws PageException;

	/**
	 * to get a visible Property (Field or Getter) of an object
	 * 
	 * @param obj Object to invoke
	 * @param prop property to call
	 * @return property value
	 */
	public Object getProperty(Object obj, String prop, Object defaultValue);

	/**
	 * assign a value to a visible Property (Field or Setter) of an object
	 * 
	 * @param obj Object to assign value to his property
	 * @param prop name of property
	 * @param value Value to assign
	 * @throws PageException
	 */
	public void setProperty(Object obj, String prop, Object value) throws PageException;

	/**
	 * assign a value to a visible Property (Field or Setter) of an object
	 * 
	 * @param obj Object to assign value to his property
	 * @param prop name of property
	 * @param value Value to assign
	 */
	public void setPropertyEL(Object obj, String prop, Object value);

	/**
	 * return all methods that are defined by the Class itself (not extended)
	 * 
	 * @param clazz
	 * @return
	 */
	public Method[] getDeclaredMethods(Class<?> clazz);

	/**
	 * check if given Class "from" can be converted to Class "to" without explicit casting
	 * 
	 * @param from source class
	 * @param to target class
	 * @return is it possible to convert from "from" to "to"
	 */
	public boolean canConvert(Class<?> from, Class<?> to);

	public Class<?> loadClassByBundle(String className, String name, String strVersion, Identification id) throws IOException, BundleException;

	public Class<?> loadClassByBundle(String className, String name, Version version, Identification id) throws BundleException, IOException;

	/**
	 * loads a Class from a String classname
	 * 
	 * @param className
	 * @param defaultValue
	 * @return matching Class
	 */
	public Class<?> loadClass(String className, Class<?> defaultValue);

	/**
	 * loads a Class from a specified Classloader with given classname
	 * 
	 * @param className
	 * @param cl
	 * @return matching Class
	 */
	public Class<?> loadClass(ClassLoader cl, String className, Class<?> defaultValue);

	/**
	 * loads a Class from a specified Classloader with given classname
	 * 
	 * @param className
	 * @param cl
	 * @return matching Class
	 * @throws IOException
	 */
	public Class<?> loadClass(ClassLoader cl, String className) throws IOException;

	/**
	 * loads a Class from a String classname
	 * 
	 * @param clazz Class to load
	 * @return matching Class
	 * @throws IOException
	 */
	public Object loadInstance(Class<?> clazz) throws IOException;

	public Object loadInstance(String className) throws IOException;

	public Object loadInstance(ClassLoader cl, String className) throws IOException;

	/**
	 * loads a Class from a String classname
	 * 
	 * @param clazz Class to load
	 * @return matching Class
	 */
	public Object loadInstance(Class<?> clazz, Object defaultValue);

	public Object loadInstance(String className, Object defaultValue);

	public Object loadInstance(ClassLoader cl, String className, Object defaultValue);

	/**
	 * loads a Class from a String classname
	 * 
	 * @param clazz Class to load
	 * @param args
	 * @return matching Class
	 * @throws IOException
	 * @throws InvocationTargetException
	 */
	public Object loadInstance(Class<?> clazz, Object[] args) throws IOException, InvocationTargetException;

	public Object loadInstance(String className, Object[] args) throws IOException, InvocationTargetException;

	public Object loadInstance(ClassLoader cl, String className, Object[] args) throws IOException, InvocationTargetException;

	/**
	 * loads a Class from a String classname
	 * 
	 * @param clazz Class to load
	 * @param args
	 * @return matching Class
	 */
	public Object loadInstance(Class<?> clazz, Object[] args, Object defaultValue);

	public Object loadInstance(String className, Object[] args, Object defaultValue);

	public Object loadInstance(ClassLoader cl, String className, Object[] args, Object defaultValue);

	/**
	 * check if given stream is a bytecode stream, if yes remove bytecode mark
	 * 
	 * @param is
	 * @return is bytecode stream
	 * @throws IOException
	 */
	public boolean isBytecode(InputStream is) throws IOException;

	public boolean isBytecode(byte[] barr);

	public String getName(Class<?> clazz);

	public Method getMethodIgnoreCase(Class<?> clazz, String methodName, Class<?>[] args) throws IOException;

	public Method getMethodIgnoreCase(Class<?> clazz, String methodName, Class<?>[] args, Method defaultValue);

	/**
	 * return all field names as String array
	 * 
	 * @param clazz Class to get field names from
	 * @return field names
	 */
	public String[] getFieldNames(Class<?> clazz);

	public byte[] toBytes(Class<?> clazz) throws IOException;

	/**
	 * return an array Class based on the given Class (opposite from Class.getComponentType())
	 * 
	 * @param clazz
	 * @return
	 */
	public Class<?> toArrayClass(Class<?> clazz);

	public Class<?> toComponentType(Class<?> clazz);

	/**
	 * returns the path to the directory or jar file that the Class was loaded from
	 * 
	 * @param clazz - the Class object to check, for a live object pass obj.getClass();
	 * @param defaultValue - a value to return in case the source could not be determined
	 * @return
	 */
	public String getSourcePathForClass(Class<?> clazz, String defaultValue);

	/**
	 * tries to load the Class and returns the path that it was loaded from
	 * 
	 * @param className - the name of the Class to check
	 * @param defaultValue - a value to return in case the source could not be determined
	 * @return
	 */
	public String getSourcePathForClass(String className, String defaultValue);

	/**
	 * extracts the package from a className, return null, if there is none.
	 * 
	 * @param className
	 * @return
	 */
	public String extractPackage(String className);

	/**
	 * extracts the Class name of a classname with package
	 * 
	 * @param className
	 * @return
	 */
	public String extractName(String className);

	public void start(Bundle bundle) throws BundleException;

	public Bundle addBundle(BundleContext context, InputStream is, boolean closeStream, boolean checkExistence) throws BundleException, IOException;

}