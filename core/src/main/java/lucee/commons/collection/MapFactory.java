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
package lucee.commons.collection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport;
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupportJDK;
import lucee.commons.io.SystemUtil;

public final class MapFactory {

	private static final String CONCURRENT_MAP = SystemUtil.getSystemPropOrEnvVar("lucee.concurrent.map.impl", null);
	private static final boolean LEGACY = "legacy".equalsIgnoreCase(CONCURRENT_MAP);
	private static final MethodHandle DELEGATE_CTOR;
	private static final boolean DELEGATE_CTOR_TAKES_INT;

	static {
		MethodHandle mh = null;
		boolean takesInt = false;
		if (CONCURRENT_MAP != null && !LEGACY) {
			try {
				// Look in MapFactory's classloader first (matches Class.forName semantics);
				// fall back to the system classloader so users can drop the delegate jar on
				// the JVM primary classpath (Tomcat lib/, CATALINA_OPTS -cp, etc.) without
				// needing it bundled inside lucee.core's OSGi bundle scope.
				Class<?> cls;
				try {
					cls = Class.forName(CONCURRENT_MAP);
				}
				catch (ClassNotFoundException e) {
					cls = ClassLoader.getSystemClassLoader().loadClass(CONCURRENT_MAP);
				}
				MethodHandles.Lookup lookup = MethodHandles.publicLookup();
				try {
					mh = lookup.findConstructor(cls, MethodType.methodType(void.class, int.class));
					takesInt = true;
				}
				catch (NoSuchMethodException e) {
					mh = lookup.findConstructor(cls, MethodType.methodType(void.class));
				}
			}
			catch (Throwable t) {
				String msg = "lucee.concurrent.map.impl: failed to resolve [" + CONCURRENT_MAP
						+ "] (" + t.getClass().getName() + ": " + t.getMessage()
						+ "); remove the property or set to 'legacy' to recover";
				throw new RuntimeException(msg, t);
			}
		}
		DELEGATE_CTOR = mh;
		DELEGATE_CTOR_TAKES_INT = takesInt;
	}

	@SuppressWarnings("unchecked")
	private static <K> Map<K, Object> newDelegate(int initialCapacity) {
		if (DELEGATE_CTOR == null) return null;
		try {
			return (Map<K, Object>) (DELEGATE_CTOR_TAKES_INT ? DELEGATE_CTOR.invoke(initialCapacity) : DELEGATE_CTOR.invoke());
		}
		catch (Throwable t) {
			throw new RuntimeException("lucee.concurrent.map.impl: failed to instantiate [" + CONCURRENT_MAP + "]", t);
		}
	}

	public static <K, V> Map<K, V> getConcurrentMap() {
		return getConcurrentMap(ConcurrentHashMapNullSupportJDK.DEFAULT_INITIAL_CAPACITY);
	}

	public static <K, V> Map<K, V> getConcurrentMap(int initialCapacity) {
		// Note on naming: ConcurrentHashMapNullSupport (no suffix) is the segmented pre-7.1
		// implementation, restored under its original name so pre-7.1 serialised sessions
		// deserialise transparently. ConcurrentHashMapNullSupportJDK is the LDEV-5098
		// JDK-backed wrapper, the default since 7.1. See LDEV-6288 for full rationale.
		if (LEGACY) return new ConcurrentHashMapNullSupport<>(initialCapacity);
		Map<K, Object> delegate = newDelegate(initialCapacity);
		return delegate != null ? new ConcurrentHashMapNullSupportJDK<>(delegate) : new ConcurrentHashMapNullSupportJDK<>(initialCapacity);
	}

	public static <K, V> Map<K, V> getConcurrentMap(Map<K, V> map) {
		if (LEGACY) return new ConcurrentHashMapNullSupport<>(map);
		Map<K, V> result = getConcurrentMap(map.size());
		result.putAll(map);
		return result;
	}
}
