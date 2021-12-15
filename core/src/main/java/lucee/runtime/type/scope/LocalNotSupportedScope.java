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

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.StructSupport;

/**
 * 
 */
public final class LocalNotSupportedScope extends StructSupport implements Scope, Local {

	private static final long serialVersionUID = 6670210379924188569L;

	private static LocalNotSupportedScope instance = new LocalNotSupportedScope();
	private boolean bind;

	private LocalNotSupportedScope() {
	}

	public static LocalNotSupportedScope getInstance() {
		return instance;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Collection.Key[] keys() {
		return null;
	}

	@Override
	public Object removeEL(Key key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object remove(Key key) throws PageException {
		throw new ExpressionException("Unsupported Context for Local Scope", "Can't invoke key " + key + ", Local Scope can only be invoked inside a Function");
	}

	@Override
	public void clear() {
	}

	@Override
	public Object get(Collection.Key key) throws ExpressionException {
		throw new ExpressionException("Unsupported Context for Local Scope", "Can't invoke key " + key.getString() + ", Local Scope can only be invoked inside a Function");
	}

	@Override
	public Object get(PageContext pc, Collection.Key key) throws ExpressionException {
		throw new ExpressionException("Unsupported Context for Local Scope", "Can't invoke key " + key.getString() + ", Local Scope can only be invoked inside a Function");
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		return defaultValue;
	}

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		return defaultValue;
	}

	@Override
	public Object set(Key key, Object value) throws ExpressionException {
		throw new ExpressionException("Unsupported Context for Local Scope", "Can't invoke key " + key.getString() + ", Local Scope can only be invoked inside a Function");
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		return null;
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		throw new PageRuntimeException(new ExpressionException("Unsupported Context for Local Scope", "Local Scope can only be invoked inside a Function"));
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		throw new PageRuntimeException(new ExpressionException("Unsupported Context for Local Scope", "Local Scope can only be invoked inside a Function"));
	}

	@Override
	public Iterator<Object> valueIterator() {
		throw new PageRuntimeException(new ExpressionException("Unsupported Context for Local Scope", "Local Scope can only be invoked inside a Function"));
	}

	@Override
	public boolean isInitalized() {
		return false;
	}

	@Override
	public void initialize(PageContext pc) {
	}

	@Override
	public void release(PageContext pc) {
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		throw new PageRuntimeException(new ExpressionException("Unsupported Context for Local Scope"));
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new LocalNotSupportedScope();
	}

	@Override
	public final boolean containsKey(Collection.Key key) {
		return false;
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public java.util.Collection values() {
		return null;
	}

	@Override
	public String castToString() throws ExpressionException {
		throw new ExpressionException("Unsupported Context for Local Scope");
	}

	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

	@Override
	public boolean castToBooleanValue() throws ExpressionException {
		throw new ExpressionException("Unsupported Context for Local Scope");
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() throws ExpressionException {
		throw new ExpressionException("Unsupported Context for Local Scope");
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	@Override
	public DateTime castToDateTime() throws ExpressionException {
		throw new ExpressionException("Unsupported Context for Local Scope");
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	@Override
	public int getType() {
		return SCOPE_LOCAL;
	}

	@Override
	public String getTypeAsString() {
		return "local";
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("Unsupported Context for Local Scope");
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		throw new ExpressionException("Unsupported Context for Local Scope");
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("Unsupported Context for Local Scope");
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("Unsupported Context for Local Scope");
	}

	@Override
	public boolean isBind() {
		return bind;
	}

	@Override
	public void setBind(boolean bind) {
		this.bind = bind;
	}
}