package lucee.runtime.type.query;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.runtime.PageContext;
import lucee.runtime.db.SQL;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;

public class QueryFQ implements Query, Objects, QueryResult, Serializable {

	private Query qry;
	private transient QueryResult qr;
	private transient Objects obj;
	private transient boolean cloned;

	public QueryFQ(Query qry) {
		this.qry = qry;
		this.qr = (QueryResult) qry;
		this.obj = (Objects) qry;
	}

	// method that do not change the inner state of the query
	@Override
	public String getTemplate() {
		return qry.getTemplate();
	}

	@Override
	public TemplateLine getTemplateLine() {
		if (qry instanceof QueryImpl) return ((QueryImpl) qry).getTemplateLine();
		return new TemplateLine(qry.getTemplate(), 0);
	}

	@Override
	public int executionTime() {
		return qry.executionTime();
	}

	@Override
	public int getUpdateCount() {
		return qry.getUpdateCount();
	}

	@Override
	public Query getGeneratedKeys() {
		return qry.getGeneratedKeys();
	}

	@Override
	public int size() {
		return qry.size();
	}

	@Override
	public Key[] keys() {
		return qry.keys();
	}

	@Override
	public Object get(String key, Object defaultValue) {
		return qry.get(key, defaultValue);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return qry.get(key, defaultValue);
	}

	@Override
	public Object get(String key) throws PageException {
		return qry.get(key);
	}

	@Override
	public Object get(Key key) throws PageException {
		return qry.get(key);
	}

	@Override
	public Object getAt(String key, int row, Object defaultValue) {
		return qry.getAt(key, row, defaultValue);
	}

	@Override
	public Object getAt(Key key, int row, Object defaultValue) {
		return qry.getAt(key, row, defaultValue);
	}

	@Override
	public Object getAt(String key, int row) throws PageException {
		return qry.getAt(key, row);
	}

	@Override
	public Object getAt(Key key, int row) throws PageException {
		return qry.getAt(key, row);
	}

	@Override
	public boolean next() {
		return qry.next();
	}

	@Override
	public boolean next(int pid) throws PageException {
		return qry.next(pid);
	}

	@Override
	public void reset() throws PageException {
		qry.reset();
	}

	@Override
	public void reset(int pid) throws PageException {
		qry.reset(pid);
	}

	@Override
	public int getRecordcount() {
		return qry.getRecordcount();
	}

	@Override
	public int getColumncount() {
		return qr.getColumncount();
	}

	@Override
	public int getCurrentrow(int pid) {
		return qry.getCurrentrow(pid);
	}

	@Override
	public boolean go(int index, int pid) throws PageException {
		return qry.go(index, pid);
	}

	@Override
	public boolean isEmpty() {
		return qry.isEmpty();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return qry.toDumpData(pageContext, maxlevel, dp);
	}

	@Override
	public synchronized int[] getTypes() {
		return qry.getTypes();
	}

	@Override
	public synchronized Map<Key, String> getTypesAsMap() {
		return qry.getTypesAsMap();
	}

	@Override
	public Object clone() {
		return qry.clone();
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return qry.duplicate(deepCopy);
	}

	@Override
	public QueryColumn getColumn(String key) throws PageException {
		return qry.getColumn(key);
	}

	@Override
	public QueryColumn getColumn(Key key) throws PageException {
		return qry.getColumn(key);
	}

	@Override
	public void rename(Key columnName, Key newColumnName) throws PageException {
		qry.rename(columnName, newColumnName);
	}

	@Override
	public QueryColumn getColumn(String key, QueryColumn defaultValue) {
		// TODO wrap column
		return qry.getColumn(key, defaultValue);
	}

	@Override
	public QueryColumn getColumn(Key key, QueryColumn defaultValue) {
		// TODO wrap column
		return qry.getColumn(key, defaultValue);
	}

	@Override
	public String toString() {
		return qry.toString();
	}

	@Override
	public String getCacheType() {
		return qry.getCacheType();
	}

	@Override
	public boolean isCached() {
		return qry.isCached();
	}

	@Override
	public int getColumnIndex(String coulmnName) {
		return qry.getColumnIndex(coulmnName);
	}

	@Override
	public String[] getColumns() {
		return qry.getColumns();
	}

	@Override
	public Key[] getColumnNames() {
		return qry.getColumnNames();
	}

	@Override
	public String[] getColumnNamesAsString() {
		return qry.getColumnNamesAsString();
	}

	@Override
	public int getColumnCount() {
		return qry.getColumnCount();
	}

	@Override
	public String getData(int row, int col) throws IndexOutOfBoundsException {
		return qry.getData(row, col);
	}

	@Override
	public String getName() {
		return qry.getName();
	}

	@Override
	public int getRowCount() {
		return qry.getRowCount();
	}

	@Override
	public boolean containsKey(String key) {

		return qry.containsKey(key);
	}

	@Override
	public boolean containsKey(Key key) {

		return qry.containsKey(key);
	}

	@Override
	public String castToString() throws PageException {
		return qry.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return qry.castToString(defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return qry.castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {

		return qry.castToBoolean(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return qry.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return qry.castToDoubleValue(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return qry.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return qry.castToDateTime(defaultValue);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return qry.compareTo(b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return qry.compareTo(dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return qry.compareTo(d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return qry.compareTo(str);
	}

	@Override
	public synchronized Array getMetaDataSimple() {
		return qry.getMetaDataSimple();
	}

	@Override
	public SQL getSql() {
		return qry.getSql();
	}

	@Override
	public Object getObject(String columnName) throws SQLException {
		return qry.getObject(columnName);
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		return qry.getObject(columnIndex);
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		return qry.getString(columnIndex);
	}

	@Override
	public String getString(String columnName) throws SQLException {
		return qry.getString(columnName);
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		return qry.getBoolean(columnIndex);
	}

	@Override
	public boolean getBoolean(String columnName) throws SQLException {
		return qry.getBoolean(columnName);
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] arguments) throws PageException {
		return obj.call(pc, methodName, arguments);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		return obj.callWithNamedValues(pc, methodName, args);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		return obj.get(pc, key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		return obj.get(pc, key);
	}

	@Override
	public boolean wasNull() throws SQLException {
		return qry.wasNull();
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		return qry.absolute(row);
	}

	@Override
	public void afterLast() throws SQLException {
		qry.afterLast();
	}

	@Override
	public void beforeFirst() throws SQLException {
		qry.beforeFirst();
	}

	@Override
	public int findColumn(String columnName) throws SQLException {
		return qry.findColumn(columnName);
	}

	@Override
	public boolean first() throws SQLException {
		return qry.first();
	}

	@Override
	public java.sql.Array getArray(int i) throws SQLException {
		return qry.getArray(i);
	}

	@Override
	public java.sql.Array getArray(String colName) throws SQLException {
		return qry.getArray(colName);
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return qry.getAsciiStream(columnIndex);
	}

	@Override
	public InputStream getAsciiStream(String columnName) throws SQLException {
		return qry.getAsciiStream(columnName);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return qry.getBigDecimal(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		return qry.getBigDecimal(columnName);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return qry.getBigDecimal(columnIndex, scale);
	}

	@Override
	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
		return qry.getBigDecimal(columnName, scale);
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return qry.getBinaryStream(columnIndex);
	}

	@Override
	public InputStream getBinaryStream(String columnName) throws SQLException {
		return qry.getBinaryStream(columnName);
	}

	@Override
	public Blob getBlob(int i) throws SQLException {
		return qry.getBlob(i);
	}

	@Override
	public Blob getBlob(String colName) throws SQLException {
		return qry.getBlob(colName);
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		return qry.getByte(columnIndex);
	}

	@Override
	public byte getByte(String columnName) throws SQLException {
		return qry.getByte(columnName);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		return qry.getBytes(columnIndex);
	}

	@Override
	public byte[] getBytes(String columnName) throws SQLException {
		return qry.getBytes(columnName);
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return qry.getCharacterStream(columnIndex);
	}

	@Override
	public Reader getCharacterStream(String columnName) throws SQLException {
		return qry.getCharacterStream(columnName);
	}

	@Override
	public Clob getClob(int i) throws SQLException {
		return qry.getClob(i);
	}

	@Override
	public Clob getClob(String colName) throws SQLException {
		return qry.getClob(colName);
	}

	@Override
	public int getConcurrency() throws SQLException {
		return qry.getConcurrency();
	}

	@Override
	public String getCursorName() throws SQLException {
		return qry.getCursorName();
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		return qry.getDate(columnIndex);
	}

	@Override
	public Date getDate(String columnName) throws SQLException {
		return qry.getDate(columnName);
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return qry.getDate(columnIndex, cal);
	}

	@Override
	public Date getDate(String columnName, Calendar cal) throws SQLException {
		return qry.getDate(columnName, cal);
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		return qry.getDouble(columnIndex);
	}

	@Override
	public double getDouble(String columnName) throws SQLException {
		return qry.getDouble(columnName);
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return qry.getFetchDirection();
	}

	@Override
	public int getFetchSize() throws SQLException {
		return qry.getFetchSize();
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		return qry.getFloat(columnIndex);
	}

	@Override
	public float getFloat(String columnName) throws SQLException {
		return qry.getFloat(columnName);
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		return qry.getInt(columnIndex);
	}

	@Override
	public int getInt(String columnName) throws SQLException {
		return qry.getInt(columnName);
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		return qry.getLong(columnIndex);
	}

	@Override
	public long getLong(String columnName) throws SQLException {
		return qry.getLong(columnName);
	}

	@Override
	public Object getObject(int i, Map map) throws SQLException {
		return qry.getObject(i, map);
	}

	@Override
	public Object getObject(String colName, Map map) throws SQLException {
		return qry.getObject(colName, map);
	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return qry.getObject(columnIndex, type);
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		return qry.getObject(columnLabel, type);
	}

	@Override
	public Ref getRef(int i) throws SQLException {
		return qry.getRef(i);
	}

	@Override
	public Ref getRef(String colName) throws SQLException {
		return qry.getRef(colName);
	}

	@Override
	public int getRow() throws SQLException {
		return qry.getRow();
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		return qry.getShort(columnIndex);
	}

	@Override
	public short getShort(String columnName) throws SQLException {
		return qry.getShort(columnName);
	}

	@Override
	public Statement getStatement() throws SQLException {
		return qry.getStatement();
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		return qry.getTime(columnIndex);
	}

	@Override
	public Time getTime(String columnName) throws SQLException {
		return qry.getTime(columnName);
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return qry.getTime(columnIndex, cal);
	}

	@Override
	public Time getTime(String columnName, Calendar cal) throws SQLException {
		return qry.getTime(columnName, cal);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return qry.getTimestamp(columnIndex);
	}

	@Override
	public Timestamp getTimestamp(String columnName) throws SQLException {
		return qry.getTimestamp(columnName);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return qry.getTimestamp(columnIndex, cal);
	}

	@Override
	public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
		return qry.getTimestamp(columnName, cal);
	}

	@Override
	public int getType() throws SQLException {
		return qry.getType();
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		return qry.getURL(columnIndex);
	}

	@Override
	public URL getURL(String columnName) throws SQLException {
		return qry.getURL(columnName);
	}

	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return qry.getUnicodeStream(columnIndex);
	}

	@Override
	public InputStream getUnicodeStream(String columnName) throws SQLException {
		return qry.getUnicodeStream(columnName);
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return qry.getWarnings();
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		return qry.isAfterLast();
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		return qry.isBeforeFirst();
	}

	@Override
	public boolean isFirst() throws SQLException {
		return qry.isFirst();
	}

	@Override
	public boolean isLast() throws SQLException {
		return qry.isLast();
	}

	@Override
	public boolean last() throws SQLException {
		return qry.last();
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		qry.moveToCurrentRow();
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		qry.moveToInsertRow();
	}

	@Override
	public boolean previous() throws SQLException {
		return qry.previous();
	}

	@Override
	public boolean previous(int pid) {
		return qry.previous(pid);
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		return qry.relative(rows);
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		return qry.rowDeleted();
	}

	@Override
	public boolean rowInserted() throws SQLException {
		return qry.rowInserted();
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		return qry.rowUpdated();
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return qry.getMetaData();
	}

	@Override
	public Iterator<Key> keyIterator() {
		return qry.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return qry.keysAsStringIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return qry.entryIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return qry.valueIterator();
	}

	@Override
	public int getHoldability() throws SQLException {
		return qry.getHoldability();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return qry.isClosed();
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		return qry.getNString(columnIndex);
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		return qry.getNString(columnLabel);
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return qry.getNCharacterStream(columnIndex);
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return qry.getNCharacterStream(columnLabel);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return qry.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return qry.isWrapperFor(iface);
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		return qry.getNClob(columnIndex);
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		return qry.getNClob(columnLabel);
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return qry.getSQLXML(columnIndex);
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return qry.getSQLXML(columnLabel);
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		return qry.getRowId(columnIndex);
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		return qry.getRowId(columnLabel);
	}

	@Override
	public long getExecutionTime() {
		return qry.getExecutionTime();
	}

	@Override
	public Iterator getIterator() {
		return qry.getIterator();
	}

	@Override
	public boolean equals(Object obj) {
		return qry.equals(obj);
	}

	// methods changing the inner state of the Query
	@Override
	public void setUpdateCount(int updateCount) {
		if (!cloned) _clone();
		qr.setUpdateCount(updateCount);
	}

	@Override
	public Object removeEL(Key key) {
		if (!cloned) _clone();
		return qry.removeEL(key);
	}

	@Override
	public Object remove(Key key) throws PageException {
		if (!cloned) _clone();
		return qry.remove(key);
	}

	@Override
	public Object remove(Key key, Object defaultValue) {
		if (!cloned) _clone();
		return qry.remove(key, defaultValue);
	}

	@Override
	public void clear() {
		if (!cloned) _clone();
		qry.clear();
	}

	@Override
	public synchronized int removeRow(int row) throws PageException {
		if (!cloned) _clone();
		return qry.removeRow(row);
	}

	@Override
	public int removeRowEL(int row) {
		if (!cloned) _clone();
		return qry.removeRowEL(row);
	}

	@Override
	public QueryColumn removeColumn(String key) throws PageException {
		if (!cloned) _clone();
		return qry.removeColumn(key);
	}

	@Override
	public QueryColumn removeColumn(Key key) throws PageException {
		if (!cloned) _clone();
		return qry.removeColumn(key);
	}

	@Override
	public synchronized QueryColumn removeColumnEL(String key) {
		if (!cloned) _clone();
		return qry.removeColumnEL(key);
	}

	@Override
	public QueryColumn removeColumnEL(Key key) {
		if (!cloned) _clone();
		return qry.removeColumnEL(key);
	}

	@Override
	public Object setEL(String key, Object value) {
		if (!cloned) _clone();
		return qry.setEL(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		if (!cloned) _clone();
		return qry.setEL(key, value);
	}

	@Override
	public Object set(String key, Object value) throws PageException {
		if (!cloned) _clone();
		return qry.set(key, value);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		if (!cloned) _clone();
		return qry.set(key, value);
	}

	@Override
	public Object setAt(String key, int row, Object value) throws PageException {
		if (!cloned) _clone();
		return qry.setAt(key, row, value);
	}

	@Override
	public Object setAt(Key key, int row, Object value) throws PageException {
		if (!cloned) _clone();
		return qry.setAt(key, row, value);
	}

	@Override
	public Object setAtEL(String key, int row, Object value) {
		if (!cloned) _clone();
		return qry.setAtEL(key, row, value);
	}

	@Override
	public Object setAtEL(Key key, int row, Object value) {
		if (!cloned) _clone();
		return qry.setAtEL(key, row, value);
	}

	@Override
	public Object setAtIndex(int index, int row, Object value, boolean trustType) throws PageException{
		if (!cloned) _clone();
		return qry.setAtIndex(index, row, value, trustType);
	}

	@Override
	public void sort(String column) throws PageException {
		if (!cloned) _clone();
		qry.sort(column);
	}

	@Override
	public void sort(Key column) throws PageException {
		if (!cloned) _clone();
		qry.sort(column);
	}

	@Override
	public void sort(String strColumn, int order) throws PageException {
		if (!cloned) _clone();
		qry.sort(strColumn, order);
	}

	@Override
	public void sort(Key keyColumn, int order) throws PageException {
		if (!cloned) _clone();
		qry.sort(keyColumn, order);
	}

	@Override
	public boolean addRow(int count) {
		if (!cloned) _clone();
		return qry.addRow(count);
	}

	@Override
	public boolean addColumn(String columnName, Array content) throws PageException {
		if (!cloned) _clone();
		return qry.addColumn(columnName, content);
	}

	@Override
	public boolean addColumn(Key columnName, Array content) throws PageException {
		if (!cloned) _clone();
		return qry.addColumn(columnName, content);
	}

	@Override
	public boolean addColumn(String columnName, Array content, int type) throws PageException {
		if (!cloned) _clone();
		return qry.addColumn(columnName, content, type);
	}

	@Override
	public boolean addColumn(Key columnName, Array content, int type) throws PageException {
		if (!cloned) _clone();
		return qry.addColumn(columnName, content, type);
	}

	@Override
	public void setExecutionTime(long exeTime) {
		if (!cloned) _clone();
		qry.setExecutionTime(exeTime);
	}

	@Override
	public void setCacheType(String cacheType) {
		if (!cloned) _clone();
		qry.setCacheType(cacheType);
	}

	@Override
	public void setCached(boolean isCached) {
		if (!cloned) _clone();
		qry.setCached(isCached);
	}

	@Override
	public int addRow() {
		if (!cloned) _clone();
		return qry.addRow();
	}

	@Override
	public void setColumnNames(Key[] trg) throws PageException {
		if (!cloned) _clone();
		qr.setColumnNames(trg);
	}

	@Override
	public void setData(int row, int col, String value) throws IndexOutOfBoundsException {
		if (!cloned) _clone();
		qry.setData(row, col, value);
	}

	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		if (!cloned) _clone();
		return obj.set(pc, propertyName, value);
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		if (!cloned) _clone();
		return obj.setEL(pc, propertyName, value);
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		if (!cloned) _clone();
		qry.cancelRowUpdates();
	}

	@Override
	public void clearWarnings() throws SQLException {
		if (!cloned) _clone();
		qry.clearWarnings();
	}

	@Override
	public void close() throws SQLException {
		if (!cloned) _clone();
		qry.close();
	}

	@Override
	public void deleteRow() throws SQLException {
		if (!cloned) _clone();
		qry.deleteRow();
	}

	@Override
	public void insertRow() throws SQLException {
		if (!cloned) _clone();
		qry.insertRow();
	}

	@Override
	public void refreshRow() throws SQLException {
		if (!cloned) _clone();
		qry.refreshRow();
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		if (!cloned) _clone();
		qry.setFetchDirection(direction);
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		if (!cloned) _clone();
		qry.setFetchSize(rows);
	}

	@Override
	public void updateArray(int columnIndex, java.sql.Array x) throws SQLException {
		if (!cloned) _clone();
		qry.updateArray(columnIndex, x);
	}

	@Override
	public void updateArray(String columnName, java.sql.Array x) throws SQLException {
		if (!cloned) _clone();
		qry.updateArray(columnName, x);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		if (!cloned) _clone();
		qry.updateAsciiStream(columnIndex, x, length);
	}

	@Override
	public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
		if (!cloned) _clone();
		qry.updateAsciiStream(columnName, x, length);
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		if (!cloned) _clone();
		qry.updateBigDecimal(columnIndex, x);
	}

	@Override
	public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
		if (!cloned) _clone();
		qry.updateBigDecimal(columnName, x);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		if (!cloned) _clone();
		qry.updateBinaryStream(columnIndex, x, length);
	}

	@Override
	public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
		if (!cloned) _clone();
		qry.updateBinaryStream(columnName, x, length);
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		if (!cloned) _clone();
		qry.updateBlob(columnIndex, x);
	}

	@Override
	public void updateBlob(String columnName, Blob x) throws SQLException {
		if (!cloned) _clone();
		qry.updateBlob(columnName, x);
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		if (!cloned) _clone();
		qry.updateBoolean(columnIndex, x);
	}

	@Override
	public void updateBoolean(String columnName, boolean x) throws SQLException {
		if (!cloned) _clone();
		qry.updateBoolean(columnName, x);
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		if (!cloned) _clone();
		qry.updateByte(columnIndex, x);
	}

	@Override
	public void updateByte(String columnName, byte x) throws SQLException {
		if (!cloned) _clone();
		qry.updateByte(columnName, x);
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		if (!cloned) _clone();
		qry.updateBytes(columnIndex, x);
	}

	@Override
	public void updateBytes(String columnName, byte[] x) throws SQLException {
		if (!cloned) _clone();
		qry.updateBytes(columnName, x);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader reader, int length) throws SQLException {
		if (!cloned) _clone();
		qry.updateCharacterStream(columnIndex, reader, length);
	}

	@Override
	public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
		if (!cloned) _clone();
		qry.updateCharacterStream(columnName, reader, length);
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		if (!cloned) _clone();
		qry.updateClob(columnIndex, x);
	}

	@Override
	public void updateClob(String columnName, Clob x) throws SQLException {
		if (!cloned) _clone();
		qry.updateClob(columnName, x);
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		if (!cloned) _clone();
		qry.updateDate(columnIndex, x);
	}

	@Override
	public void updateDate(String columnName, Date x) throws SQLException {
		if (!cloned) _clone();
		qry.updateDate(columnName, x);
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		if (!cloned) _clone();
		qry.updateDouble(columnIndex, x);
	}

	@Override
	public void updateDouble(String columnName, double x) throws SQLException {
		if (!cloned) _clone();
		qry.updateDouble(columnName, x);
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		if (!cloned) _clone();
		qry.updateFloat(columnIndex, x);
	}

	@Override
	public void updateFloat(String columnName, float x) throws SQLException {
		if (!cloned) _clone();
		qry.updateFloat(columnName, x);
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		if (!cloned) _clone();
		qry.updateInt(columnIndex, x);
	}

	@Override
	public void updateInt(String columnName, int x) throws SQLException {
		if (!cloned) _clone();
		qry.updateInt(columnName, x);
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		if (!cloned) _clone();
		qry.updateLong(columnIndex, x);
	}

	@Override
	public void updateLong(String columnName, long x) throws SQLException {
		if (!cloned) _clone();
		qry.updateLong(columnName, x);
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		if (!cloned) _clone();
		qry.updateNull(columnIndex);
	}

	@Override
	public void updateNull(String columnName) throws SQLException {
		if (!cloned) _clone();
		qry.updateNull(columnName);
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		if (!cloned) _clone();
		qry.updateObject(columnIndex, x);
	}

	@Override
	public void updateObject(String columnName, Object x) throws SQLException {
		if (!cloned) _clone();
		qry.updateObject(columnName, x);
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
		if (!cloned) _clone();
		qry.updateObject(columnIndex, x, scale);
	}

	@Override
	public void updateObject(String columnName, Object x, int scale) throws SQLException {
		if (!cloned) _clone();
		qry.updateObject(columnName, x, scale);
	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		if (!cloned) _clone();
		qry.updateRef(columnIndex, x);
	}

	@Override
	public void updateRef(String columnName, Ref x) throws SQLException {
		if (!cloned) _clone();
		qry.updateRef(columnName, x);
	}

	@Override
	public void updateRow() throws SQLException {
		if (!cloned) _clone();
		qry.updateRow();
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		if (!cloned) _clone();
		qry.updateShort(columnIndex, x);
	}

	@Override
	public void updateShort(String columnName, short x) throws SQLException {
		if (!cloned) _clone();
		qry.updateShort(columnName, x);
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		if (!cloned) _clone();
		qry.updateString(columnIndex, x);
	}

	@Override
	public void updateString(String columnName, String x) throws SQLException {
		if (!cloned) _clone();
		qry.updateString(columnName, x);
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		if (!cloned) _clone();
		qry.updateTime(columnIndex, x);
	}

	@Override
	public void updateTime(String columnName, Time x) throws SQLException {
		if (!cloned) _clone();
		qry.updateTime(columnName, x);
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		if (!cloned) _clone();
		qry.updateTimestamp(columnIndex, x);
	}

	@Override
	public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
		if (!cloned) _clone();
		qry.updateTimestamp(columnName, x);
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		if (!cloned) _clone();
		qry.updateNString(columnIndex, nString);
	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		if (!cloned) _clone();
		qry.updateNString(columnLabel, nString);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateNCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateNCharacterStream(columnLabel, reader, length);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateAsciiStream(columnIndex, x, length);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateBinaryStream(columnIndex, x, length);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateCharacterStream(columnIndex, x, length);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateAsciiStream(columnLabel, x, length);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateBinaryStream(columnLabel, x, length);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateCharacterStream(columnLabel, reader, length);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateBlob(columnIndex, inputStream, length);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateBlob(columnLabel, inputStream, length);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateClob(columnIndex, reader, length);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateClob(columnLabel, reader, length);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateNClob(columnIndex, reader, length);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		if (!cloned) _clone();
		qry.updateNClob(columnLabel, reader, length);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		if (!cloned) _clone();
		qry.updateNCharacterStream(columnIndex, x);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		if (!cloned) _clone();
		qry.updateNCharacterStream(columnLabel, reader);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		if (!cloned) _clone();
		qry.updateAsciiStream(columnIndex, x);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		if (!cloned) _clone();
		qry.updateBinaryStream(columnIndex, x);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		if (!cloned) _clone();
		qry.updateCharacterStream(columnIndex, x);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		if (!cloned) _clone();
		qry.updateAsciiStream(columnLabel, x);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		if (!cloned) _clone();
		qry.updateBinaryStream(columnLabel, x);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		if (!cloned) _clone();
		qry.updateCharacterStream(columnLabel, reader);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		if (!cloned) _clone();
		qry.updateBlob(columnIndex, inputStream);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		if (!cloned) _clone();
		qry.updateBlob(columnLabel, inputStream);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		if (!cloned) _clone();
		qry.updateClob(columnIndex, reader);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		if (!cloned) _clone();
		qry.updateClob(columnLabel, reader);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		if (!cloned) _clone();
		qry.updateNClob(columnIndex, reader);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		if (!cloned) _clone();
		qry.updateNClob(columnLabel, reader);
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		if (!cloned) _clone();
		qry.updateNClob(columnIndex, nClob);
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		if (!cloned) _clone();
		qry.updateNClob(columnLabel, nClob);
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		if (!cloned) _clone();
		qry.updateSQLXML(columnIndex, xmlObject);
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		if (!cloned) _clone();
		qry.updateSQLXML(columnLabel, xmlObject);
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		if (!cloned) _clone();
		qry.updateRowId(columnIndex, x);
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		if (!cloned) _clone();
		qry.updateRowId(columnLabel, x);
	}

	@Override
	public synchronized void enableShowQueryUsage() {
		if (!cloned) _clone();
		qry.enableShowQueryUsage();
	}

	public synchronized void _clone() {
		if (cloned) return; // we repeat this because the check outside the method is not sync
		qry = (Query) qry.duplicate(true);
		qr = (QueryResult) qry;
		obj = (Objects) qry;
		cloned = true;
	}

}
