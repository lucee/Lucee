/**
 * Copyright (c) 2026, Lucee Association Switzerland. All rights reserved.
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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches "class name -> Class" lookups done through a (RPC) classloader, used by createObject("java")
 * and the new operator.
 * 
 * To not keep classloaders alive (see LDEV-5903), the classloader is held as a weak key and the
 * classes as weak references. Only classes defined by the classloader itself or one of its parents
 * are cached, classes resolved via the OSGi fallback are not, because they depend on the installed
 * bundles and not on the classloader. The cache is also cleared explicitly when RPC classloaders are
 * cleared or flushed and when the java settings change.
 */
public final class ClassLookupCache {

	private static final Map<ClassLoader, Map<String, WeakReference<Class<?>>>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

	private ClassLookupCache() {
	}

	public static Class<?> get(ClassLoader cl, String className) {
		if (cl == null || className == null) return null;
		Map<String, WeakReference<Class<?>>> classes = CACHE.get(cl);
		if (classes == null) return null;
		WeakReference<Class<?>> ref = classes.get(className);
		return ref == null ? null : ref.get();
	}

	public static void put(ClassLoader cl, String className, Class<?> clazz) {
		if (cl == null || className == null || clazz == null || !isVisibleFrom(cl, clazz)) return;
		Map<String, WeakReference<Class<?>>> classes = CACHE.get(cl);
		if (classes == null) {
			synchronized (CACHE) {
				classes = CACHE.get(cl);
				if (classes == null) {
					classes = new ConcurrentHashMap<>();
					CACHE.put(cl, classes);
				}
			}
		}
		classes.put(className, new WeakReference<Class<?>>(clazz));
	}

	public static void remove(ClassLoader cl) {
		if (cl != null) CACHE.remove(cl);
	}

	public static void clear() {
		CACHE.clear();
	}

	public static int size() {
		return CACHE.size();
	}

	/**
	 * true if the class is defined by the given classloader or one of its parents
	 */
	private static boolean isVisibleFrom(ClassLoader cl, Class<?> clazz) {
		ClassLoader defining = clazz.getClassLoader();
		if (defining == null) return true; // bootstrap
		ClassLoader curr = cl;
		while (curr != null) {
			if (curr == defining) return true;
			curr = curr.getParent();
		}
		return false;
	}
}
