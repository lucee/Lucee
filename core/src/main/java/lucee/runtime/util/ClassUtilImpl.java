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

import lucee.commons.lang.ClassException;
import lucee.commons.lang.types.RefInteger;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.Identification;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.BIFProxy;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection.Key;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;

public class ClassUtilImpl implements ClassUtil {

	@Override
	public Class<?> loadClass(String className) throws ClassException {
		return lucee.commons.lang.ClassUtil.loadClass(className);
	}

	@Override
	public Class<?> loadClass(PageContext pc, String className, String bundleName, String bundleVersion) throws ClassException, BundleException {
		Config config = ThreadLocalPageContext.getConfig(pc);
		return lucee.commons.lang.ClassUtil.loadClassByBundle(className, bundleName, bundleVersion, config.getIdentification(), JavaSettingsImpl.getBundleDirectories(pc));
	}

	@Override
	public BIF loadBIF(PageContext pc, String name) throws InstantiationException, IllegalAccessException {
		// first of all we chek if itis a class
		Class<?> res = lucee.commons.lang.ClassUtil.loadClass(name, null);
		if (res != null) {
			if (Reflector.isInstaneOf(res, BIF.class, false)) {
				return (BIF) res.newInstance();
			}
			return new BIFProxy(res);
		}

		FunctionLib[] flds = ((ConfigWebImpl) pc.getConfig()).getFLDs(pc.getCurrentTemplateDialect());
		FunctionLibFunction flf;
		for (int i = 0; i < flds.length; i++) {
			flf = flds[i].getFunction(name);
			if (flf != null) return flf.getBIF();
		}
		return null;
	}

	// FUTURE add to loader
	public BIF loadBIF(PageContext pc, String name, String bundleName, Version bundleVersion)
			throws InstantiationException, IllegalAccessException, ClassException, BundleException {
		// first of all we chek if itis a class
		Class<?> res = lucee.commons.lang.ClassUtil.loadClassByBundle(name, bundleName, bundleVersion, pc.getConfig().getIdentification(),
				JavaSettingsImpl.getBundleDirectories(pc));
		if (res != null) {
			if (Reflector.isInstaneOf(res, BIF.class, false)) {
				return (BIF) res.newInstance();
			}
			return new BIFProxy(res);
		}
		return null;
	}

	@Override
	public boolean isInstaneOf(String srcClassName, Class<?> trg) {
		return Reflector.isInstaneOf(srcClassName, trg);
	}

	@Override
	public boolean isInstaneOf(String srcClassName, String trgClassName) {
		return Reflector.isInstaneOf(srcClassName, trgClassName);
	}

	@Override
	public boolean isInstaneOf(Class<?> src, String trgClassName) {
		return Reflector.isInstaneOf(src, trgClassName);
	}

	@Override
	public boolean isInstaneOfIgnoreCase(Class<?> src, String trg) {
		return Reflector.isInstaneOfIgnoreCase(src, trg);
	}

	@Override
	public boolean isInstaneOf(Class<?> src, Class<?> trg) {
		return Reflector.isInstaneOf(src, trg, true);
	}

	public boolean isInstaneOf(Class<?> src, Class<?> trg, boolean exatctMatch) { // FUTURE
		return Reflector.isInstaneOf(src, trg, exatctMatch);
	}

	@Override
	public Class<?>[] getClasses(Object[] objs) {
		return Reflector.getClasses(objs);
	}

	@Override
	public Class<?> toReferenceClass(Class<?> c) {
		return Reflector.toReferenceClass(c);
	}

	@Override
	public boolean like(Class<?> src, Class<?> trg) {
		return Reflector.like(src, trg);
	}

	@Override
	public Object convert(Object src, Class<?> trgClass, RefInteger rating) throws PageException {
		return Reflector.convert(src, trgClass, rating);
	}

	@Override
	public Field[] getFieldsIgnoreCase(Class<?> clazz, String name) throws NoSuchFieldException {
		return Reflector.getFieldsIgnoreCase(clazz, name);
	}

	@Override
	public Field[] getFieldsIgnoreCase(Class<?> clazz, String name, Field[] defaultValue) {
		return Reflector.getFieldsIgnoreCase(clazz, name, defaultValue);
	}

	@Override
	public String[] getPropertyKeys(Class<?> clazz) {
		return Reflector.getPropertyKeys(clazz);
	}

	@Override
	public boolean hasPropertyIgnoreCase(Class<?> clazz, String name) {
		return Reflector.hasPropertyIgnoreCase(clazz, name);
	}

	@Override
	public boolean hasFieldIgnoreCase(Class<?> clazz, String name) {
		return Reflector.hasFieldIgnoreCase(clazz, name);
	}

	@Override
	public Object callConstructor(Class<?> clazz, Object[] args) throws PageException {
		return Reflector.callConstructor(clazz, args);
	}

	@Override
	public Object callConstructor(Class<?> clazz, Object[] args, Object defaultValue) {
		return Reflector.callConstructor(clazz, args, defaultValue);
	}

	@Override
	public Object callMethod(Object obj, Key methodName, Object[] args) throws PageException {
		return Reflector.callMethod(obj, methodName, args);
	}

	@Override
	public Object callMethod(Object obj, Key methodName, Object[] args, Object defaultValue) {
		return Reflector.callMethod(obj, methodName, args, defaultValue);
	}

	@Override
	public Object callStaticMethod(Class<?> clazz, String methodName, Object[] args) throws PageException {
		return Reflector.callStaticMethod(clazz, methodName, args);
	}

	@Override
	public Object getField(Object obj, String prop) throws PageException {
		return Reflector.getField(obj, prop);
	}

	@Override
	public Object getField(Object obj, String prop, Object defaultValue) {
		return Reflector.getField(obj, prop, defaultValue);
	}

	@Override
	public boolean setField(Object obj, String prop, Object value) throws PageException {
		return Reflector.setField(obj, prop, value);
	}

	@Override
	public Object getProperty(Object obj, String prop) throws PageException {
		return Reflector.getProperty(obj, prop);
	}

	@Override
	public Object getProperty(Object obj, String prop, Object defaultValue) {
		return Reflector.getProperty(obj, prop, defaultValue);
	}

	@Override
	public void setProperty(Object obj, String prop, Object value) throws PageException {
		Reflector.setProperty(obj, prop, value);
	}

	@Override
	public void setPropertyEL(Object obj, String prop, Object value) {
		Reflector.setPropertyEL(obj, prop, value);
	}

	@Override
	public Method[] getDeclaredMethods(Class<?> clazz) {
		return Reflector.getDeclaredMethods(clazz);
	}

	@Override
	public boolean canConvert(Class<?> from, Class<?> to) {
		return Reflector.canConvert(from, to);
	}

	@Override
	public Class<?> loadClassByBundle(String className, String name, String strVersion, Identification id) throws IOException, BundleException {
		return lucee.commons.lang.ClassUtil.loadClassByBundle(className, name, strVersion, id, JavaSettingsImpl.getBundleDirectories(null));
	}

	@Override
	public Class<?> loadClassByBundle(String className, String name, Version version, Identification id) throws BundleException, IOException {
		return lucee.commons.lang.ClassUtil.loadClassByBundle(className, name, version, id, JavaSettingsImpl.getBundleDirectories(null));
	}

	@Override
	public Class<?> loadClass(String className, Class<?> defaultValue) {
		return lucee.commons.lang.ClassUtil.loadClass(className, defaultValue);
	}

	@Override
	public Class<?> loadClass(ClassLoader cl, String className, Class<?> defaultValue) {
		return lucee.commons.lang.ClassUtil.loadClass(cl, className, defaultValue);
	}

	@Override
	public Class<?> loadClass(ClassLoader cl, String className) throws IOException {
		return lucee.commons.lang.ClassUtil.loadClass(cl, className);
	}

	@Override
	public Object loadInstance(Class<?> clazz) throws ClassException {
		return lucee.commons.lang.ClassUtil.loadInstance(clazz);
	}

	@Override
	public Object loadInstance(String className) throws ClassException {
		return lucee.commons.lang.ClassUtil.loadInstance(className);
	}

	@Override
	public Object loadInstance(ClassLoader cl, String className) throws ClassException {
		return lucee.commons.lang.ClassUtil.loadInstance(cl, className);
	}

	@Override
	public Object loadInstance(Class<?> clazz, Object defaultValue) {
		return lucee.commons.lang.ClassUtil.loadInstance(clazz, defaultValue);
	}

	@Override
	public Object loadInstance(String className, Object defaultValue) {
		return lucee.commons.lang.ClassUtil.loadInstance(className, defaultValue);
	}

	@Override
	public Object loadInstance(ClassLoader cl, String className, Object defaultValue) {
		return lucee.commons.lang.ClassUtil.loadInstance(cl, className, defaultValue);
	}

	@Override
	public Object loadInstance(Class<?> clazz, Object[] args) throws ClassException, InvocationTargetException {
		return lucee.commons.lang.ClassUtil.loadInstance(clazz, args);
	}

	@Override
	public Object loadInstance(String className, Object[] args) throws ClassException, InvocationTargetException {
		return lucee.commons.lang.ClassUtil.loadInstance(className, args);
	}

	@Override
	public Object loadInstance(ClassLoader cl, String className, Object[] args) throws ClassException, InvocationTargetException {
		return lucee.commons.lang.ClassUtil.loadInstance(cl, className, args);
	}

	@Override
	public Object loadInstance(Class<?> clazz, Object[] args, Object defaultValue) {
		return lucee.commons.lang.ClassUtil.loadInstance(clazz, args, defaultValue);
	}

	@Override
	public Object loadInstance(String className, Object[] args, Object defaultValue) {
		return lucee.commons.lang.ClassUtil.loadInstance(className, args, defaultValue);
	}

	@Override
	public Object loadInstance(ClassLoader cl, String className, Object[] args, Object defaultValue) {
		return lucee.commons.lang.ClassUtil.loadInstance(cl, className, args, defaultValue);
	}

	@Override
	public boolean isBytecode(InputStream is) throws IOException {
		return lucee.commons.lang.ClassUtil.isBytecode(is);
	}

	@Override
	public boolean isBytecode(byte[] barr) {
		return lucee.commons.lang.ClassUtil.isRawBytecode(barr);
	}

	@Override
	public String getName(Class<?> clazz) {
		return lucee.commons.lang.ClassUtil.getName(clazz);
	}

	@Override
	public Method getMethodIgnoreCase(Class<?> clazz, String methodName, Class<?>[] args, Method defaultValue) {
		return lucee.commons.lang.ClassUtil.getMethodIgnoreCase(clazz, methodName, args, defaultValue);
	}

	@Override
	public Method getMethodIgnoreCase(Class<?> clazz, String methodName, Class<?>[] args) throws ClassException {
		return lucee.commons.lang.ClassUtil.getMethodIgnoreCase(clazz, methodName, args);
	}

	@Override
	public String[] getFieldNames(Class<?> clazz) {
		return lucee.commons.lang.ClassUtil.getFieldNames(clazz);
	}

	@Override
	public byte[] toBytes(Class<?> clazz) throws IOException {
		return lucee.commons.lang.ClassUtil.toBytes(clazz);
	}

	@Override
	public Class<?> toArrayClass(Class<?> clazz) {
		return lucee.commons.lang.ClassUtil.toArrayClass(clazz);
	}

	@Override
	public Class<?> toComponentType(Class<?> clazz) {
		return lucee.commons.lang.ClassUtil.toComponentType(clazz);
	}

	@Override
	public String getSourcePathForClass(Class<?> clazz, String defaultValue) {
		return lucee.commons.lang.ClassUtil.getSourcePathForClass(clazz, defaultValue);
	}

	@Override
	public String getSourcePathForClass(String className, String defaultValue) {
		return lucee.commons.lang.ClassUtil.getSourcePathForClass(className, defaultValue);
	}

	@Override
	public String extractPackage(String className) {
		return lucee.commons.lang.ClassUtil.extractPackage(className);
	}

	@Override
	public String extractName(String className) {
		return lucee.commons.lang.ClassUtil.extractName(className);
	}

	@Override
	public void start(Bundle bundle) throws BundleException {
		OSGiUtil.start(bundle);
	}

	@Override
	public Bundle addBundle(BundleContext context, InputStream is, boolean closeStream, boolean checkExistence) throws BundleException, IOException {
		return OSGiUtil.installBundle(context, is, closeStream, checkExistence);
	}
}