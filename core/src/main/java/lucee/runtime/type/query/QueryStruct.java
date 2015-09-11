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
package lucee.runtime.type.query;

import java.util.Iterator;

import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;

public class QueryStruct extends StructSupport {

	private Query qry;
	private int row;

	public QueryStruct(Query qry, int row) {
		this.qry=qry;
		this.row=row;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object remove(Key key) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object removeEL(Key key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setEL(Key key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		return qry.keys().length;
	}

	@Override
	public Key[] keys() {
		return qry.keys();
	}

	@Override
	public Object get(Key key) throws PageException {
		return qry.getAt(key, row);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return qry.getAt(key, row,defaultValue);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return StructUtil.duplicate(this, deepCopy);
	}

	@Override
	public boolean containsKey(Key key) {
		return qry.containsKey(key);
	}

	@Override
	public Iterator<Key> keyIterator() {
		return qry.keyIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return null;//new QueryValueIterator(qry, row);
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return null;//new QueryEntryItrator(qry, row);
	}

}