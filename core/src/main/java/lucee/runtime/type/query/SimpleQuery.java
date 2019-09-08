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
package lucee.runtime.type.query;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.db.DataSourceUtil;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.SQL;
import lucee.runtime.db.SQLCaster;
import lucee.runtime.db.SQLItem;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.ArrayInt;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.QueryColumnRef;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.it.CollectionIterator;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.ForEachQueryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.QueryUtil;

public class SimpleQuery implements Query, ResultSet, Objects, QueryResult {

	static final Object DEFAULT_VALUE = new Object();
	private Statement stat;
	private ResultSet res;
	private ResultSetMetaData meta;
	private Collection.Key[] columnNames;
	private Map<String, SimpleQueryColumn> columns = new LinkedHashMap<String, SimpleQueryColumn>();
	private int[] _types;

	private String name;
	private TemplateLine templateLine;
	private SQL sql;
	private long exeTime;
	private int recordcount;
	private ArrayInt arrCurrentRow = new ArrayInt();
	private String cacheType;
	private int updateCount;
	private DatasourceConnection dc;

	public SimpleQuery(PageContext pc, DatasourceConnection dc, SQL sql, int maxrow, int fetchsize, TimeSpan timeout, String name, TemplateLine templateLine, TimeZone tz)
			throws PageException {
		this.dc = dc;
		this.name = name;
		this.templateLine = templateLine;
		this.sql = sql;

		// ResultSet result=null;
		stat = null;
		// check SQL Restrictions
		if (dc.getDatasource().hasSQLRestriction()) {
			QueryUtil.checkSQLRestriction(dc, sql);
		}

		// Stopwatch stopwatch=new Stopwatch(Stopwatch.UNIT_NANO);
		// stopwatch.start();
		long start = System.nanoTime();
		boolean hasResult = false;
		try {
			SQLItem[] items = sql.getItems();
			if (items.length == 0) {
				stat = dc.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				setAttributes(stat, maxrow, fetchsize, timeout);
				// some driver do not support second argument
				hasResult = stat.execute(sql.getSQLString());
			}
			else {
				// some driver do not support second argument
				PreparedStatement preStat = dc.getPreparedStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				stat = preStat;
				setAttributes(preStat, maxrow, fetchsize, timeout);
				setItems(pc, tz, preStat, items);
				hasResult = preStat.execute();
			}
			ResultSet res;

			do {
				if (hasResult) {
					res = stat.getResultSet();
					init(res);
					break;
				}
				throw new ApplicationException("Simple queries can only be used for queries returning a resultset");
			}
			while (true);
		}
		catch (SQLException e) {
			throw new DatabaseException(e, sql, dc);
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
			throw Caster.toPageException(e);
		}
		exeTime = System.nanoTime() - start;

		((PageContextImpl) pc).registerLazyStatement(stat);
	}

	public DatasourceConnection getDc() {
		return dc;
	}

	private void setAttributes(Statement stat, int maxrow, int fetchsize, TimeSpan timeout) throws SQLException {
		if (maxrow > -1) stat.setMaxRows(maxrow);
		if (fetchsize > 0) stat.setFetchSize(fetchsize);
		if (timeout != null && ((int) timeout.getSeconds()) > 0) DataSourceUtil.setQueryTimeoutSilent(stat, (int) timeout.getSeconds());
	}

	private void setItems(PageContext pc, TimeZone tz, PreparedStatement preStat, SQLItem[] items) throws DatabaseException, PageException, SQLException {
		for (int i = 0; i < items.length; i++) {
			SQLCaster.setValue(pc, tz, preStat, i + 1, items[i]);
		}
	}

	private void init(ResultSet res) throws SQLException {
		this.res = res;
		this.meta = res.getMetaData();

		// init columns
		int columncount = meta.getColumnCount();
		List<Key> tmpKeys = new ArrayList<Key>();
		// List<Integer> tmpTypes=new ArrayList<Integer>();
		// int count=0;
		Collection.Key key;
		String columnName;
		int type;
		for (int i = 0; i < columncount; i++) {
			try {
				columnName = meta.getColumnName(i + 1);
				type = meta.getColumnType(i + 1);
			}
			catch (SQLException e) {
				throw toRuntimeExc(e);
			}
			if (StringUtil.isEmpty(columnName)) columnName = "column_" + i;
			key = KeyImpl.init(columnName);
			int index = tmpKeys.indexOf(key);
			if (index == -1) {
				// mappings.put(key.getLowerString(), Caster.toInteger(i+1));
				tmpKeys.add(key);
				// tmpTypes.add(type);
				columns.put(key.getLowerString(), new SimpleQueryColumn(this, res, key, type, i + 1));

				// count++;
			}

		}
		columnNames = tmpKeys.toArray(new Key[tmpKeys.size()]);

		res.last();
		recordcount = res.getRow();
		res.beforeFirst();
		/*
		 * Iterator<Integer> it = tmpTypes.iterator(); types=new int[tmpTypes.size()]; int index=0;
		 * while(it.hasNext()){ types[index++]=it.next(); }
		 */

	}

	@Override
	public int executionTime() {
		return (int) exeTime;
	}

	@Override

	public int getUpdateCount() {
		return updateCount;
	}

	@Override
	public void setUpdateCount(int updateCount) {
		this.updateCount = updateCount;
	}

	@Override
	public int size() {
		return columnNames.length;
	}

	@Override

	public Key[] keys() {
		return columnNames;
	}

	@Override
	public Object removeEL(Key key) {
		throw notSupported();
	}

	@Override
	public Object remove(Key key, Object defaultValue) {
		throw notSupported();
	}

	@Override
	public Object remove(Key key) throws PageException {
		throw notSupported();
	}

	@Override
	public void clear() {
		throw notSupported();
	}

	@Override

	public Object get(Key key, Object defaultValue) {
		int pid = getPid();
		return getAt(key, getCurrentrow(pid), pid, defaultValue);
	}

	@Override
	public Object get(String key, Object defaultValue) {
		return get(KeyImpl.init(key), defaultValue);
	}

	@Override
	public Object get(String key) throws PageException {
		return get(KeyImpl.init(key));
	}

	@Override

	public Object get(Key key) throws PageException {
		int pid = getPid();
		return getAt(key, getCurrentrow(pid), pid);
	}

	public Object getAt(Key key, int row, int pid, Object defaultValue) {
		char c = key.lowerCharAt(0);
		if (c == 'r') {
			if (key.equals(KeyConstants._RECORDCOUNT)) return new Double(getRecordcount());
		}
		else if (c == 'c') {
			if (key.equals(KeyConstants._CURRENTROW)) return new Double(getCurrentrow(pid));
			else if (key.equals(KeyConstants._COLUMNLIST)) return getColumnlist();
		}

		SimpleQueryColumn column = columns.get(key.getLowerString());
		if (column == null) return null;
		try {
			return column.get(row, defaultValue);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public Object getAt(Key key, int row, int pid) throws PageException {
		Object res = getAt(key, row, pid, DEFAULT_VALUE);
		if (res != DEFAULT_VALUE) return res;
		throw new DatabaseException("key [" + key + "] not found", null, null, null);
	}

	@Override

	public Object getAt(Key key, int row, Object defaultValue) {
		return getAt(key, row, getPid(), defaultValue);
	}

	@Override
	public Object getAt(Key key, int row) throws PageException {
		Object res = getAt(key, row, getPid(), DEFAULT_VALUE);
		if (res != DEFAULT_VALUE) return res;
		throw new DatabaseException("key [" + key + "] not found", null, null, null);
	}

	@Override
	public Object getAt(String key, int row, Object defaultValue) {
		return getAt(KeyImpl.init(key), row, defaultValue);
	}

	@Override

	public Object getAt(String key, int row) throws PageException {
		return getAt(KeyImpl.init(key), row);
	}

	@Override

	public synchronized int removeRow(int row) throws PageException {
		throw notSupported();
	}

	@Override

	public int removeRowEL(int row) {
		throw notSupported();
	}

	@Override

	public QueryColumn removeColumn(String key) throws DatabaseException {
		throw notSupported();
	}

	@Override

	public QueryColumn removeColumn(Key key) throws DatabaseException {
		throw notSupported();
	}

	@Override

	public synchronized QueryColumn removeColumnEL(String key) {
		throw notSupported();
	}

	@Override

	public QueryColumn removeColumnEL(Key key) {
		throw notSupported();
	}

	@Override

	public Object setEL(String key, Object value) {
		throw notSupported();
	}

	@Override

	public Object setEL(Key key, Object value) {
		throw notSupported();
	}

	@Override

	public Object set(String key, Object value) throws PageException {
		throw notSupported();
	}

	@Override

	public Object set(Key key, Object value) throws PageException {
		throw notSupported();
	}

	@Override

	public Object setAt(String key, int row, Object value) throws PageException {
		throw notSupported();
	}

	@Override

	public Object setAt(Key key, int row, Object value) throws PageException {
		throw notSupported();
	}

	@Override

	public Object setAtEL(String key, int row, Object value) {
		throw notSupported();
	}

	@Override

	public Object setAtEL(Key key, int row, Object value) {
		throw notSupported();
	}

	@Override
	public synchronized boolean next() {
		try {
			return next(getPid());
		}
		catch (DatabaseException e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public synchronized boolean next(int pid) throws DatabaseException {
		throwIfClosed();
		if (recordcount >= (arrCurrentRow.set(pid, arrCurrentRow.get(pid, 0) + 1))) {
			return true;
		}
		arrCurrentRow.set(pid, 0);
		return false;
	}

	@Override
	public synchronized void reset() {
		reset(getPid());
	}

	@Override
	public synchronized void reset(int pid) {
		arrCurrentRow.set(pid, 0);
	}

	@Override
	public int getRecordcount() {
		return recordcount;
	}

	@Override
	public int getColumncount() {
		return columnNames == null ? 0 : columnNames.length;
	}

	@Override
	public boolean isEmpty() {
		return recordcount + getColumnCount() == 0;
	}

	@Override

	public synchronized int getCurrentrow(int pid) {
		return arrCurrentRow.get(pid, 1);
	}

	public String getColumnlist(boolean upperCase) {
		Key[] columnNames = keys();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < columnNames.length; i++) {
			if (i > 0) sb.append(',');
			sb.append(upperCase ? columnNames[i].getUpperString() : columnNames[i].getString());
		}
		return sb.toString();
	}

	public String getColumnlist() {
		return getColumnlist(true);
	}

	public boolean go(int index) throws DatabaseException {
		return go(index, getPid());
	}

	@Override
	public boolean go(int index, int pid) throws DatabaseException {
		throwIfClosed();
		if (index > 0 && index <= recordcount) {
			arrCurrentRow.set(pid, index);
			return true;
		}
		arrCurrentRow.set(pid, 0);
		return false;
	}

	/*
	 * public synchronized boolean go(int index) { if(index==getCurrentrow()) return true; try { return
	 * res.absolute(index); } catch (SQLException e) { throw toRuntimeExc(e); } }
	 * 
	 * public boolean go(int index, int pid) { return go(index); }
	 */

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return QueryUtil.toDumpData(this, pageContext, maxlevel, dp);
	}

	@Override

	public void sort(String column) throws PageException {
		throw notSupported();
	}

	@Override

	public void sort(Key column) throws PageException {
		throw notSupported();
	}

	@Override

	public synchronized void sort(String strColumn, int order) throws PageException {
		throw notSupported();
	}

	@Override

	public synchronized void sort(Key keyColumn, int order) throws PageException {
		throw notSupported();
	}

	@Override

	public synchronized boolean addRow(int count) {
		throw notSupported();
	}

	@Override

	public boolean addColumn(String columnName, lucee.runtime.type.Array content) throws DatabaseException {
		throw notSupported();
	}

	@Override

	public boolean addColumn(Key columnName, lucee.runtime.type.Array content) throws PageException {
		throw notSupported();
	}

	@Override

	public synchronized boolean addColumn(String columnName, lucee.runtime.type.Array content, int type) throws DatabaseException {
		throw notSupported();
	}

	@Override

	public boolean addColumn(Key columnName, lucee.runtime.type.Array content, int type) throws DatabaseException {
		throw notSupported();
	}

	@Override

	public Object clone() {
		return cloneQuery(true);
	}

	@Override

	public Collection duplicate(boolean deepCopy) {
		return cloneQuery(deepCopy);
	}

	public QueryImpl cloneQuery(boolean deepCopy) {
		return QueryImpl.cloneQuery(this, deepCopy);
	}

	@Override

	public synchronized int[] getTypes() {
		if (_types == null) {
			_types = new int[columns.size()];
			int i = 0;
			Iterator<Entry<String, SimpleQueryColumn>> it = columns.entrySet().iterator();
			while (it.hasNext()) {
				_types[i++] = it.next().getValue().getType();
			}
		}
		return _types;
	}

	@Override

	public synchronized Map<Collection.Key, String> getTypesAsMap() {
		Map<Collection.Key, String> map = new HashMap<Collection.Key, String>();
		Iterator<SimpleQueryColumn> it = columns.values().iterator();
		SimpleQueryColumn c;
		while (it.hasNext()) {
			c = it.next();
			map.put(c.getKey(), c.getTypeAsString());
		}
		return map;
	}

	@Override

	public QueryColumn getColumn(String key) throws DatabaseException {
		return getColumn(KeyImpl.init(key));
	}

	@Override

	public QueryColumn getColumn(Key key) throws DatabaseException {
		QueryColumn rtn = getColumn(key, null);
		if (rtn != null) return rtn;
		throw new DatabaseException("key [" + key.getString() + "] not found in query, columns are [" + getColumnlist(false) + "]", null, null, null);
	}

	@Override
	public QueryColumn getColumn(String key, QueryColumn defaultValue) {
		return getColumn(KeyImpl.init(key), defaultValue);
	}

	@Override

	public QueryColumn getColumn(Key key, QueryColumn defaultValue) {
		if (key.getString().length() > 0) {
			char c = key.lowerCharAt(0);
			if (c == 'r') {
				if (key.equals(KeyConstants._RECORDCOUNT)) return new QueryColumnRef(this, key, Types.INTEGER);
			}
			else if (c == 'c') {
				if (key.equals(KeyConstants._CURRENTROW)) return new QueryColumnRef(this, key, Types.INTEGER);
				else if (key.equals(KeyConstants._COLUMNLIST)) return new QueryColumnRef(this, key, Types.INTEGER);
			}
			SimpleQueryColumn col = columns.get(key.getLowerString());
			if (col != null) return col;

		}
		return defaultValue;
	}

	@Override

	public synchronized void rename(Key columnName, Key newColumnName) throws ExpressionException {
		throw notSupported();
		// Integer index=mappings.get(columnName);
		// if(index==null) throw new ExpressionException("invalid column name definitions");
		// TODO implement
	}

	@Override
	public String toString() {
		return res.toString();
	}

	@Override
	public void setExecutionTime(long exeTime) {
		throw notSupported();
	}

	public synchronized boolean cutRowsTo(int maxrows) {
		throw notSupported();
	}

	@Override
	public void setCached(boolean isCached) {
		throw notSupported();
	}

	@Override
	public boolean isCached() {
		return false;
	}

	@Override
	public int addRow() {
		throw notSupported();
	}

	public Key getColumnName(int columnIndex) {
		Iterator<SimpleQueryColumn> it = columns.values().iterator();
		SimpleQueryColumn c;
		while (it.hasNext()) {
			c = it.next();
			if (c.getIndex() == columnIndex) return c.getKey();
		}
		return null;
	}

	@Override

	public int getColumnIndex(String coulmnName) {
		SimpleQueryColumn col = columns.get(coulmnName.toLowerCase());
		if (col == null) return -1;
		return col.getIndex();
	}

	@Override

	public String[] getColumns() {
		return getColumnNamesAsString();
	}

	@Override

	public Key[] getColumnNames() {
		Key[] _columns = new Key[columnNames.length];
		for (int i = 0; i < columnNames.length; i++) {
			_columns[i] = columnNames[i];
		}
		return _columns;
	}

	@Override
	public void setColumnNames(Key[] trg) {
		throw notSupported();
	}

	@Override

	public String[] getColumnNamesAsString() {
		String[] _columns = new String[columnNames.length];
		for (int i = 0; i < columnNames.length; i++) {
			_columns[i] = columnNames[i].getString();
		}
		return _columns;
	}

	@Override

	public synchronized String getData(int row, int col) throws IndexOutOfBoundsException {
		try {
			int rowBefore = res.getRow();
			try {
				res.absolute(row);
				if (col < 1 || col > columnNames.length) {
					new IndexOutOfBoundsException("invalid column index to retrieve Data from query, valid index goes from 1 to " + columnNames.length);
				}
				return Caster.toString(get(columnNames[col]));

			}
			finally {
				res.absolute(rowBefore);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw toRuntimeExc(t);
		}
	}

	@Override

	public String getName() {
		return name;
	}

	@Override

	public int getRowCount() {
		return getRecordcount();
	}

	@Override

	public void setData(int row, int col, String value) throws IndexOutOfBoundsException {
		throw notSupported();
	}

	@Override

	public boolean containsKey(String key) {
		return columns.get(key.toLowerCase()) != null;
	}

	@Override

	public boolean containsKey(Key key) {
		return containsKey(key.getString());
	}

	@Override

	public String castToString() throws ExpressionException {
		throw notSupported();
	}

	@Override

	public String castToString(String defaultValue) {
		throw notSupported();
	}

	@Override

	public boolean castToBooleanValue() throws ExpressionException {
		throw notSupported();
	}

	@Override

	public Boolean castToBoolean(Boolean defaultValue) {
		throw notSupported();
	}

	@Override

	public double castToDoubleValue() throws ExpressionException {
		throw notSupported();
	}

	@Override

	public double castToDoubleValue(double defaultValue) {
		throw notSupported();
	}

	@Override

	public DateTime castToDateTime() throws ExpressionException {
		throw notSupported();
	}

	@Override

	public DateTime castToDateTime(DateTime defaultValue) {
		throw notSupported();
	}

	@Override

	public int compareTo(boolean b) throws ExpressionException {
		throw notSupported();
	}

	@Override

	public int compareTo(DateTime dt) throws PageException {
		throw notSupported();
	}

	@Override

	public int compareTo(double d) throws PageException {
		throw notSupported();
	}

	@Override

	public int compareTo(String str) throws PageException {
		throw notSupported();
	}

	@Override
	public synchronized lucee.runtime.type.Array getMetaDataSimple() {
		lucee.runtime.type.Array cols = new ArrayImpl();
		SimpleQueryColumn sqc;
		Struct column;
		Iterator<SimpleQueryColumn> it = columns.values().iterator();
		while (it.hasNext()) {
			sqc = it.next();
			column = new StructImpl();
			column.setEL(KeyConstants._name, sqc.getKey());
			column.setEL("isCaseSensitive", Boolean.FALSE);
			column.setEL("typeName", sqc.getTypeAsString());
			cols.appendEL(column);
		}
		return cols;
	}

	@Override

	public Object getObject(String columnName) throws SQLException {
		return res.getObject(toIndex(columnName));
	}

	@Override

	public Object getObject(int columnIndex) throws SQLException {
		return res.getObject(columnIndex);
	}

	@Override

	public String getString(int columnIndex) throws SQLException {
		return res.getString(columnIndex);
	}

	@Override

	public String getString(String columnName) throws SQLException {
		return res.getString(toIndex(columnName));
	}

	@Override

	public boolean getBoolean(int columnIndex) throws SQLException {
		return res.getBoolean(columnIndex);
	}

	@Override

	public boolean getBoolean(String columnName) throws SQLException {
		return res.getBoolean(toIndex(columnName));
	}

	@Override

	public Object call(PageContext pc, Key methodName, Object[] arguments) throws PageException {
		throw notSupported();
	}

	@Override

	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		throw notSupported();
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		return getAt(key, getCurrentrow(pc.getId()), pc.getId(), defaultValue);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		return getAt(key, getCurrentrow(pc.getId()), pc.getId());
	}

	public boolean isInitalized() {
		return true;
	}

	@Override

	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		throw notSupported();
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		throw notSupported();
	}

	@Override
	public boolean wasNull() {
		try {
			return res.wasNull();
		}
		catch (SQLException e) {
			throw toRuntimeExc(e);
		}
	}

	@Override

	public synchronized boolean absolute(int row) throws SQLException {
		return res.absolute(row);
	}

	@Override

	public synchronized void afterLast() throws SQLException {
		res.afterLast();
	}

	@Override

	public synchronized void beforeFirst() throws SQLException {
		res.beforeFirst();
	}

	@Override

	public synchronized void cancelRowUpdates() throws SQLException {
		res.cancelRowUpdates();
	}

	@Override

	public synchronized void clearWarnings() throws SQLException {
		res.clearWarnings();
	}

	@Override

	public synchronized void close() throws SQLException {
		if (res != null && !res.isClosed()) {
			res.close();
		}
		if (stat != null && !stat.isClosed()) {
			stat.close();
		}
	}

	@Override

	public synchronized void deleteRow() throws SQLException {
		res.deleteRow();
	}

	@Override

	public int findColumn(String columnName) throws SQLException {
		return res.findColumn(columnName);
	}

	@Override

	public synchronized boolean first() throws SQLException {
		return res.first();
	}

	@Override

	public Array getArray(int i) throws SQLException {
		return res.getArray(i);
	}

	@Override

	public Array getArray(String colName) throws SQLException {
		return res.getArray(toIndex(colName));
	}

	@Override

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return res.getAsciiStream(columnIndex);
	}

	@Override

	public InputStream getAsciiStream(String columnName) throws SQLException {
		return res.getAsciiStream(toIndex(columnName));
	}

	@Override

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return res.getBigDecimal(columnIndex);
	}

	@Override

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		return res.getBigDecimal(toIndex(columnName));
	}

	@Override

	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return res.getBigDecimal(columnIndex, scale);
	}

	@Override

	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
		return res.getBigDecimal(toIndex(columnName), scale);
	}

	@Override

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return res.getBinaryStream(columnIndex);
	}

	@Override

	public InputStream getBinaryStream(String columnName) throws SQLException {
		return res.getBinaryStream(toIndex(columnName));
	}

	@Override

	public Blob getBlob(int i) throws SQLException {
		return res.getBlob(i);
	}

	@Override

	public Blob getBlob(String colName) throws SQLException {
		return res.getBlob(toIndex(colName));
	}

	@Override

	public byte getByte(int columnIndex) throws SQLException {
		return res.getByte(columnIndex);
	}

	@Override

	public byte getByte(String columnName) throws SQLException {
		return res.getByte(toIndex(columnName));
	}

	@Override

	public byte[] getBytes(int columnIndex) throws SQLException {
		return res.getBytes(columnIndex);
	}

	@Override

	public byte[] getBytes(String columnName) throws SQLException {
		return res.getBytes(toIndex(columnName));
	}

	@Override

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return res.getCharacterStream(columnIndex);
	}

	@Override

	public Reader getCharacterStream(String columnName) throws SQLException {
		return res.getCharacterStream(toIndex(columnName));
	}

	@Override

	public Clob getClob(int i) throws SQLException {
		return res.getClob(i);
	}

	@Override

	public Clob getClob(String colName) throws SQLException {
		return res.getClob(toIndex(colName));
	}

	@Override

	public int getConcurrency() throws SQLException {
		return res.getConcurrency();
	}

	@Override

	public String getCursorName() throws SQLException {
		return res.getCursorName();
	}

	@Override

	public Date getDate(int columnIndex) throws SQLException {
		return res.getDate(columnIndex);
	}

	@Override

	public Date getDate(String columnName) throws SQLException {
		return res.getDate(toIndex(columnName));
	}

	@Override

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return res.getDate(columnIndex, cal);
	}

	@Override

	public Date getDate(String columnName, Calendar cal) throws SQLException {
		return res.getDate(toIndex(columnName), cal);
	}

	@Override

	public double getDouble(int columnIndex) throws SQLException {
		return res.getDouble(columnIndex);
	}

	@Override

	public double getDouble(String columnName) throws SQLException {
		return res.getDouble(toIndex(columnName));
	}

	@Override

	public int getFetchDirection() throws SQLException {
		return res.getFetchDirection();
	}

	@Override

	public int getFetchSize() throws SQLException {
		return res.getFetchSize();
	}

	@Override

	public float getFloat(int columnIndex) throws SQLException {
		return res.getFloat(columnIndex);
	}

	@Override

	public float getFloat(String columnName) throws SQLException {
		return res.getFloat(toIndex(columnName));
	}

	@Override

	public int getInt(int columnIndex) throws SQLException {
		return res.getInt(columnIndex);
	}

	@Override

	public int getInt(String columnName) throws SQLException {
		return res.getInt(toIndex(columnName));
	}

	@Override

	public long getLong(int columnIndex) throws SQLException {
		return res.getLong(columnIndex);
	}

	@Override

	public long getLong(String columnName) throws SQLException {
		return res.getLong(toIndex(columnName));
	}

	@Override

	public Object getObject(int i, Map map) throws SQLException {
		return res.getObject(i, map);
	}

	@Override

	public Object getObject(String colName, Map map) throws SQLException {
		return res.getObject(toIndex(colName), map);
	}

	// used only with java 7, do not set @Override
	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return (T) QueryUtil.getObject(this, columnIndex, type);
	}

	// used only with java 7, do not set @Override
	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		return (T) QueryUtil.getObject(this, columnLabel, type);
	}

	@Override

	public Ref getRef(int i) throws SQLException {
		return res.getRef(i);
	}

	@Override

	public Ref getRef(String colName) throws SQLException {
		return res.getRef(toIndex(colName));
	}

	@Override

	public int getRow() throws SQLException {
		return res.getRow();
	}

	@Override

	public short getShort(int columnIndex) throws SQLException {
		return res.getShort(columnIndex);
	}

	@Override

	public short getShort(String columnName) throws SQLException {
		return res.getShort(toIndex(columnName));
	}

	@Override

	public Statement getStatement() throws SQLException {
		return res.getStatement();
	}

	@Override

	public Time getTime(int columnIndex) throws SQLException {
		return res.getTime(columnIndex);
	}

	@Override

	public Time getTime(String columnName) throws SQLException {
		return res.getTime(toIndex(columnName));
	}

	@Override

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return res.getTime(columnIndex, cal);
	}

	@Override

	public Time getTime(String columnName, Calendar cal) throws SQLException {
		return res.getTime(toIndex(columnName), cal);
	}

	@Override

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return res.getTimestamp(columnIndex);
	}

	@Override

	public Timestamp getTimestamp(String columnName) throws SQLException {
		return res.getTimestamp(toIndex(columnName));
	}

	@Override

	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return res.getTimestamp(columnIndex, cal);
	}

	@Override

	public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
		return res.getTimestamp(toIndex(columnName), cal);
	}

	@Override

	public int getType() throws SQLException {
		return res.getType();
	}

	@Override

	public URL getURL(int columnIndex) throws SQLException {
		return res.getURL(columnIndex);
	}

	@Override

	public URL getURL(String columnName) throws SQLException {
		return res.getURL(toIndex(columnName));
	}

	@Override

	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return res.getUnicodeStream(columnIndex);
	}

	@Override

	public InputStream getUnicodeStream(String columnName) throws SQLException {
		return res.getUnicodeStream(toIndex(columnName));
	}

	@Override

	public SQLWarning getWarnings() throws SQLException {
		return res.getWarnings();
	}

	@Override

	public void insertRow() throws SQLException {
		res.insertRow();
	}

	@Override

	public boolean isAfterLast() throws SQLException {
		return res.isAfterLast();
	}

	@Override

	public boolean isBeforeFirst() throws SQLException {
		return res.isBeforeFirst();
	}

	@Override

	public boolean isFirst() throws SQLException {
		return res.isFirst();
	}

	@Override

	public boolean isLast() throws SQLException {
		return res.isLast();
	}

	@Override

	public boolean last() throws SQLException {
		return res.last();
	}

	@Override

	public void moveToCurrentRow() throws SQLException {
		res.moveToCurrentRow();
	}

	@Override

	public void moveToInsertRow() throws SQLException {
		res.moveToInsertRow();
	}

	@Override

	public boolean previous() {
		throw notSupported();
	}

	@Override

	public boolean previous(int pid) {
		throw notSupported();
	}

	@Override

	public void refreshRow() throws SQLException {
		res.refreshRow();
	}

	@Override

	public boolean relative(int rows) throws SQLException {
		return res.relative(rows);
	}

	@Override

	public boolean rowDeleted() throws SQLException {
		return res.rowDeleted();
	}

	@Override

	public boolean rowInserted() throws SQLException {
		return res.rowInserted();
	}

	@Override

	public boolean rowUpdated() throws SQLException {
		return res.rowUpdated();
	}

	@Override

	public void setFetchDirection(int direction) throws SQLException {
		res.setFetchDirection(direction);
	}

	@Override

	public void setFetchSize(int rows) throws SQLException {
		res.setFetchSize(rows);
	}

	@Override

	public void updateArray(int columnIndex, Array x) throws SQLException {
		res.updateArray(columnIndex, x);
	}

	@Override

	public void updateArray(String columnName, Array x) throws SQLException {
		res.updateArray(toIndex(columnName), x);
	}

	@Override

	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		res.updateAsciiStream(columnIndex, x, length);
	}

	@Override

	public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
		res.updateAsciiStream(toIndex(columnName), x, length);
	}

	@Override

	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		res.updateBigDecimal(columnIndex, x);
	}

	@Override

	public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
		res.updateBigDecimal(toIndex(columnName), x);
	}

	@Override

	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		res.updateBinaryStream(columnIndex, x, length);
	}

	@Override

	public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
		res.updateBinaryStream(toIndex(columnName), x, length);
	}

	@Override

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		res.updateBlob(columnIndex, x);
	}

	@Override

	public void updateBlob(String columnName, Blob x) throws SQLException {
		res.updateBlob(toIndex(columnName), x);
	}

	@Override

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		res.updateBoolean(columnIndex, x);
	}

	@Override

	public void updateBoolean(String columnName, boolean x) throws SQLException {
		res.updateBoolean(toIndex(columnName), x);
	}

	@Override

	public void updateByte(int columnIndex, byte x) throws SQLException {
		res.updateByte(columnIndex, x);
	}

	@Override

	public void updateByte(String columnName, byte x) throws SQLException {
		res.updateByte(toIndex(columnName), x);
	}

	@Override

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		res.updateBytes(columnIndex, x);
	}

	@Override

	public void updateBytes(String columnName, byte[] x) throws SQLException {
		res.updateBytes(toIndex(columnName), x);
	}

	@Override

	public void updateCharacterStream(int columnIndex, Reader reader, int length) throws SQLException {
		res.updateCharacterStream(columnIndex, reader, length);
	}

	@Override

	public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
		res.updateCharacterStream(toIndex(columnName), reader, length);
	}

	@Override

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		res.updateClob(columnIndex, x);
	}

	@Override

	public void updateClob(String columnName, Clob x) throws SQLException {
		res.updateClob(toIndex(columnName), x);
	}

	@Override

	public void updateDate(int columnIndex, Date x) throws SQLException {
		res.updateDate(columnIndex, x);
	}

	@Override

	public void updateDate(String columnName, Date x) throws SQLException {
		res.updateDate(toIndex(columnName), x);
	}

	@Override

	public void updateDouble(int columnIndex, double x) throws SQLException {
		res.updateDouble(columnIndex, x);
	}

	@Override

	public void updateDouble(String columnName, double x) throws SQLException {
		res.updateDouble(toIndex(columnName), x);
	}

	@Override

	public void updateFloat(int columnIndex, float x) throws SQLException {
		res.updateFloat(columnIndex, x);
	}

	@Override

	public void updateFloat(String columnName, float x) throws SQLException {
		res.updateFloat(toIndex(columnName), x);
	}

	@Override

	public void updateInt(int columnIndex, int x) throws SQLException {
		res.updateInt(columnIndex, x);
	}

	@Override

	public void updateInt(String columnName, int x) throws SQLException {
		res.updateInt(toIndex(columnName), x);
	}

	@Override

	public void updateLong(int columnIndex, long x) throws SQLException {
		res.updateLong(columnIndex, x);
	}

	@Override

	public void updateLong(String columnName, long x) throws SQLException {
		res.updateLong(toIndex(columnName), x);
	}

	@Override

	public void updateNull(int columnIndex) throws SQLException {
		res.updateNull(columnIndex);
	}

	@Override

	public void updateNull(String columnName) throws SQLException {
		res.updateNull(toIndex(columnName));
	}

	@Override

	public void updateObject(int columnIndex, Object x) throws SQLException {
		res.updateObject(columnIndex, x);
	}

	@Override

	public void updateObject(String columnName, Object x) throws SQLException {
		res.updateObject(toIndex(columnName), x);
	}

	@Override

	public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
		res.updateObject(columnIndex, x, scale);
	}

	@Override

	public void updateObject(String columnName, Object x, int scale) throws SQLException {
		res.updateObject(toIndex(columnName), x, scale);
	}

	@Override

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		res.updateRef(columnIndex, x);
	}

	@Override

	public void updateRef(String columnName, Ref x) throws SQLException {
		res.updateRef(toIndex(columnName), x);
	}

	@Override

	public void updateRow() throws SQLException {
		res.updateRow();
	}

	@Override

	public void updateShort(int columnIndex, short x) throws SQLException {
		res.updateShort(columnIndex, x);
	}

	@Override

	public void updateShort(String columnName, short x) throws SQLException {
		res.updateShort(toIndex(columnName), x);
	}

	@Override

	public void updateString(int columnIndex, String x) throws SQLException {
		res.updateString(columnIndex, x);
	}

	@Override

	public void updateString(String columnName, String x) throws SQLException {
		res.updateString(toIndex(columnName), x);
	}

	@Override

	public void updateTime(int columnIndex, Time x) throws SQLException {
		res.updateTime(columnIndex, x);
	}

	@Override

	public void updateTime(String columnName, Time x) throws SQLException {
		res.updateTime(toIndex(columnName), x);
	}

	@Override

	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		res.updateTimestamp(columnIndex, x);
	}

	@Override

	public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
		res.updateTimestamp(toIndex(columnName), x);
	}

	@Override

	public ResultSetMetaData getMetaData() throws SQLException {
		return res.getMetaData();
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
		return new CollectionIterator(keys(), this);
	}

	@Override

	public boolean equals(Object obj) {
		return res.equals(obj);
	}

	@Override

	public int getHoldability() throws SQLException {
		return res.getHoldability();
	}

	@Override

	public boolean isClosed() throws SQLException {
		return res.isClosed();
	}

	@Override

	public void updateNString(int columnIndex, String nString) throws SQLException {
		res.updateNString(columnIndex, nString);
	}

	@Override

	public void updateNString(String columnLabel, String nString) throws SQLException {
		res.updateNString(toIndex(columnLabel), nString);
	}

	@Override

	public String getNString(int columnIndex) throws SQLException {
		return res.getNString(columnIndex);
	}

	@Override

	public String getNString(String columnLabel) throws SQLException {
		return res.getNString(toIndex(columnLabel));
	}

	@Override

	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return res.getNCharacterStream(columnIndex);
	}

	@Override

	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return res.getNCharacterStream(toIndex(columnLabel));
	}

	@Override

	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		res.updateNCharacterStream(columnIndex, x, length);
	}

	@Override

	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		res.updateNCharacterStream(toIndex(columnLabel), reader, length);
	}

	@Override

	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		res.updateAsciiStream(columnIndex, x, length);
	}

	@Override

	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		res.updateBinaryStream(columnIndex, x, length);
	}

	@Override

	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		res.updateCharacterStream(columnIndex, x, length);
	}

	@Override

	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		res.updateAsciiStream(toIndex(columnLabel), x, length);
	}

	@Override

	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		res.updateBinaryStream(toIndex(columnLabel), x, length);
	}

	@Override

	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		res.updateCharacterStream(toIndex(columnLabel), reader, length);
	}

	@Override

	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		res.updateBlob(columnIndex, inputStream, length);
	}

	@Override

	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		res.updateBlob(toIndex(columnLabel), inputStream, length);
	}

	@Override

	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		res.updateClob(columnIndex, reader, length);
	}

	@Override

	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		res.updateClob(toIndex(columnLabel), reader, length);
	}

	@Override

	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		res.updateNClob(columnIndex, reader, length);
	}

	@Override

	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		res.updateNClob(toIndex(columnLabel), reader, length);
	}

	@Override

	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		res.updateNCharacterStream(columnIndex, x);
	}

	@Override

	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		res.updateNCharacterStream(toIndex(columnLabel), reader);
	}

	@Override

	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		res.updateAsciiStream(columnIndex, x);
	}

	@Override

	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		res.updateBinaryStream(columnIndex, x);
	}

	@Override

	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		res.updateCharacterStream(columnIndex, x);
	}

	@Override

	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		res.updateAsciiStream(toIndex(columnLabel), x);
	}

	@Override

	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		res.updateBinaryStream(columnLabel, x);
	}

	@Override

	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		res.updateCharacterStream(toIndex(columnLabel), reader);
	}

	@Override

	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		res.updateBlob(columnIndex, inputStream);
	}

	@Override

	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		res.updateBlob(toIndex(columnLabel), inputStream);
	}

	@Override

	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		res.updateClob(columnIndex, reader);
	}

	@Override

	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		res.updateClob(toIndex(columnLabel), reader);
	}

	@Override

	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		res.updateNClob(columnIndex, reader);
	}

	@Override

	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		res.updateNClob(toIndex(columnLabel), reader);
	}

	@Override

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return res.unwrap(iface);
	}

	@Override

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return res.isWrapperFor(iface);
	}

	@Override

	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		res.updateNClob(columnIndex, nClob);
	}

	@Override

	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		res.updateNClob(toIndex(columnLabel), nClob);
	}

	@Override

	public NClob getNClob(int columnIndex) throws SQLException {
		return res.getNClob(columnIndex);
	}

	@Override

	public NClob getNClob(String columnLabel) throws SQLException {
		return res.getNClob(toIndex(columnLabel));
	}

	@Override

	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return res.getSQLXML(columnIndex);
	}

	@Override

	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return res.getSQLXML(toIndex(columnLabel));
	}

	@Override

	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		res.updateSQLXML(columnIndex, xmlObject);
	}

	@Override

	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		res.updateSQLXML(toIndex(columnLabel), xmlObject);
	}

	@Override

	public RowId getRowId(int columnIndex) throws SQLException {
		return res.getRowId(columnIndex);
	}

	@Override

	public RowId getRowId(String columnLabel) throws SQLException {
		return res.getRowId(toIndex(columnLabel));
	}

	@Override

	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		res.updateRowId(columnIndex, x);
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		res.updateRowId(toIndex(columnLabel), x);
	}

	@Override
	public synchronized void enableShowQueryUsage() {
		throw notSupported();
	}

	public static PageRuntimeException notSupported() {
		return toRuntimeExc(new SQLFeatureNotSupportedException("not supported"));
	}

	public static PageRuntimeException toRuntimeExc(Throwable t) {
		return new PageRuntimeException(Caster.toPageException(t));
	}

	public static PageException toPageExc(Throwable t) {
		return Caster.toPageException(t);
	}

	private int toIndex(String columnName) throws SQLException {
		SimpleQueryColumn col = columns.get(columnName.toLowerCase());
		if (col == null) throw new SQLException("There is no column with name [" + columnName + "], available columns are [" + getColumnlist() + "]");
		return col.getIndex();
	}

	int getPid() {

		PageContext pc = ThreadLocalPageContext.get();
		if (pc == null) {
			pc = CFMLEngineFactory.getInstance().getThreadPageContext();
			if (pc == null) throw new RuntimeException("cannot get pid for current thread");
		}
		return pc.getId();
	}

	@Override
	public Query getGeneratedKeys() {
		return null;
	}

	@Override
	public SQL getSql() {
		return sql;
	}

	@Override
	public String getTemplate() {
		return templateLine.template;
	}

	public TemplateLine getTemplateLine() {
		return templateLine;
	}

	@Override
	public long getExecutionTime() {
		return exeTime;
	}

	@Override
	public java.util.Iterator getIterator() {
		return new ForEachQueryIterator(null, this, ThreadLocalPageContext.get().getId());
	}

	@Override
	public String getCacheType() {
		return cacheType;
	}

	@Override
	public void setCacheType(String cacheType) {
		this.cacheType = cacheType;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	public void throwIfClosed() throws DatabaseException {
		try {
			if (res != null && res.isClosed()) {
				throw new RuntimeException("The query is already closed and cannot be read again.");
			}
		}
		catch (SQLException e) {
			throw new DatabaseException(e, dc);
		}
	}

}