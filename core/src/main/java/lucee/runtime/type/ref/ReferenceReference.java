/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.type.ref;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.StructImpl;

/**
 * Handle a Reference
 */
public final class ReferenceReference implements Reference {

	private Reference reference;
	private Collection.Key key;

	/**
	 * @param reference
	 * @param key
	 */
	public ReferenceReference(Reference reference, String key) {
		this(reference, KeyImpl.init(key));
	}

	public ReferenceReference(Reference reference, Collection.Key key) {
		this.reference = reference;
		this.key = key;
	}

	@Override
	public Collection.Key getKey() {
		return key;
	}

	@Override
	public String getKeyAsString() {
		return key.getString();
	}

	@Override
	public Object get(PageContext pc) throws PageException {
		return pc.getCollection(reference.get(pc), key);
	}

	@Override
	public Object get(PageContext pc, Object defaultValue) {
		return pc.getCollection(reference.get(pc, null), key, defaultValue);
	}

	@Override
	public Object set(PageContext pc, Object value) throws PageException {
		return pc.set(reference.touch(pc), key, value);
	}

	@Override
	public Object setEL(PageContext pc, Object value) {
		try {
			return set(pc, value);
		}
		catch (PageException e) {
			return null;
		}
	}

	@Override
	public Object touch(PageContext pc) throws PageException {
		Object parent = reference.touch(pc);
		Object o = pc.getCollection(parent, key, null);
		if (o != null) return o;
		return pc.set(parent, key, new StructImpl());
	}

	@Override
	public Object touchEL(PageContext pc) {
		Object parent = reference.touchEL(pc);
		Object o = pc.getCollection(parent, key, null);
		if (o != null) return o;
		try {
			return pc.set(parent, key, new StructImpl());
		}
		catch (PageException e) {
			return null;
		}
	}

	@Override
	public Object remove(PageContext pc) throws PageException {
		return pc.getVariableUtil().remove(reference.get(pc), key);
	}

	@Override
	public Object removeEL(PageContext pc) {
		return pc.getVariableUtil().removeEL(reference.get(pc, null), key);
	}

	@Override
	public Object getParent() {
		return reference;
	}

	@Override
	public String toString() {
		return "java.util.ReferenceReference(reference:" + reference + ";key:" + key + ")";
	}
}