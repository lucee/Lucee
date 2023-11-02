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
package lucee.runtime.type.scope;

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.java.JavaObject;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.it.ValueIterator;
import lucee.runtime.type.util.StructSupport;

public final class ObjectStruct extends StructSupport implements Struct, Objects {

	private JavaObject jo;

	public ObjectStruct(Object o) {
		if (o instanceof JavaObject) this.jo = (JavaObject) o;
		else this.jo = new JavaObject(ThreadLocalPageContext.get().getVariableUtil(), o);
	}

	public ObjectStruct(JavaObject jo) {
		this.jo = jo;
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] arguments) throws PageException {
		return jo.call(pc, methodName, arguments);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		return jo.callWithNamedValues(pc, methodName, args);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		return jo.get(pc, key);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		return jo.get(pc, key, defaultValue);
	}

	public boolean isInitalized() {
		return jo.isInitalized();
	}

	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		return jo.set(pc, propertyName, value);
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		return jo.setEL(pc, propertyName, value);
	}

	@Override
	public void clear() {
		// throw new PageRuntimeException(new ExpressionException("can't clear fields from object
		// ["+objects.getClazz().getName()+"]"));
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		throw new PageRuntimeException(new ExpressionException("can't clone object of type [" + jo.getClazz().getName() + "]"));
		// return null;
	}

	@Override
	public final boolean containsKey(Key key) {
		return Reflector.hasPropertyIgnoreCase(jo.getClazz(), key.getString());
	}

	@Override
	public final boolean containsKey(PageContext pc, Key key) {
		return Reflector.hasPropertyIgnoreCase(jo.getClazz(), key.getString());
	}

	@Override
	public Object get(Key key) throws PageException {
		return jo.get(ThreadLocalPageContext.get(), key);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return jo.get(ThreadLocalPageContext.get(), key, defaultValue);
	}

	@Override
	public Key[] keys() {
		String[] strKeys = Reflector.getPropertyKeys(jo.getClazz());
		Key[] keys = new Key[strKeys.length];
		for (int i = 0; i < strKeys.length; i++) {
			keys[i] = KeyImpl.init(strKeys[i]);
		}
		return keys;
	}

	@Override
	public Object remove(Key key) throws PageException {
		throw new ExpressionException("can't remove field [" + key.getString() + "] from object [" + jo.getClazz().getName() + "]");
	}

	@Override
	public Object removeEL(Key key) {
		return null;
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		return jo.set(ThreadLocalPageContext.get(), key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		return jo.setEL(ThreadLocalPageContext.get(), key, value);
	}

	@Override
	public int size() {
		return keys().length;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return jo.toDumpData(pageContext, maxlevel, dp);
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return new KeyIterator(keys());
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return new StringIterator(keys());
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}

	@Override
	public Iterator<Object> valueIterator() {
		return new ValueIterator(this, keys());
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return jo.castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return jo.castToBoolean(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return jo.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return jo.castToDateTime(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return jo.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return jo.castToDoubleValue(defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return jo.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return jo.castToString(defaultValue);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return jo.compareTo(str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return jo.compareTo(b);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return jo.compareTo(d);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return jo.compareTo(dt);
	}

	@Override
	public int getType() {
		return Struct.TYPE_REGULAR;
	}
}