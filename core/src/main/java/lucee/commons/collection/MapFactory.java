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
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupportLegacy;
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
				Class<?> cls = Class.forName(CONCURRENT_MAP);
				MethodHandles.Lookup lookup = MethodHandles.publicLookup();
				try {
					mh = lookup.findConstructor(cls, MethodType.methodType(void.class, int.class));
					takesInt = true;
				}
				catch (NoSuchMethodException e) {
					mh = lookup.findConstructor(cls, MethodType.methodType(void.class));
				}
			}
			catch (Exception e) {
				throw new RuntimeException("lucee.concurrent.map: failed to resolve [" + CONCURRENT_MAP + "]", e);
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
			throw new RuntimeException("lucee.concurrent.map: failed to instantiate [" + CONCURRENT_MAP + "]", t);
		}
	}

	public static <K, V> Map<K, V> getConcurrentMap() {
		return getConcurrentMap(ConcurrentHashMapNullSupport.DEFAULT_INITIAL_CAPACITY);
	}

	public static <K, V> Map<K, V> getConcurrentMap(int initialCapacity) {
		if (LEGACY) return new ConcurrentHashMapNullSupportLegacy<>(initialCapacity);
		Map<K, Object> delegate = newDelegate(initialCapacity);
		return delegate != null ? ConcurrentHashMapNullSupport.withDelegate(delegate) : new ConcurrentHashMapNullSupport<>(initialCapacity);
	}

	public static <K, V> Map<K, V> getConcurrentMap(Map<K, V> map) {
		if (LEGACY) return new ConcurrentHashMapNullSupportLegacy<>(map);
		Map<K, Object> delegate = newDelegate(map.size());
		if (delegate != null) {
			ConcurrentHashMapNullSupport<K, V> result = ConcurrentHashMapNullSupport.withDelegate(delegate);
			result.putAll(map);
			return result;
		}
		return new ConcurrentHashMapNullSupport<>(map);
	}
}
