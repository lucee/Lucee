/**
 * Copyright (c) 2016, Lucee Assosication Switzerland
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
package lucee.runtime.functions;

import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;

// TODO kann man nicht auf context ebene

/**
 * Pool to Handle Tags
 */
public final class FunctionHandlerPool {
	private static ConcurrentHashMap<String, BIF> map = new ConcurrentHashMap<String, BIF>();

	public static Object invoke(PageContext pc, Object[] args, String className, String bundleName, String bundleVersion) throws PageException {
		return use(pc, className, bundleName, bundleVersion).invoke(pc, args);
	}

	/**
	 * return a tag to use from a class
	 * 
	 * @param tagClass
	 * @return Tag
	 * @throws PageException
	 */
	public static BIF use(PageContext pc, String className, String bundleName, String bundleVersion) throws PageException {
		String id = toId(className, bundleName, bundleVersion);
		BIF bif = map.get(id);
		if (bif != null) return bif;

		try {
			Class<?> clazz;
			// OSGi bundle
			if (!StringUtil.isEmpty(bundleName))
				clazz = ClassUtil.loadClassByBundle(className, bundleName, bundleVersion, pc.getConfig().getIdentification(), JavaSettingsImpl.getBundles(pc));
			// JAR
			else clazz = ClassUtil.loadClass(className);

			if (Reflector.isInstaneOf(clazz, BIF.class, false)) bif = (BIF) ClassUtil.newInstance(clazz);
			else bif = new BIFProxy(clazz);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		map.put(id, bif);
		return bif;
	}

	private static String toId(String className, String bundleName, String bundleVersion) {
		if (bundleName == null && bundleVersion == null) return className;
		if (bundleVersion == null) return className + ":" + bundleName;
		return className + ":" + bundleName + ":" + bundleVersion;
	}
}