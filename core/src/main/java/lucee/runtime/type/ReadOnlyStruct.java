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
package lucee.runtime.type;

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;

/**
 * a read only Struct if flag is set to readonly
 */
public class ReadOnlyStruct extends StructImpl {

	private boolean isReadOnly = false;

	/**
	 * sets if scope is readonly or not
	 * 
	 * @param isReadOnly
	 */
	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		if (isReadOnly) throw new ExpressionException("can't remove key [" + key.getString() + "] from struct, struct is readonly");
		return super.remove(key);
	}

	@Override
	public Object removeEL(Collection.Key key) {
		if (isReadOnly) return null;
		return super.removeEL(key);
	}

	public void removeAll() {
		if (!isReadOnly) super.clear();
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		if (isReadOnly) throw new ExpressionException("can't set key [" + key.getString() + "] to struct, struct is readonly");
		return super.set(key, value);
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		if (!isReadOnly) super.setEL(key, value);
		return value;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		Struct trg = new StructImpl();
		// trg.isReadOnly=isReadOnly;
		copy(this, trg, deepCopy);
		return trg;
	}

	@Override
	public void clear() {
		if (isReadOnly) throw new PageRuntimeException(new ExpressionException("can't clear struct, struct is readonly"));
		super.clear();
	}
}