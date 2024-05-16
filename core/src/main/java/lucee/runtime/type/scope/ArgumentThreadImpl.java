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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lucee.commons.lang.CFTypes;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.MemberUtil;

public final class ArgumentThreadImpl implements Argument {

	private final Struct sct;

	public ArgumentThreadImpl(Struct sct) {
		this.sct = sct;
	}

	@Override
	public Object getFunctionArgument(String key, Object defaultValue) {
		return sct.get(key, defaultValue);
	}

	@Override
	public Object getFunctionArgument(Key key, Object defaultValue) {
		return sct.get(key, defaultValue);
	}

	@Override
	public boolean containsFunctionArgumentKey(Key key) {
		return sct.containsKey(key);
	}

	@Override
	public Object setArgument(Object obj) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFunctionArgumentNames(Set functionArgumentNames) {

	}

	@Override
	public boolean insert(int index, String key, Object value) throws PageException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBind() {
		return true;
	}

	@Override
	public void setBind(boolean bind) {

	}

	@Override
	public int getType() {
		return SCOPE_ARGUMENTS;
	}

	@Override
	public String getTypeAsString() {
		return "arguments";
	}

	@Override
	public void initialize(PageContext pc) {
	}

	@Override
	public boolean isInitalized() {
		return true;
	}

	@Override
	public void release(PageContext pc) {
	}

	@Override
	public void clear() {
		sct.clear();
	}

	@Override
	public boolean containsKey(String key) {
		return sct.containsKey(key);
	}

	@Override
	public boolean containsKey(Key key) {
		return sct.containsKey(key);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new ArgumentThreadImpl((Struct) Duplicator.duplicate(sct, deepCopy));
	}

	@Override
	public Object get(String key) throws PageException {
		return get(KeyImpl.init(key));
	}

	@Override
	public Object get(Key key) throws PageException {
		return sct.get(key);
	}

	@Override
	public Object get(String key, Object defaultValue) {
		return sct.get(key, defaultValue);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return sct.get(key, defaultValue);
	}

	@Override
	public Key[] keys() {
		return sct.keys();
	}

	@Override
	public Object remove(Key key) throws PageException {
		return sct.remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		return sct.removeEL(key);
	}

	@Override
	public Object remove(Key key, Object defaultValue) {
		return sct.remove(key, defaultValue);
	}

	@Override
	public Object set(String key, Object value) throws PageException {
		return sct.set(key, value);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		return sct.set(key, value);
	}

	@Override
	public Object setEL(String key, Object value) {
		return sct.setEL(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		return sct.setEL(key, value);
	}

	@Override
	public int size() {
		return sct.size();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		return sct.toDumpData(pageContext, maxlevel, properties);
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return sct.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return sct.keysAsStringIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return sct.entryIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return sct.valueIterator();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return sct.castToBoolean(defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return sct.castToBooleanValue();
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return sct.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return sct.castToDateTime(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return sct.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return sct.castToDoubleValue(defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return sct.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return sct.castToString(defaultValue);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return sct.compareTo(str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return sct.compareTo(b);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return sct.compareTo(d);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return sct.compareTo(dt);
	}

	@Override
	public boolean containsKey(Object key) {
		return sct.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return sct.containsValue(value);
	}

	@Override
	public Set entrySet() {
		return sct.entrySet();
	}

	@Override
	public Object get(Object key) {
		return sct.get(key);
	}

	@Override
	public boolean isEmpty() {
		return sct.isEmpty();
	}

	@Override
	public Set keySet() {
		return sct.keySet();
	}

	@Override
	public Object put(Object key, Object value) {
		return sct.put(key, value);
	}

	@Override
	public void putAll(Map m) {
		sct.putAll(m);
	}

	@Override
	public Object remove(Object key) {
		return sct.remove(key);
	}

	@Override
	public java.util.Collection values() {
		return sct.values();
	}

	@Override
	public Object append(Object o) throws PageException {
		throw new CasterException(sct, "Array");
	}

	@Override
	public Object appendEL(Object o) {
		throw new PageRuntimeException(new CasterException(sct, "Array"));
	}

	@Override
	public boolean containsKey(int key) {
		return sct.containsKey(ArgumentIntKey.init(key));
	}

	@Override
	public Object get(int key, Object defaultValue) {
		return sct.get(ArgumentIntKey.init(key), defaultValue);
	}

	@Override
	public int getDimension() {
		throw new PageRuntimeException(new CasterException(sct, "Array"));
	}

	@Override
	public Object getE(int key) throws PageException {
		return sct.get(KeyImpl.init(Caster.toString(key)));
	}

	@Override
	public boolean insert(int key, Object value) throws PageException {
		throw new CasterException(sct, "Array");
	}

	@Override
	public int[] intKeys() {
		throw new PageRuntimeException(new CasterException(sct, "Array"));
	}

	@Override
	public Object prepend(Object o) throws PageException {
		throw new CasterException(sct, "Array");
	}

	@Override
	public Object removeE(int key) throws PageException {
		return sct.remove(KeyImpl.init(Caster.toString(key)));
	}

	@Override
	public Object removeEL(int key) {
		return sct.removeEL(KeyImpl.init(Caster.toString(key)));
	}

	@Override
	public void resize(int to) throws PageException {
		throw new CasterException(sct, "Array");
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @throws PageException
	 */
	@Override
	public Object setE(int key, Object value) throws PageException {
		return sct.set(Caster.toString(key), value);
	}

	@Override
	public Object setEL(int key, Object value) {
		return sct.setEL(Caster.toString(key), value);
	}

	@Override
	public void sort(String sortType, String sortOrder) throws PageException {
		throw new CasterException(sct, "Array");
	}

	@Override
	public void sortIt(Comparator com) {
		throw new PageRuntimeException(new CasterException(sct, "Array"));
	}

	@Override
	public Object[] toArray() {
		try {
			return Caster.toArray(sct).toArray();
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public List toList() {
		try {
			return Caster.toArray(sct).toList();
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public Object clone() {
		return duplicate(true);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		return get(key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		return get(key);
	}

	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		return set(propertyName, value);
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		return setEL(propertyName, value);
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] args) throws PageException {
		return MemberUtil.call(pc, this, methodName, args, new short[] { CFTypes.TYPE_ARRAY }, new String[] { "array" });
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		return MemberUtil.callWithNamedValues(pc, this, methodName, args, CFTypes.TYPE_ARRAY, "array");
	}

	@Override
	public java.util.Iterator<String> getIterator() {
		return keysAsStringIterator();
	}

}