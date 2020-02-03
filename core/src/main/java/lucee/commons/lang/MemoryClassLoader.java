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
package lucee.commons.lang;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.config.Config;
import lucee.runtime.instrumentation.InstrumentationFactory;
import lucee.transformer.bytecode.util.ClassRenamer;

/**
 * ClassLoader that loads classes in memory that are not stored somewhere physically
 */
public final class MemoryClassLoader extends ExtendableClassLoader {

	private Config config;
	private ClassLoader pcl;
	private long size;

	/**
	 * Constructor of the class
	 * 
	 * @param directory
	 * @param parent
	 * @throws IOException
	 */
	public MemoryClassLoader(Config config, ClassLoader parent) throws IOException {
		super(parent);
		this.pcl = parent;
		this.config = config;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);
		if (c == null) {
			try {
				c = pcl.loadClass(name);// if(name.indexOf("sub")!=-1)print.ds(name);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				c = findClass(name);
			}
		}
		if (resolve) {
			resolveClass(c);
		}
		return c;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		throw new ClassNotFoundException("class " + name + " is invalid or doesn't exist");
	}

	@Override
	public synchronized Class<?> loadClass(String name, byte[] barr) throws UnmodifiableClassException {
		Class<?> clazz = null;
		try {
			clazz = loadClass(name);
		}
		catch (ClassNotFoundException cnf) {}

		// if class already exists
		if (clazz != null) {
			try {
				InstrumentationFactory.getInstrumentation(config).redefineClasses(new ClassDefinition(clazz, barr));
				return clazz;
			}
			catch (Exception e) {
				LogUtil.log(null, "compilation", e);
			}
			return rename(clazz, barr);
		}
		// class not exists yet
		return _loadClass(name, barr);
	}

	private Class<?> rename(Class<?> clazz, byte[] barr) {
		String prefix = clazz.getName();
		Class<?> clazz2 = null;
		String newName;
		int index = 0;
		do {
			clazz2 = null;
			newName = prefix + "$" + (++index);
			try {
				clazz2 = loadClass(newName); // we do not load existing class from disk
			}
			catch (ClassNotFoundException cnf) {}
		}
		while (clazz2 != null);
		return _loadClass(newName, ClassRenamer.rename(barr, newName));

	}

	private synchronized Class<?> _loadClass(String name, byte[] barr) {
		size += barr.length;
		// class not exists yet
		try {
			return defineClass(name, barr, 0, barr.length);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			SystemUtil.sleep(1);
			try {
				return defineClass(name, barr, 0, barr.length);
			}
			catch (Throwable t2) {
				ExceptionUtil.rethrowIfNecessary(t2);
				SystemUtil.sleep(1);
				return defineClass(name, barr, 0, barr.length);
			}
		}
	}

	public long getSize() {
		return size;
	}
}