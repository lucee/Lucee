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
import lucee.runtime.config.ConfigServer;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.StructSupport;

/**
 * d
 * 
 */
public final class ClusterNotSupported extends StructSupport implements Cluster {

	private static final String NOT_SUPPORTED = "to enable the cluster scope, please install a cluster scope implementation with the help of the extension manager";

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
		return null;
	}

	@Override
	public Object remove(Key key) throws PageException {
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public void clear() {
	}

	@Override
	public final Object get(Collection.Key key) throws ExpressionException {
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key) throws ExpressionException {
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public final Object get(Collection.Key key, Object defaultValue) {
		return defaultValue;
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		return defaultValue;
	}

	@Override
	public Object set(Key key, Object value) throws ExpressionException {
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		return null;
	}

	@Override
	public void setEntry(ClusterEntry entry) {
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return null;
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return null;
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return null;
	}

	@Override
	public Iterator<Object> valueIterator() {
		return null;
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
		throw new PageRuntimeException(new ExpressionException(NOT_SUPPORTED));
		// return new SimpleDumpData(NOT_SUPPORTED);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new ClusterNotSupported();
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
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

	@Override
	public boolean castToBooleanValue() throws ExpressionException {
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() throws ExpressionException {
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	@Override
	public DateTime castToDateTime() throws ExpressionException {
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	@Override
	public int getType() {
		return SCOPE_CLUSTER;
	}

	@Override
	public String getTypeAsString() {
		return "Cluster";
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException(NOT_SUPPORTED);
	}

	@Override
	public void broadcast() {
		// print.out("Cluster#broadcast()");
	}

	@Override
	public void init(ConfigServer configServer) {
	}

}
