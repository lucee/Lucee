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

import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;

/**
 * MUST this is a value iterator instead of a key iterator
 * 
 */
public final class COMKeyWrapperIterator implements Iterator<Collection.Key> {

	private EnumVariant enumVariant;
	private COMObject wrapper;

	/**
	 * @param wrapper
	 */
	public COMKeyWrapperIterator(COMObject wrapper) {
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
	public Collection.Key next() {
		try {
			return Caster.toKey(COMUtil.toObject(wrapper, enumVariant.Next(), "", null));
		}
		catch (CasterException e) {
			throw new PageRuntimeException(e);
		}
	}
}