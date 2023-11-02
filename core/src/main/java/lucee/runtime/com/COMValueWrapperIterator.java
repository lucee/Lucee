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
package lucee.runtime.com;

import java.util.Iterator;

import com.jacob.com.EnumVariant;

/**
 * 
 */
public final class COMValueWrapperIterator implements Iterator {

	private EnumVariant enumVariant;
	private COMObject wrapper;

	/**
	 * @param wrapper
	 */
	public COMValueWrapperIterator(COMObject wrapper) {
		this.enumVariant = new EnumVariant(wrapper.getDispatch());
		this.wrapper = wrapper;
	}

	@Override
	public void remove() {
		enumVariant.safeRelease();
	}

	@Override
	public boolean hasNext() {
		return enumVariant.hasMoreElements();
	}

	@Override
	public Object next() {
		return COMUtil.toObject(wrapper, enumVariant.Next(), "", null);
	}
}