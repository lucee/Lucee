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

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.StructSupport;

public final class CollectionStruct extends StructSupport implements ObjectWrap, Struct {

	private final Collection coll;

	public CollectionStruct(Collection coll) {
		this.coll = coll;
	}

	@Override
	public void clear() {
		coll.clear();
	}

	@Override
	public final boolean containsKey(Key key) {
		return coll.containsKey(key);
	}

	@Override
	public final boolean containsKey(PageContext pc, Key key) {
		return coll.containsKey(key);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return (Collection) Duplicator.duplicate(coll, deepCopy);
	}

	@Override
	public final Object get(Key key) throws PageException {
		return coll.get(key);
	}

	@Override
	public final Object get(PageContext pc, Key key) throws PageException {
		return coll.get(key);
	}

	@Override
	public final Object get(Key key, Object defaultValue) {
		return coll.get(key, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Key key, Object defaultValue) {
		return coll.get(key, defaultValue);
	}

	@Override
	public Key[] keys() {
		return coll.keys();
	}

	@Override
	public Object remove(Key key) throws PageException {
		return coll.remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		return coll.removeEL(key);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		return coll.set(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		return coll.setEL(key, value);
	}

	@Override
	public int size() {
		return coll.size();
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return coll.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return coll.keysAsStringIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return coll.entryIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return coll.valueIterator();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		return coll.toDumpData(pageContext, maxlevel, properties);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return coll.castToBooleanValue();
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return coll.castToDoubleValue();
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return coll.castToDateTime();
	}

	@Override
	public String castToString() throws PageException {
		return coll.castToString();
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return coll.compareTo(b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return coll.compareTo(dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return coll.compareTo(d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return coll.compareTo(str);
	}

	@Override
	public Object getEmbededObject(Object defaultValue) {
		return coll;
	}

	@Override
	public Object getEmbededObject() throws PageException {
		return coll;
	}

	/**
	 * @return
	 */
	public Collection getCollection() {
		return coll;
	}

	@Override
	public int getType() {
		if (coll instanceof StructSupport) return ((StructSupport) coll).getType();
		return Struct.TYPE_REGULAR;
	}
}