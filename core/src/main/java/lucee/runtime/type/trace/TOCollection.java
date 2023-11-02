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
package lucee.runtime.type.trace;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.debug.Debugger;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.dt.DateTime;

abstract class TOCollection extends TOObjects implements Collection {

	private static final long serialVersionUID = -6006915508424163880L;

	private Collection coll;

	protected TOCollection(Debugger debugger, Collection coll, int type, String category, String text) {
		super(debugger, coll, type, category, text);
		this.coll = coll;
	}

	@Override
	public Object clone() {
		return duplicate(true);
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		log();
		return coll.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		log();
		return coll.keysAsStringIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		log();
		return coll.entryIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		log();
		return coll.valueIterator();
	}

	@Override
	public String castToString() throws PageException {
		log();
		return coll.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		log();
		return coll.castToString(defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		log();
		return coll.castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		log();
		return coll.castToBoolean(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		log();
		return coll.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		log();
		return coll.castToDoubleValue(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		log();
		return new TODateTime(debugger, coll.castToDateTime(), type, category, text);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		log();
		return new TODateTime(debugger, coll.castToDateTime(defaultValue), type, category, text);
	}

	@Override
	public int compareTo(String str) throws PageException {
		log();
		return coll.compareTo(str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		log();
		return coll.compareTo(b);
	}

	@Override
	public int compareTo(double d) throws PageException {
		log();
		return coll.compareTo(d);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		log();
		return coll.compareTo(dt);
	}

	@Override
	public int size() {
		log();
		return coll.size();
	}

	@Override
	public Key[] keys() {
		log();
		return coll.keys();
	}

	@Override
	public Object remove(Key key) throws PageException {
		log(key.getString());
		return coll.remove(key);
		// return TraceObjectSupport.toTraceObject(debugger,coll.remove(key),type,category,text);
	}

	@Override
	public Object remove(Key key, Object defaultValue) {
		log(key.getString());
		return coll.remove(key, defaultValue);
		// return TraceObjectSupport.toTraceObject(debugger,coll.remove(key),type,category,text);
	}

	@Override
	public Object removeEL(Key key) {
		log(key.getString());
		return coll.removeEL(key);
		// return TraceObjectSupport.toTraceObject(debugger,coll.removeEL(key),type,category,text);
	}

	@Override
	public void clear() {
		log();
		coll.clear();
	}

	@Override
	public Object get(String key) throws PageException {
		log(key);
		return coll.get(KeyImpl.init(key));
		// return TraceObjectSupport.toTraceObject(debugger,coll.get(key),type,category,text);
	}

	@Override
	public Object get(Key key) throws PageException {
		log(key.getString());
		return coll.get(key);
		// return TraceObjectSupport.toTraceObject(debugger,coll.get(key),type,category,text);
	}

	@Override
	public Object get(String key, Object defaultValue) {
		log(key);
		return coll.get(key, defaultValue);
		// return TraceObjectSupport.toTraceObject(debugger,coll.get(key, defaultValue),type,category,text);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		log(key.getString());
		return coll.get(key, defaultValue);
		// return TraceObjectSupport.toTraceObject(debugger,coll.get(key,defaultValue),type,category,text);
	}

	@Override
	public Object set(String key, Object value) throws PageException {
		log(key, value);
		return coll.set(key, value);
		// return TraceObjectSupport.toTraceObject(debugger,coll.set(key, value),type,category,text);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		log(key.getString(), value);
		return coll.set(key, value);
		// return TraceObjectSupport.toTraceObject(debugger,coll.set(key, value),type,category,text);
	}

	@Override
	public Object setEL(String key, Object value) {
		log(key, value);
		return coll.setEL(key, value);
		// return TraceObjectSupport.toTraceObject(debugger,coll.setEL(key, value),type,category,text);
	}

	@Override
	public Object setEL(Key key, Object value) {
		log(key.getString(), value);
		return coll.setEL(key, value);
		// return TraceObjectSupport.toTraceObject(debugger,coll.setEL(key, value),type,category,text);
	}

	@Override
	public boolean containsKey(String key) {
		log(key);
		return coll.containsKey(key);
	}

	@Override
	public boolean containsKey(Key key) {
		log(key.getString());
		return coll.containsKey(key);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		log();
		return coll.toDumpData(pageContext, maxlevel, properties);
	}
}