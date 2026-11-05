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
import lucee.runtime.type.util.ArrayUtil;

/**
 * Copy-on-write Array view over a native long[].
 */
public final class LongArrayAsArray extends NativeArrayAsArray {

	private static final long serialVersionUID = 6806202511036806013L;

	private long[] array;

	private LongArrayAsArray(long[] array) {
		this.array = array;
	}

	public static Array wrap(long[] array) {
		return new LongArrayAsArray(array);
	}

	@Override
	protected int nativeSize() {
		return array.length;
	}

	@Override
	protected Object nativeGet(int zeroBasedIndex) {
		return Long.valueOf(array[zeroBasedIndex]);
	}

	@Override
	protected Object[] nativeToArray() {
		return ArrayUtil.toReferenceType(array);
	}

	@Override
	protected void releaseNative() {
		this.array = null;
	}
}
