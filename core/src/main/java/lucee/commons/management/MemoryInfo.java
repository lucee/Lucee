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
package lucee.commons.management;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class MemoryInfo {

	public static int ALL = 0;
	public static int PRIVATE_ONLY = 1;
	public static int NON_PUBLIC = 2;
	public static int NONE = 3;

	private static int[] access;
	static {
		access = new int[4];
		access[ALL] = 1;
		access[PRIVATE_ONLY] = 2;
		access[NON_PUBLIC] = 3;
		access[NONE] = 4;
	}

	/**
	 * Returns an estimation of the "shallow" memory usage, in bytes, of the given object. The estimate
	 * is provided by the running JVM and is likely to be as accurate a measure as can be reasonably
	 * made by the running Java program. It will generally include memory taken up for "housekeeping" of
	 * that object.
	 * 
	 * The shallow memory usage does not count the memory used by objects referenced by obj.
	 * 
	 * @param obj The object whose memory usage is to be estimated.
	 * @return An estimate, in bytes, of the heap memory taken up by obj.
	 */
	public static long memoryUsageOf(Instrumentation inst, final Object obj) {
		return inst.getObjectSize(obj);
	}

	/**
	 * returns an estimation, in bytes, of the memory usage of the given object plus (recursively)
	 * objects it references via non-static private or protected fields. The estimate for each
	 * individual object is provided by the running JVM and is likely to be as accurate a measure as can
	 * be reasonably made by the running Java program. It will generally include memory taken up for
	 * "housekeeping" of that object.
	 * 
	 * @param obj The object whose memory usage (and that of objects it references) is to be estimated.
	 * @return An estimate, in bytes, of the heap memory taken up by obj and objects it references via
	 *         private or protected non-static fields.
	 */
	public static long deepMemoryUsageOf(Instrumentation inst, final Object obj) {
		return deepMemoryUsageOf(inst, obj, NON_PUBLIC);
	}

	/**
	 * Returns an estimation, in bytes, of the memory usage of the given object plus (recursively)
	 * objects it references via non-static references. Which references are traversed depends on the
	 * Visibility Filter passed in. The estimate for each individual object is provided by the running
	 * JVM and is likely to be as accurate a measure as can be reasonably made by the running Java
	 * program. It will generally include memory taken up for "housekeeping" of that object.
	 * 
	 * @param obj The object whose memory usage (and that of objects it references) is to be estimated.
	 * @param referenceFilter specifies which references are to be recursively included in the resulting
	 *            count (ALL,PRIVATE_ONLY,NON_PUBLIC,NONE).
	 * @return An estimate, in bytes, of the heap memory taken up by obj and objects it references.
	 */
	public static long deepMemoryUsageOf(Instrumentation inst, final Object obj, final int referenceFilter) {
		return deepMemoryUsageOf0(inst, new HashSet<Integer>(), obj, referenceFilter);
	}

	/**
	 * Returns an estimation, in bytes, of the memory usage of the given objects plus (recursively)
	 * objects referenced via non-static references from any of those objects via non-public fields. If
	 * two or more of the given objects reference the same Object X, then the memory used by Object X
	 * will only be counted once. However, the method guarantees that the memory for a given object
	 * (either in the passed-in collection or found while traversing the object graphs from those
	 * objects) will not be counted more than once. The estimate for each individual object is provided
	 * by the running JVM and is likely to be as accurate a measure as can be reasonably made by the
	 * running Java program. It will generally include memory taken up for "housekeeping" of that
	 * object.
	 * 
	 * @param objs The collection of objects whose memory usage is to be totalled.
	 * @return An estimate, in bytes, of the total heap memory taken up by the obejcts in objs and,
	 *         recursively, objects referenced by private or protected (non-static) fields.
	 * @throws IOException
	 */
	public static long deepMemoryUsageOfAll(Instrumentation inst, final Collection<? extends java.lang.Object> objs) throws IOException {
		return deepMemoryUsageOfAll(inst, objs, NON_PUBLIC);
	}

	/**
	 * Returns an estimation, in bytes, of the memory usage of the given objects plus (recursively)
	 * objects referenced via non-static references from any of those objects. Which references are
	 * traversed depends on the VisibilityFilter passed in. If two or more of the given objects
	 * reference the same Object X, then the memory used by Object X will only be counted once. However,
	 * the method guarantees that the memory for a given object (either in the passed-in collection or
	 * found while traversing the object graphs from those objects) will not be counted more than once.
	 * The estimate for each individual object is provided by the running JVM and is likely to be as
	 * accurate a measure as can be reasonably made by the running Java program. It will generally
	 * include memory taken up for "housekeeping" of that object.
	 * 
	 * @param objs The collection of objects whose memory usage is to be totalled.
	 * @param referenceFilter Specifies which references are to be recursively included in the resulting
	 *            count (ALL,PRIVATE_ONLY,NON_PUBLIC,NONE).
	 * @return An estimate, in bytes, of the total heap memory taken up by the obejcts in objs and,
	 *         recursively, objects referenced by any of those objects that match the VisibilityFilter
	 *         criterion.
	 * @throws IOException
	 */
	public static long deepMemoryUsageOfAll(Instrumentation inst, final Collection<? extends java.lang.Object> objs, final int referenceFilter) throws IOException {
		long total = 0L;
		final Set<Integer> counted = new HashSet<Integer>(objs.size() * 4);
		for (final Object o: objs) {
			total += deepMemoryUsageOf0(inst, counted, o, referenceFilter);
		}
		return total;
	}

	private static long deepMemoryUsageOf0(final Instrumentation instrumentation, final Set<Integer> counted, final Object obj, final int filter) throws SecurityException {
		final Stack<Object> st = new Stack<Object>();
		st.push(obj);
		long total = 0L;
		while (!st.isEmpty()) {
			final Object o = st.pop();
			if (counted.add(System.identityHashCode(o))) {
				final long sz = instrumentation.getObjectSize(o);
				total += sz;
				Class clz = o.getClass();
				final Class compType = clz.getComponentType();
				if (compType != null && !compType.isPrimitive()) {
					final Object[] array;
					final Object[] arr = array = (Object[]) o;
					for (int i = 0; i < array.length; ++i) {
						final Object el = array[i];
						if (el != null) {
							st.push(el);
						}
					}
				}
				while (clz != null) {
					final Field[] declaredFields = clz.getDeclaredFields();
					for (int j = 0; j < declaredFields.length; ++j) {
						final Field fld = declaredFields[j];
						final int mod = fld.getModifiers();
						if ((mod & 0x8) == 0x0 && isOf(filter, mod)) {
							final Class fieldClass = fld.getType();
							if (!fieldClass.isPrimitive()) {
								if (!fld.isAccessible()) {
									fld.setAccessible(true);
								}
								try {
									final Object subObj = fld.get(o);
									if (subObj != null) {
										st.push(subObj);
									}
								}
								catch (IllegalAccessException illAcc) {
									throw new InternalError("Couldn't read " + fld);
								}
							}
						}
					}
					clz = clz.getSuperclass();
				}
			}
		}
		return total;
	}

	private static boolean isOf(final int f, final int mod) {
		switch (access[f]) {
		case 1:
			return true;
		case 4:
			return false;
		case 2:
			return (mod & 0x2) != 0x0;
		case 3:
			return (mod & 0x1) == 0x0;
		default:
			throw new IllegalArgumentException("Illegal filter " + mod);
		}
	}

}