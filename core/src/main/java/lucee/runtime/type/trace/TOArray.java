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

import java.util.Comparator;
import java.util.List;

import lucee.runtime.debug.Debugger;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;

public class TOArray extends TOCollection implements Array {

	private static final long serialVersionUID = 5130217962217368552L;

	private final Array arr;

	protected TOArray(Debugger debugger, Array arr, int type, String category, String text) {
		super(debugger, arr, type, category, text);
		this.arr = arr;
	}

	@Override
	public int getDimension() {
		log();
		return arr.getDimension();
	}

	@Override
	public Object get(int key, Object defaultValue) {
		log("" + key);
		return arr.get(key, defaultValue);
		// return TraceObjectSupport.toTraceObject(debugger,arr.get(key, defaultValue),type,category,text);
	}

	@Override
	public Object getE(int key) throws PageException {
		log("" + key);
		return arr.getE(key);
		// return TraceObjectSupport.toTraceObject(debugger,arr.getE(key),type,category,text);
	}

	@Override
	public Object setEL(int key, Object value) {
		log("" + key, value);
		return arr.setEL(key, value);
		// return TraceObjectSupport.toTraceObject(debugger,arr.setEL(key, value),type,category,text);
	}

	@Override
	public Object setE(int key, Object value) throws PageException {
		log("" + key, value);
		return arr.setEL(key, value);
		// return TraceObjectSupport.toTraceObject(debugger,arr.setEL(key, value),type,category,text);
	}

	@Override
	public int[] intKeys() {
		log();
		return arr.intKeys();
	}

	@Override
	public boolean insert(int key, Object value) throws PageException {
		log("" + key);
		return arr.insert(key, value);
	}

	@Override
	public Object append(Object o) throws PageException {
		log(o.toString());
		return arr.append(o);
		// return TraceObjectSupport.toTraceObject(debugger,arr.append(o),type,category,text);
	}

	@Override
	public Object appendEL(Object o) {
		log(o.toString());
		return arr.appendEL(o);
		// return TraceObjectSupport.toTraceObject(debugger,arr.appendEL(o),type,category,text);
	}

	@Override
	public Object prepend(Object o) throws PageException {
		log();
		return arr.prepend(o);
		// return TraceObjectSupport.toTraceObject(debugger,arr.prepend(o),type,category,text);
	}

	@Override
	public void resize(int to) throws PageException {
		log();
		arr.resize(to);
	}

	@Override
	public void sort(String sortType, String sortOrder) throws PageException {
		log();
		arr.sort(sortType, sortOrder);
	}

	@Override
	public void sortIt(Comparator comp) {
		log();
		arr.sortIt(comp);
	}

	@Override
	public Object[] toArray() {
		log();
		return arr.toArray();
	}

	@Override
	public List toList() {
		log();
		return arr.toList();
	}

	@Override
	public Object removeE(int key) throws PageException {
		log("" + key);
		return arr.removeE(key);
		// return TraceObjectSupport.toTraceObject(debugger,arr.removeE(key),type,category,text);
	}

	@Override
	public Object removeEL(int key) {
		log("" + key);
		return arr.removeEL(key);
		// return TraceObjectSupport.toTraceObject(debugger,arr.removeEL(key),type,category,text);
	}

	@Override
	public Object remove(Collection.Key key, Object defaultValue) {
		log("" + key);
		return arr.remove(key, defaultValue);
	}

	@Override
	public boolean containsKey(int key) {
		log("" + key);
		return arr.containsKey(key);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		log();
		return new TOArray(debugger, (Array) Duplicator.duplicate(arr, deepCopy), type, category, text);
	}

	@Override
	public java.util.Iterator<Object> getIterator() {
		return valueIterator();
	}

}