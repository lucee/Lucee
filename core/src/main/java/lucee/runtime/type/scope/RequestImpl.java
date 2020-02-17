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
package lucee.runtime.type.scope;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.StructSupport;

public final class RequestImpl extends StructSupport implements Request {

	private HttpServletRequest _req;
	private boolean init;
	private static int _id = 0;
	private int id = 0;

	public RequestImpl() {
		id = ++_id;
		// super("request",SCOPE_REQUEST,Struct.TYPE_REGULAR);

	}

	/**
	 * @return Returns the id.
	 */
	public int _getId() {
		return id;
	}

	@Override
	public void initialize(PageContext pc) {
		_req = pc.getHttpServletRequest();// HTTPServletRequestWrap.pure(pc.getHttpServletRequest());
		init = true;

	}

	@Override
	public boolean isInitalized() {
		return init;
	}

	@Override
	public void release(PageContext pc) {
		init = false;
	}

	@Override
	public int getType() {
		return SCOPE_REQUEST;
	}

	@Override
	public String getTypeAsString() {
		return "request";
	}

	@Override
	public int size() {
		int size = 0;
		synchronized (_req) {
			Enumeration<String> names = _req.getAttributeNames();
			while (names.hasMoreElements()) {
				names.nextElement();
				size++;
			}
		}
		return size;
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return keyList().iterator();
	}

	private List<Key> keyList() {
		synchronized (_req) {
			Enumeration<String> names = _req.getAttributeNames();
			List<Key> list = new ArrayList<Key>();
			while (names.hasMoreElements()) {
				list.add(KeyImpl.getInstance(names.nextElement()));
			}
			return list;
		}
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}

	@Override
	public Iterator<Object> valueIterator() {
		synchronized (_req) {
			Enumeration<String> names = _req.getAttributeNames();
			List<Object> list = new ArrayList<Object>();
			while (names.hasMoreElements()) {
				list.add(_req.getAttribute(names.nextElement()));
			}
			return list.iterator();
		}
	}

	@Override
	public Key[] keys() {
		List<Key> list = keyList();
		return list.toArray(new Key[list.size()]);
	}

	@Override
	public Object remove(Key key) throws PageException {
		Object _null = NullSupportHelper.NULL();
		Object value = remove(key, _null);
		if (value != _null) return value;
		throw new ExpressionException("can't remove key [" + key + "] from struct, key doesn't exist");
	}

	@Override
	public void clear() {
		synchronized (_req) {
			Iterator<String> it = ListUtil.toIterator(_req.getAttributeNames());
			while (it.hasNext()) {
				_req.removeAttribute(it.next());
			}
		}
	}

	@Override
	public final Object get(Key key) throws PageException {
		Object _null = NullSupportHelper.NULL();
		Object value = get(key, _null);
		if (value == _null) throw invalidKey(null, this, key, "request scope");
		return value;
	}

	@Override
	public final Object get(PageContext pc, Key key) throws PageException {
		Object _null = NullSupportHelper.NULL(pc);
		Object value = get(pc, key, _null);
		if (value == _null) throw invalidKey(null, this, key, "request scope");
		return value;
	}

	@Override
	public Object removeEL(Key key) {
		return remove(key, null);
	}

	@Override
	public Object remove(Key key, Object defaultValue) {
		synchronized (_req) {
			Object value = _req.getAttribute(key.getLowerString());
			if (value != null) {
				_req.removeAttribute(key.getLowerString());
				return value;
			}

			value = defaultValue;
			Enumeration<String> names = _req.getAttributeNames();
			String k;
			while (names.hasMoreElements()) {
				k = names.nextElement();
				if (k.equalsIgnoreCase(key.getString())) {
					value = _req.getAttribute(k);
					_req.removeAttribute(k);
					return value;
				}
			}
			return defaultValue;
		}
	}

	@Override
	public final Object get(Key key, Object defaultValue) {
		return get((PageContext) null, key, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Key key, Object defaultValue) {
		synchronized (_req) {
			Object value = _req.getAttribute(key.getLowerString());
			if (value != null) return value;

			Iterator<String> it = ListUtil.toIterator(_req.getAttributeNames());
			Collection.Key k;
			while (it.hasNext()) {
				k = KeyImpl.init(it.next());
				if (key.equals(k)) return _req.getAttribute(k.getString());
			}
			return defaultValue;
		}
	}

	@Override
	public Object setEL(Key key, Object value) {
		synchronized (_req) {
			_req.setAttribute(key.getLowerString(), value);
		}
		return value;
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		return setEL(key, value);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		Struct trg = new StructImpl();
		StructImpl.copy(this, trg, deepCopy);
		return trg;
	}

	@Override
	public final boolean containsKey(Key key) {
		Object _null = NullSupportHelper.NULL();
		return get(key, _null) != _null;
	}

	@Override
	public final boolean containsKey(PageContext pc, Key key) {
		Object _null = NullSupportHelper.NULL(pc);
		return get(pc, key, _null) != _null;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return ScopeSupport.toDumpData(pageContext, maxlevel, dp, this, getTypeAsString());
	}
}