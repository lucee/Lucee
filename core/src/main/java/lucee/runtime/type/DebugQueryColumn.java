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

import java.util.Iterator;
import java.util.List;

import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.DeprecatedException;

/**
 * implementation of the query column
 */
public final class DebugQueryColumn extends QueryColumnImpl implements QueryColumnPro, Objects {

	private boolean used;

	/**
	 * @return the used
	 */
	public boolean isUsed() {
		return used;
	}

	public DebugQueryColumn(Object[] data, Key key, QueryImpl query, int size, int type, boolean typeChecked) {
		this.data = data;
		this.key = key;
		this.query = query;
		this.size = size;
		this.type = type;
		this.typeChecked = typeChecked;
	}

	/**
	 * Constructor of the class for internal usage only
	 */
	public DebugQueryColumn() {
		super();
	}

	@Override
	public Object get(int row) throws DeprecatedException {
		used = true;
		return super.get(row);
	}

	/**
	 * touch the given line on the column at given row
	 * 
	 * @param row
	 * @return new row or existing
	 * @throws DatabaseException
	 */
	@Override
	public Object touch(int row) {
		used = true;
		return super.touch(row);
	}

	/**
	 * touch the given line on the column at given row
	 * 
	 * @param row
	 * @return new row or existing
	 * @throws DatabaseException
	 */
	@Override
	public Object touchEL(int row) {
		used = true;
		return super.touchEL(row);
	}

	@Override
	public Object get(int row, Object defaultValue) {
		used = true;
		return super.get(row, defaultValue);
	}

	@Override
	public Object clone() {
		return cloneColumnImpl(true);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return cloneColumnImpl(deepCopy);
	}

	@Override
	public QueryColumnPro cloneColumn(boolean deepCopy) {
		return cloneColumnImpl(deepCopy);
	}

	public DebugQueryColumn cloneColumnImpl(boolean deepCopy) {
		DebugQueryColumn clone = new DebugQueryColumn();
		populate(clone, deepCopy);
		return clone;
	}

	@Override
	public Iterator<Object> valueIterator() {
		used = true;
		return super.valueIterator();
	}

	@Override
	public int indexOf(Object o) {
		used = true;
		return super.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		used = true;
		return super.lastIndexOf(o);
	}

	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		used = true;
		return super.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		used = true;
		return super.toArray();
	}

	@Override
	public Object[] toArray(Object[] trg) {
		used = true;
		return super.toArray(trg);
	}

	@Override
	public QueryColumnPro toDebugColumn() {
		return this;
	}
}