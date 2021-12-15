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
package lucee.commons.lang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import lucee.commons.io.log.Log;

public final class LogClassLoader extends ClassLoader {

	private final ClassLoader cl;
	private final Log log;

	public LogClassLoader(ClassLoader cl, Log log) {
		this.cl = cl;
		this.log = log;
	}

	@Override
	public synchronized void clearAssertionStatus() {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "clearAssertion");
		cl.clearAssertionStatus();
	}

	@Override
	protected Package definePackage(String name, String specTitle, String specVersion, String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase)
			throws IllegalArgumentException {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "definePackage");
		return null;
	}

	@Override
	protected Class findClass(String name) throws ClassNotFoundException {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "findClass");
		return null;
	}

	@Override
	protected String findLibrary(String libname) {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "findLibrary");
		return null;
	}

	@Override
	protected URL findResource(String name) {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "findResource");
		return null;
	}

	@Override
	protected Enumeration findResources(String name) throws IOException {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "findResources");
		return null;
	}

	@Override
	protected Package getPackage(String name) {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "getPackage");
		return null;
	}

	@Override
	protected Package[] getPackages() {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "getPackages");
		return null;
	}

	@Override
	public URL getResource(String name) {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "getResource");
		return cl.getResource(name);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "getResourceAsStream");
		return cl.getResourceAsStream(name);
	}

	@Override
	protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "loadClass");
		return null;
	}

	@Override
	public Class loadClass(String name) throws ClassNotFoundException {
		Class clazz = cl.loadClass(name);
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "loadClass(" + name + "):" + clazz);
		return clazz;
	}

	@Override
	public synchronized void setClassAssertionStatus(String className, boolean enabled) {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "setClassAssertionStatus");
		cl.setClassAssertionStatus(className, enabled);
	}

	@Override
	public synchronized void setDefaultAssertionStatus(boolean enabled) {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "setdefaultAssertionStatus");
		cl.setDefaultAssertionStatus(enabled);
	}

	@Override
	public synchronized void setPackageAssertionStatus(String packageName, boolean enabled) {
		log.log(Log.LEVEL_DEBUG, "LogClassLoader", "setPackageAssertionStatus");
		cl.setPackageAssertionStatus(packageName, enabled);
	}
}