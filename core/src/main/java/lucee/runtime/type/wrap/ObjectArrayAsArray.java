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
 * Copy-on-write Array view over a native Object[].
 */
public final class ObjectArrayAsArray extends NativeArrayAsArray {

	private static final long serialVersionUID = 6806202511036806007L;

	private Object[] array;

	private ObjectArrayAsArray(Object[] array) {
		this.array = array;
	}

	public static Array wrap(Object[] array) {
		return new ObjectArrayAsArray(array);
	}

	/**
	 * @return the wrapped native array while still in read-through mode; null after a mutation
	 *         has switched this instance to an ArrayImpl.
	 */
	public Object[] getNativeArray() {
		return array;
	}

	@Override
	protected int nativeSize() {
		return array.length;
	}

	@Override
	protected Object nativeGet(int zeroBasedIndex) {
		return array[zeroBasedIndex];
	}

	@Override
	protected Object[] nativeToArray() {
		Object[] src = array;
		Object[] copy = new Object[src.length];
		System.arraycopy(src, 0, copy, 0, src.length);
		return copy;
	}

	@Override
	protected void releaseNative() {
		this.array = null;
	}
}
