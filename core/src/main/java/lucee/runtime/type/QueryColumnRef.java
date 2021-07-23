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
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.ListUtil;

/**
 * Recordcount Query Column
 */
public final class QueryColumnRef implements QueryColumn {

	private Query query;
	private Collection.Key columnName;
	private int type;

	/**
	 * Constructor of the class
	 * 
	 * @param query
	 * @param columnName
	 * @param type
	 */
	public QueryColumnRef(Query query, Collection.Key columnName, int type) {
		this.query = query;
		this.columnName = columnName;
		this.type = type;
	}

	@Override
	public Object remove(int row) throws DatabaseException {
		throw new DatabaseException("can't remove " + columnName + " at row " + row + " value from Query", null, null, null);
	}

	@Override
	public Object removeEL(int row) {
		return query.getAt(columnName, row, null);
	}

	@Override
	public Object get(int row) throws PageException {
		return query.getAt(columnName, row);
	}

	/**
	 * touch a value, means if key dosent exist, it will created
	 * 
	 * @param row
	 * @return matching value or created value
	 * @throws PageException
	 */
	public Object touch(int row) throws PageException {
		Object _null = NullSupportHelper.NULL();
		Object o = query.getAt(columnName, row, _null);
		if (o != _null) return o;
		return query.setAt(columnName, row, new StructImpl());
	}

	public Object touchEL(int row) {
		Object _null = NullSupportHelper.NULL();
		Object o = query.getAt(columnName, row, _null);
		if (o != _null) return o;
		return query.setAtEL(columnName, row, new StructImpl());
	}

	@Override
	public Object get(int row, Object defaultValue) {
		return query.getAt(columnName, row, defaultValue);
	}

	@Override
	public Object set(int row, Object value) throws DatabaseException {
		throw new DatabaseException("can't change " + columnName + " value from Query", null, null, null);
	}

	@Override
	public Object setEL(int row, Object value) {
		return query.getAt(columnName, row, null);
	}

	@Override
	public void add(Object value) {
	}

	@Override
	public void addRow(int count) {
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public String getTypeAsString() {
		return QueryImpl.getColumTypeName(getType());
	}

	@Override
	public void cutRowsTo(int maxrows) {
	}

	@Override
	public int size() {
		return query.size();
	}

	@Override
	public Collection.Key[] keys() {
		Collection.Key[] k = new Collection.Key[size()];
		for (int i = 1; i <= k.length; i++) {
			k[i - 1] = KeyImpl.init(Caster.toString(i));
		}
		return k;
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		throw new DatabaseException("can't remove " + key + " from Query", null, null, null);
	}

	@Override
	public Object removeEL(Collection.Key key) {
		return get(key, null);
	}

	@Override
	public Object remove(Collection.Key key, Object defaultValue) {
		return get(key, defaultValue);
	}

	@Override
	public void clear() {
	}

	@Override
	public Object get(String key) throws PageException {
		return get(Caster.toIntValue(key));
	}

	@Override
	public Object get(Collection.Key key) throws PageException {
		return get(Caster.toIntValue(key.getString()));
	}

	@Override
	public Object get(String key, Object defaultValue) {
		return get(Caster.toIntValue(key, query.getCurrentrow(ThreadLocalPageContext.get().getId())), defaultValue);
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		return get(Caster.toIntValue(key, query.getCurrentrow(ThreadLocalPageContext.get().getId())), defaultValue);
	}

	@Override
	public Object set(String key, Object value) throws PageException {
		return set(Caster.toIntValue(key), value);
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		return set(Caster.toIntValue(key), value);
	}

	@Override
	public Object setEL(String key, Object value) {
		return setEL(Caster.toIntValue(key, query.getCurrentrow(ThreadLocalPageContext.get().getId())), value);
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		return setEL(Caster.toIntValue(key, query.getCurrentrow(ThreadLocalPageContext.get().getId())), value);
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
		return query.getColumn(columnName, null).valueIterator();
	}

	@Override
	public boolean containsKey(String key) {
		Object _null = NullSupportHelper.NULL();
		return get(key, _null) != _null;
	}

	@Override
	public boolean containsKey(Collection.Key key) {
		Object _null = NullSupportHelper.NULL();
		return get(key, _null) != _null;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return DumpUtil.toDumpData(get(query.getCurrentrow(pageContext.getId()), null), pageContext, maxlevel, dp);
	}

	@Override
	public String castToString() throws PageException {
		return Caster.toString(get(query.getCurrentrow(ThreadLocalPageContext.get().getId())));
	}

	@Override
	public String castToString(String defaultValue) {
		Object _null = NullSupportHelper.NULL();
		Object value = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null);
		if (value == _null) return defaultValue;
		return Caster.toString(value, defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return Caster.toBooleanValue(get(query.getCurrentrow(ThreadLocalPageContext.get().getId())));
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		Object _null = NullSupportHelper.NULL();
		Object value = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null);
		if (value == _null) return defaultValue;
		return Caster.toBoolean(value, defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return Caster.toDoubleValue(get(query.getCurrentrow(ThreadLocalPageContext.get().getId())));
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		Object _null = NullSupportHelper.NULL();
		Object value = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null);
		if (value == _null) return defaultValue;
		return Caster.toDoubleValue(value, true, defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return Caster.toDate(get(query.getCurrentrow(ThreadLocalPageContext.get().getId())), null);
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		Object _null = NullSupportHelper.NULL();
		Object value = get(query.getCurrentrow(ThreadLocalPageContext.get().getId()), _null);
		if (value == _null) return defaultValue;
		return DateCaster.toDateAdvanced(value, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return Operator.compare(castToBooleanValue(), b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return Operator.compare((Date) castToDateTime(), (Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return Operator.compare(castToDoubleValue(), d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return Operator.compare(castToString(), str);
	}

	@Override
	public String getKeyAsString() throws PageException {
		return columnName.toString();
	}

	@Override
	public Collection.Key getKey() throws PageException {
		return columnName;
	}

	@Override
	public Object get(PageContext pc) throws PageException {
		return get(query.getCurrentrow(pc.getId()));
	}

	@Override
	public Object get(PageContext pc, Object defaultValue) {
		return get(query.getCurrentrow(pc.getId()), defaultValue);
	}

	@Override
	public Object removeRow(int row) throws DatabaseException {
		throw new DatabaseException("can't remove row from Query", null, null, null);
	}

	@Override
	public Object touch(PageContext pc) throws PageException {
		return touch(query.getCurrentrow(pc.getId()));
	}

	@Override
	public Object touchEL(PageContext pc) {
		return touchEL(query.getCurrentrow(pc.getId()));
	}

	@Override
	public Object set(PageContext pc, Object value) throws PageException {
		return set(query.getCurrentrow(pc.getId()), value);
	}

	@Override
	public Object setEL(PageContext pc, Object value) {
		return setEL(query.getCurrentrow(pc.getId()), value);
	}

	@Override
	public Object remove(PageContext pc) throws PageException {
		return remove(query.getCurrentrow(pc.getId()));
	}

	@Override
	public Object removeEL(PageContext pc) {
		return removeEL(query.getCurrentrow(pc.getId()));
	}

	@Override
	public Object getParent() {
		return query;
	}

	@Override
	public Object clone() {
		QueryColumn clone = new QueryColumnRef(query, columnName, type);
		return clone;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		// MUST muss deepCopy checken
		QueryColumn clone = new QueryColumnRef(query, columnName, type);
		return clone;
	}

	@Override
	public java.util.Iterator<String> getIterator() {
		return keysAsStringIterator();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Collection)) return false;
		return CollectionUtil.equals(this, (Collection) obj);
	}

	/**
	 * This method was added for ACF compatibility per LDEV-1142 and should be avoided if cross engine
	 * code is not required. Use instead Query.columnArray() or Query.columnList().listToArray().
	 * 
	 * @return an Array of the names of columns
	 * @throws PageException
	 */
	public Array listToArray() throws PageException {

		if (this.query instanceof QueryImpl) return ListUtil.listToArray(((QueryImpl) this.query).getColumnlist(false, ", "), ",");

		throw new ApplicationException("Query is not of type QueryImpl. Use instead Query.columnArray() or Query.columnList().listToArray().");
	}

	/*
	 * @Override public int hashCode() { return CollectionUtil.hashCode(this); }
	 */
}