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

import java.util.Date;
import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.ThreadLocalDuplication;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.ref.Reference;
import lucee.runtime.type.util.StructSupport;

public final class SVStruct extends StructSupport implements Reference, Struct {

	private Collection.Key key;
	private StructImpl parent = new StructImpl();

	/**
	 * constructor of the class
	 * 
	 * @param key
	 */
	public SVStruct(Collection.Key key) {
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
		return get(key);
	}

	@Override
	public Object get(PageContext pc, Object defaultValue) {
		return get(key, defaultValue);
	}

	@Override
	public Object set(PageContext pc, Object value) throws PageException {
		return set(key, value);
	}

	@Override
	public Object setEL(PageContext pc, Object value) {
		return setEL(key, value);
	}

	@Override
	public Object remove(PageContext pc) throws PageException {
		return remove(key);
	}

	@Override
	public Object removeEL(PageContext pc) {
		return removeEL(key);
	}

	@Override
	public Object touch(PageContext pc) throws PageException {
		Object o = get(key, null);
		if (o != null) return o;
		return set(key, new StructImpl());
	}

	@Override
	public Object touchEL(PageContext pc) {
		Object o = get(key, null);
		if (o != null) return o;
		return setEL(key, new StructImpl());
	}

	@Override
	public Object getParent() {
		return parent;
	}

	@Override
	public void clear() {
		parent.clear();
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return parent.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return parent.keysAsStringIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}

	@Override
	public Iterator<Object> valueIterator() {
		return parent.valueIterator();
	}

	@Override
	public Collection.Key[] keys() {
		return parent.keys();
	}

	@Override
	public int size() {
		return parent.size();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return parent.toDumpData(pageContext, maxlevel, dp);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBooleanValue(get(key));
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		Object value = get(key, defaultValue);
		if (value == null) return defaultValue;
		return Caster.toBoolean(value, defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDate(get(key), null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		Object value = get(key, defaultValue);
		if (value == null) return defaultValue;
		return DateCaster.toDateAdvanced(value, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(get(key));
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		Object value = get(key, null);
		if (value == null) return defaultValue;
		return Caster.toDoubleValue(value, true, defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return Caster.toString(get(key));
	}

	@Override
	public String castToString(String defaultValue) {
		Object value = get(key, null);
		if (value == null) return defaultValue;

		return Caster.toString(value, defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), castToBooleanValue() ? Boolean.TRUE : Boolean.FALSE, b ? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), (Date) castToDateTime(), (Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(d));
	}

	@Override
	public int compareTo(String str) throws PageException {
		return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), str);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		SVStruct svs = new SVStruct(key);
		boolean inside = ThreadLocalDuplication.set(this, svs);
		try {
			Collection.Key[] keys = keys();
			for (int i = 0; i < keys.length; i++) {
				if (deepCopy) svs.setEL(keys[i], Duplicator.duplicate(get(keys[i], null), deepCopy));
				else svs.setEL(keys[i], get(keys[i], null));
			}
			return svs;
		}
		finally {
			if (!inside) ThreadLocalDuplication.reset();
		}
	}

	@Override
	public final boolean containsKey(Collection.Key key) {
		return parent.containsKey(key);
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
		return parent.containsKey(pc, key);
	}

	@Override
	public final Object get(Collection.Key key) throws PageException {
		return parent.get(key);
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key) throws PageException {
		return parent.get(pc, key);
	}

	@Override
	public final Object get(Collection.Key key, Object defaultValue) {
		return parent.get(key, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		return parent.get(pc, key, defaultValue);
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		return parent.remove(key);
	}

	@Override
	public Object removeEL(Collection.Key key) {
		return parent.removeEL(key);
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		return parent.set(key, value);
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		return parent.setEL(key, value);
	}

	@Override
	public boolean containsValue(Object value) {
		return parent.containsValue(value);
	}

	@Override
	public java.util.Collection values() {
		return parent.values();
	}

	@Override
	public int getType() {
		return parent.getType();
	}

}