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
/**
 * Implements the CFML Function createobject
 * FUTURE neue attr unterstuestzen
 */
package lucee.runtime.functions.other;

import java.util.ArrayList;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.res.util.WildCardFilter;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.java.JavaObject;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.type.util.ListUtil;

public final class JavaProxy implements Function {

	private static final long serialVersionUID = 2696152022196556309L;

	public static Object call(PageContext pc, String className) throws PageException {
		return call(pc, className, null, null);
	}

	public static Object call(PageContext pc, String className, Object pathOrName) throws PageException {
		return call(pc, className, pathOrName, null);
	}

	public static Object call(PageContext pc, String className, Object pathOrName, String delimiterOrVersion) throws PageException {
		checkAccess(pc);
		return new JavaObject((pc).getVariableUtil(), loadClass(pc, className, pathOrName, delimiterOrVersion));
	}

	public static Class<?> loadClass(PageContext pc, String className, Object pathOrName, String delimiterOrVersion) throws PageException {

		if (StringUtil.isEmpty(pathOrName)) return loadClassByPath(pc, className, null);

		String str = Caster.toString(pathOrName, null);

		// String input
		if (str != null) {

			// Bundle Name?
			if (!str.contains("/") && !str.contains("\\") && !str.endsWith(".jar")) {
				try {
					return ClassUtil.loadClassByBundle(className, str, delimiterOrVersion, pc.getConfig().getIdentification(), JavaSettingsImpl.getBundles(pc));
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					throw Caster.toPageException(t);
				}
			}

			// path
			if (StringUtil.isEmpty(delimiterOrVersion)) delimiterOrVersion = ",";

			String[] arrPaths = ListUtil.trimItems(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(str, delimiterOrVersion)));

			return loadClassByPath(pc, className, arrPaths);
		}

		return loadClassByPath(pc, className, ListUtil.toStringArray(Caster.toArray(pathOrName)));
	}

	private static Class<?> loadClassByPath(PageContext pc, String className, String[] paths) throws PageException {

		PageContextImpl pci = (PageContextImpl) pc;
		java.util.List<Resource> resources = new ArrayList<Resource>();

		if (paths != null && paths.length > 0) {
			// load resources
			for (int i = 0; i < paths.length; i++) {

				Resource res = ResourceUtil.toResourceExisting(pc, paths[i]);

				if (res.isDirectory()) {
					// a directory was passed, add all of the jar files from it
					FileResource dir = (FileResource) res;
					Resource[] jars = dir.listResources((ResourceNameFilter) new WildCardFilter("*.jar"));

					for (Resource jar: jars) {
						resources.add(jar);
					}
				}
				else {

					resources.add(res);
				}
			}
			// throw new FunctionException(pc, "JavaProxy", 2, "path", "argument path has to be an array of
			// strings or a single string, where every string is defining a path");
		}

		// load class
		try {

			ClassLoader cl = resources.isEmpty() ? pci.getClassLoader() : pci.getClassLoader(resources.toArray(new Resource[resources.size()]));

			Class clazz = null;
			try {
				clazz = ClassUtil.loadClass(cl, className);
			}
			catch (ClassException ce) {
				// try java.lang if no package definition
				if (className.indexOf('.') == -1) {
					try {
						clazz = ClassUtil.loadClass(cl, "java.lang." + className);
					}
					catch (ClassException e) {
						throw ce;
					}
				}
				else throw ce;
			}

			return clazz;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static void checkAccess(PageContext pc) throws SecurityException {

		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == SecurityManager.VALUE_NO)
			throw new SecurityException("Can't create Java object, direct Java access is denied by the Security Manager");

		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_OBJECT) == SecurityManager.VALUE_NO)
			throw new SecurityException("Can't access function, access is denied by the Security Manager");
	}
}