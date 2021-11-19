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
package lucee.runtime.type.ref;

import java.math.BigDecimal;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.StructImpl;

/**
 * represent a reference to a variable
 */
public final class VariableReference implements Reference {

	private Collection coll;
	private Collection.Key key;

	/**
	 * constructor of the class
	 * 
	 * @param coll Collection where variable is
	 * @param key key to the value inside the collection
	 */
	public VariableReference(Collection coll, String key) {
		this.coll = coll;
		this.key = KeyImpl.init(key);
	}

	/**
	 * constructor of the class
	 * 
	 * @param coll Collection where variable is
	 * @param key key to the value inside the collection
	 */
	public VariableReference(Collection coll, Collection.Key key) {
		this.coll = coll;
		this.key = key;
	}

	/**
	 * constructor of the class
	 * 
	 * @param o Object will be casted to Collection
	 * @param key key to the value inside the collection
	 * @throws PageException
	 */
	public VariableReference(Object o, String key) throws PageException {
		this(Caster.toCollection(o), key);
	}

	/**
	 * constructor of the class
	 * 
	 * @param o Object will be casted to Collection
	 * @param key key to the value inside the collection
	 * @throws PageException
	 */
	public VariableReference(Object o, Collection.Key key) throws PageException {
		this(Caster.toCollection(o), key);
	}

	@Override
	public Object get(PageContext pc) throws PageException {
		return get();
	}

	private Object get() throws PageException {
		if (coll instanceof Query) {
			return ((Query) coll).getColumn(key);
		}
		return coll.get(key);
	}

	@Override
	public Object get(PageContext pc, Object defaultValue) {
		return get(defaultValue);
	}

	private Object get(Object defaultValue) {
		if (coll instanceof Query) {
			Object rtn = ((Query) coll).getColumn(key, null);
			if (rtn != null) return rtn;
			return defaultValue;
		}
		return coll.get(key, defaultValue);
	}

	@Override
	public Object set(PageContext pc, Object value) throws PageException {
		return coll.set(key, value);
	}

	public void set(double value) throws PageException {
		coll.set(key, Caster.toDouble(value));
	}

	public void set(BigDecimal value) throws PageException {
		coll.set(key, value);
	}

	public void set(Number value) throws PageException {
		coll.set(key, value);
	}

	@Override
	public Object setEL(PageContext pc, Object value) {
		return coll.setEL(key, value);
	}

	@Override
	public Object touch(PageContext pc) throws PageException {
		Object o;
		if (coll instanceof Query) {
			o = ((Query) coll).getColumn(key, null);
			if (o != null) return o;
			return set(pc, new StructImpl());
		}
		o = coll.get(key, null);
		if (o != null) return o;
		return set(pc, new StructImpl());
	}

	@Override
	public Object touchEL(PageContext pc) {
		Object o;
		if (coll instanceof Query) {
			o = ((Query) coll).getColumn(key, null);
			if (o != null) return o;
			return setEL(pc, new StructImpl());
		}
		o = coll.get(key, null);
		if (o != null) return o;
		return setEL(pc, new StructImpl());
	}

	@Override
	public Object remove(PageContext pc) throws PageException {
		return coll.remove(key);
	}

	@Override
	public Object removeEL(PageContext pc) {
		return coll.removeEL(key);
	}

	@Override
	public Object getParent() {
		return coll;
	}

	/**
	 * @return return the parent as Collection
	 */
	public Collection getCollection() {
		return coll;
	}

	@Override
	public String getKeyAsString() {
		return key.getString();
	}

	@Override
	public Collection.Key getKey() {
		return key;
	}

	@Override
	public String toString() {
		try {
			return Caster.toString(get());
		}
		catch (PageException e) {
			return super.toString();
		}
	}

}