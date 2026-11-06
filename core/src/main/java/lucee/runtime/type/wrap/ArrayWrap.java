/**
 * Copyright (c) 2015, Lucee Association Switzerland
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
package lucee.runtime.type.wrap;

import lucee.runtime.type.Array;

/**
 * Common contract for the Array views in this package ({@link ListAsArray} and the
 * {@code *ArrayAsArray} views over native Java arrays), so callers can check for this interface
 * once instead of special-casing every implementation.
 */
public interface ArrayWrap extends Array {

	/**
	 * Returns the content of this array as Object[], with the same nullable elements a
	 * {@code get(index, null)} loop would produce.
	 * <p>
	 * Unlike {@link Array#toArray()}, this is not guaranteed to be a copy: a view still backed by a
	 * native Object[] returns that array as is (no copy), primitive arrays are boxed, list-backed
	 * arrays use {@link java.util.List#toArray()}.
	 *
	 * @return the elements as Object[]
	 */
	public Object[] toNativeArray();

	/**
	 * Returns the native Java array (Object[], int[], ...) this view still reads through, so it can be
	 * handed back to Java without converting it element by element.
	 *
	 * @return the wrapped native array, or null when there is none (list-backed arrays) or a mutation
	 *         has already switched the view to its own copy (copy-on-write)
	 */
	public Object getNativeArray();
}
