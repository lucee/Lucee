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
package lucee.runtime.com;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Iteratorable;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.KeyAsStringIterator;
import lucee.runtime.type.it.ObjectsEntryIterator;

/**
 * 
 */
public final class COMObject implements Objects, Iteratorable {

	private String name;
	private Dispatch dispatch;
	private Variant parent;

	/**
	 * Public Constructor of the class
	 * 
	 * @param dispatch
	 * @throws ExpressionException
	 */
	public COMObject(String dispatch) {
		// if(!SystemUtil.isWindows()) throw new ExpressionException("Com Objects are only supported in
		// Windows Environments");
		this.name = dispatch;
		this.dispatch = new Dispatch(dispatch);
	}

	/**
	 * Private Constructor of the class for sub Objects
	 * 
	 * @param parent
	 * @param dispatch
	 * @param name
	 */
	COMObject(Variant parent, Dispatch dispatch, String name) {
		this.parent = parent;
		this.name = name;
		this.dispatch = dispatch;
	}

	/*
	 * public Object get(PageContext pc, String propertyName) throws PageException { return
	 * COMUtil.toObject(this,Dispatch.call(dispatch,propertyName),propertyName); }
	 */

	@Override
	public Object get(PageContext pc, Collection.Key key) throws PageException {
		return COMUtil.toObject(this, Dispatch.call(dispatch, key.getString()), key.getString());
	}

	/*
	 * public Object get(PageContext pc, String propertyName, Object defaultValue) { return
	 * COMUtil.toObject(this,Dispatch.call(dispatch,propertyName),propertyName,defaultValue); }
	 */

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		return COMUtil.toObject(this, Dispatch.call(dispatch, key.getString()), key.getString(), defaultValue);
	}

	/*
	 * public Object set(PageContext pc, String propertyName, Object value) { return
	 * setEL(pc,propertyName,value); }
	 */

	@Override
	public Object set(PageContext pc, Collection.Key propertyName, Object value) throws PageException {
		Dispatch.put(dispatch, propertyName.getString(), value);
		return value;
	}

	/*
	 * public Object setEL(PageContext pc, String propertyName, Object value) {
	 * Dispatch.put(dispatch,propertyName,value); return value; }
	 */

	@Override
	public Object setEL(PageContext pc, Collection.Key propertyName, Object value) {
		Dispatch.put(dispatch, propertyName.getString(), value);
		return value;
	}

	/*
	 * public Object call(PageContext pc, String methodName, Object[] args) throws PageException {
	 * Object[] arr=new Object[args.length]; for(int i=0;i<args.length;i++) { if(args[i] instanceof
	 * COMObject)arr[i]=((COMObject)args[i]).dispatch; else arr[i]=args[i]; } return
	 * COMUtil.toObject(this,Dispatch.callN(dispatch,methodName,arr),methodName); }
	 */

	@Override
	public Object call(PageContext pc, Collection.Key key, Object[] args) throws PageException {
		String methodName = key.getString();
		Object[] arr = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof COMObject) arr[i] = ((COMObject) args[i]).dispatch;
			else arr[i] = args[i];
		}
		return COMUtil.toObject(this, Dispatch.callN(dispatch, methodName, arr), methodName);
	}

	/*
	 * public Object callWithNamedValues(PageContext pc, String methodName, Struct args) throws
	 * PageException { // TODO gibt es hier eine bessere moeglichkeit? Iterator<Object> it =
	 * args.valueIterator(); List<Object> values=new ArrayList<Object>(); while(it.hasNext()) {
	 * values.add(it.next()); } return call(pc,KeyImpl.init(methodName),values.toArray(new
	 * Object[values.size()])); }
	 */

	@Override
	public Object callWithNamedValues(PageContext pc, Collection.Key key, Struct args) throws PageException {
		String methodName = key.getString();
		Iterator<Object> it = args.valueIterator();
		List<Object> values = new ArrayList<Object>();
		while (it.hasNext()) {
			values.add(it.next());
		}
		return call(pc, KeyImpl.init(methodName), values.toArray(new Object[values.size()]));
	}

	public boolean isInitalized() {
		return true;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable table = new DumpTable("com", "#ff3300", "#ff9966", "#660000");
		table.appendRow(1, new SimpleDumpData("COM Object"), new SimpleDumpData(name));
		return table;
	}

	@Override
	public String castToString() throws ExpressionException {
		throw new ExpressionException("can't cast Com Object to a String");
	}

	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

	@Override
	public boolean castToBooleanValue() throws ExpressionException {
		throw new ExpressionException("can't cast Com Object to a boolean value");
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() throws ExpressionException {
		throw new ExpressionException("can't cast Com Object to a number");
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	@Override
	public DateTime castToDateTime() throws ExpressionException {
		throw new ExpressionException("can't cast Com Object to a Date");
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the dispatch.
	 */
	public Dispatch getDispatch() {
		return dispatch;
	}

	/**
	 * @return Returns the parent.
	 */
	public Variant getParent() {
		return parent;
	}

	/**
	 * release the com Object
	 */
	public void release() {
		dispatch.safeRelease();
	}

	@Override
	public int compareTo(boolean b) throws ExpressionException {
		throw new ExpressionException("can't compare Com Object with a boolean value");
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare Com Object with a DateTime Object");
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare Com Object with a numeric value");
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare Com Object with a String");
	}

	public Iterator iterator() {
		return valueIterator();
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return new COMKeyWrapperIterator(this);
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return new KeyAsStringIterator(keyIterator());
	}

	@Override
	public Iterator<Object> valueIterator() {
		return new COMValueWrapperIterator(this);
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new ObjectsEntryIterator(keyIterator(), this);
	}
}