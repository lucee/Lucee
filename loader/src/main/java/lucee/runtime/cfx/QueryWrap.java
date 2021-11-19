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
package lucee.runtime.cfx;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
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

import com.allaire.cfx.Query;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;

/**
 * Implementation of the Query Interface
 */
public class QueryWrap implements Query {

	private final lucee.runtime.type.Query rst;
	private final String name;

	/**
	 * constructor of the class
	 * 
	 * @param rst runtime Query
	 */
	public QueryWrap(final lucee.runtime.type.Query rst) {
		this.rst = rst;
		this.name = rst.getName();
	}

	/**
	 * constructor of the class
	 * 
	 * @param rst runtime Query
	 * @param name name of the query (otherwise rst.getName())
	 */
	public QueryWrap(final lucee.runtime.type.Query rst, final String name) {
		this.rst = rst;
		this.name = name;
	}

	/**
	 * @see com.allaire.cfx.Query#addRow()
	 */
	@Override
	public int addRow() {
		return rst.addRow();
	}

	/**
	 * @see com.allaire.cfx.Query#getColumnIndex(java.lang.String)
	 */
	@Override
	public int getColumnIndex(final String coulmnName) {
		return rst.getColumnIndex(coulmnName);
	}

	/**
	 * @see com.allaire.cfx.Query#getColumns()
	 */
	@Override
	@SuppressWarnings("deprecation")
	public String[] getColumns() {
		return rst.getColumns();
	}

	@Override
	public Collection.Key[] getColumnNames() {
		return rst.getColumnNames();
	}

	@Override
	public String[] getColumnNamesAsString() {
		return rst.getColumnNamesAsString();
	}

	/**
	 * @see com.allaire.cfx.Query#getData(int, int)
	 */
	@Override
	public String getData(final int row, final int col) throws IndexOutOfBoundsException {
		return rst.getData(row, col);
	}

	/**
	 * @see com.allaire.cfx.Query#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see com.allaire.cfx.Query#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return rst.getRowCount();
	}

	/**
	 * @see com.allaire.cfx.Query#setData(int, int, java.lang.String)
	 */
	@Override
	public void setData(final int row, final int col, final String value) throws IndexOutOfBoundsException {
		rst.setData(row, col, value);
	}

	/**
	 * @see java.sql.ResultSet#absolute(int)
	 */
	@Override
	public boolean absolute(final int row) throws SQLException {
		return rst.absolute(row);
	}

	/**
	 * @see java.sql.ResultSet#afterLast()
	 */
	@Override
	public void afterLast() throws SQLException {
		rst.afterLast();
	}

	/**
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	@Override
	public void beforeFirst() throws SQLException {
		rst.beforeFirst();
	}

	/**
	 * @see java.sql.ResultSet#cancelRowUpdates()
	 */
	@Override
	public void cancelRowUpdates() throws SQLException {
		rst.cancelRowUpdates();
	}

	/**
	 * @see java.sql.ResultSet#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		rst.clearWarnings();
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return rst.clone();
	}

	/**
	 * @see java.sql.ResultSet#close()
	 */
	@Override
	public void close() throws SQLException {
		rst.close();
	}

	/**
	 * @see java.sql.ResultSet#deleteRow()
	 */
	@Override
	public void deleteRow() throws SQLException {
		rst.deleteRow();
	}

	/**
	 * @see java.sql.ResultSet#findColumn(java.lang.String)
	 */
	@Override
	public int findColumn(final String columnName) throws SQLException {
		return rst.findColumn(columnName);
	}

	/**
	 * @see java.sql.ResultSet#first()
	 */
	@Override
	public boolean first() throws SQLException {
		return rst.first();
	}

	/**
	 * @see java.sql.ResultSet#getArray(int)
	 */
	@Override
	public Array getArray(final int i) throws SQLException {
		return rst.getArray(i);
	}

	/**
	 * @see java.sql.ResultSet#getArray(java.lang.String)
	 */
	@Override
	public Array getArray(final String colName) throws SQLException {
		return rst.getArray(colName);
	}

	/**
	 * @see java.sql.ResultSet#getAsciiStream(int)
	 */
	@Override
	public InputStream getAsciiStream(final int columnIndex) throws SQLException {
		return rst.getAsciiStream(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getAsciiStream(java.lang.String)
	 */
	@Override
	public InputStream getAsciiStream(final String columnName) throws SQLException {
		return rst.getAsciiStream(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getBigDecimal(int, int)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
		return rst.getBigDecimal(columnIndex, scale);
	}

	/**
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	@Override
	public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
		return rst.getBigDecimal(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public BigDecimal getBigDecimal(final String columnName, final int scale) throws SQLException {
		return rst.getBigDecimal(columnName, scale);
	}

	/**
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
	 */
	@Override
	public BigDecimal getBigDecimal(final String columnName) throws SQLException {
		return rst.getBigDecimal(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getBinaryStream(int)
	 */
	@Override
	public InputStream getBinaryStream(final int columnIndex) throws SQLException {
		return rst.getBinaryStream(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getBinaryStream(java.lang.String)
	 */
	@Override
	public InputStream getBinaryStream(final String columnName) throws SQLException {
		return rst.getBinaryStream(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getBlob(int)
	 */
	@Override
	public Blob getBlob(final int i) throws SQLException {
		return rst.getBlob(i);
	}

	/**
	 * @see java.sql.ResultSet#getBlob(java.lang.String)
	 */
	@Override
	public Blob getBlob(final String colName) throws SQLException {
		return rst.getBlob(colName);
	}

	/**
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	@Override
	public boolean getBoolean(final int columnIndex) throws SQLException {
		return rst.getBoolean(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean(final String columnName) throws SQLException {
		return rst.getBoolean(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getByte(int)
	 */
	@Override
	public byte getByte(final int columnIndex) throws SQLException {
		return rst.getByte(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getByte(java.lang.String)
	 */
	@Override
	public byte getByte(final String columnName) throws SQLException {
		return rst.getByte(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getBytes(int)
	 */
	@Override
	public byte[] getBytes(final int columnIndex) throws SQLException {
		return rst.getBytes(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getBytes(java.lang.String)
	 */
	@Override
	public byte[] getBytes(final String columnName) throws SQLException {
		return rst.getBytes(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getCharacterStream(int)
	 */
	@Override
	public Reader getCharacterStream(final int columnIndex) throws SQLException {
		return rst.getCharacterStream(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getCharacterStream(java.lang.String)
	 */
	@Override
	public Reader getCharacterStream(final String columnName) throws SQLException {
		return rst.getCharacterStream(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getClob(int)
	 */
	@Override
	public Clob getClob(final int i) throws SQLException {
		return rst.getClob(i);
	}

	/**
	 * @see java.sql.ResultSet#getClob(java.lang.String)
	 */
	@Override
	public Clob getClob(final String colName) throws SQLException {
		return rst.getClob(colName);
	}

	/**
	 * @see java.sql.ResultSet#getConcurrency()
	 */
	@Override
	public int getConcurrency() throws SQLException {
		return rst.getConcurrency();
	}

	/**
	 * @see java.sql.ResultSet#getCursorName()
	 */
	@Override
	public String getCursorName() throws SQLException {
		return rst.getCursorName();
	}

	/**
	 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
	 */
	@Override
	public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
		return rst.getDate(columnIndex, cal);
	}

	/**
	 * @see java.sql.ResultSet#getDate(int)
	 */
	@Override
	public Date getDate(final int columnIndex) throws SQLException {
		return rst.getDate(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Date getDate(final String columnName, final Calendar cal) throws SQLException {
		return rst.getDate(columnName, cal);
	}

	/**
	 * @see java.sql.ResultSet#getDate(java.lang.String)
	 */
	@Override
	public Date getDate(final String columnName) throws SQLException {
		return rst.getDate(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getDouble(int)
	 */
	@Override
	public double getDouble(final int columnIndex) throws SQLException {
		return rst.getDouble(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(final String columnName) throws SQLException {
		return rst.getDouble(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getFetchDirection()
	 */
	@Override
	public int getFetchDirection() throws SQLException {
		return rst.getFetchDirection();
	}

	/**
	 * @see java.sql.ResultSet#getFetchSize()
	 */
	@Override
	public int getFetchSize() throws SQLException {
		return rst.getFetchSize();
	}

	/**
	 * @see java.sql.ResultSet#getFloat(int)
	 */
	@Override
	public float getFloat(final int columnIndex) throws SQLException {
		return rst.getFloat(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getFloat(java.lang.String)
	 */
	@Override
	public float getFloat(final String columnName) throws SQLException {
		return rst.getFloat(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getInt(int)
	 */
	@Override
	public int getInt(final int columnIndex) throws SQLException {
		return rst.getInt(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getInt(java.lang.String)
	 */
	@Override
	public int getInt(final String columnName) throws SQLException {
		return rst.getInt(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getLong(int)
	 */
	@Override
	public long getLong(final int columnIndex) throws SQLException {
		return rst.getLong(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getLong(java.lang.String)
	 */
	@Override
	public long getLong(final String columnName) throws SQLException {
		return rst.getLong(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getMetaData()
	 */
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return rst.getMetaData();
	}

	/**
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	@Override
	public Object getObject(final int i, final Map<String, Class<?>> map) throws SQLException {
		return rst.getObject(i, map);
	}

	/**
	 * @see java.sql.ResultSet#getObject(int)
	 */
	@Override
	public Object getObject(final int columnIndex) throws SQLException {
		return rst.getObject(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	@Override
	public Object getObject(final String colName, final Map<String, Class<?>> map) throws SQLException {
		return rst.getObject(colName, map);
	}

	/**
	 * @see java.sql.ResultSet#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(final String columnName) throws SQLException {
		return rst.getObject(columnName);
	}

	/**
	 * @return recordcount of the query
	 */
	public int getRecordcount() {
		return rst.getRecordcount();
	}

	/**
	 * @see java.sql.ResultSet#getRef(int)
	 */
	@Override
	public Ref getRef(final int i) throws SQLException {
		return rst.getRef(i);
	}

	/**
	 * @see java.sql.ResultSet#getRef(java.lang.String)
	 */
	@Override
	public Ref getRef(final String colName) throws SQLException {
		return rst.getRef(colName);
	}

	/**
	 * @see java.sql.ResultSet#getRow()
	 */
	@Override
	public int getRow() throws SQLException {
		return rst.getRow();
	}

	/**
	 * @see java.sql.ResultSet#getShort(int)
	 */
	@Override
	public short getShort(final int columnIndex) throws SQLException {
		return rst.getShort(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getShort(java.lang.String)
	 */
	@Override
	public short getShort(final String columnName) throws SQLException {
		return rst.getShort(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getStatement()
	 */
	@Override
	public Statement getStatement() throws SQLException {
		return rst.getStatement();
	}

	/**
	 * @see java.sql.ResultSet#getString(int)
	 */
	@Override
	public String getString(final int columnIndex) throws SQLException {
		return rst.getString(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getString(java.lang.String)
	 */
	@Override
	public String getString(final String columnName) throws SQLException {
		return rst.getString(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */
	@Override
	public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
		return rst.getTime(columnIndex, cal);
	}

	/**
	 * @see java.sql.ResultSet#getTime(int)
	 */
	@Override
	public Time getTime(final int columnIndex) throws SQLException {
		return rst.getTime(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Time getTime(final String columnName, final Calendar cal) throws SQLException {
		return rst.getTime(columnName, cal);
	}

	/**
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */
	@Override
	public Time getTime(final String columnName) throws SQLException {
		return rst.getTime(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */
	@Override
	public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
		return rst.getTimestamp(columnIndex, cal);
	}

	/**
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	@Override
	public Timestamp getTimestamp(final int columnIndex) throws SQLException {
		return rst.getTimestamp(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Timestamp getTimestamp(final String columnName, final Calendar cal) throws SQLException {
		return rst.getTimestamp(columnName, cal);
	}

	/**
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */
	@Override
	public Timestamp getTimestamp(final String columnName) throws SQLException {
		return rst.getTimestamp(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getType()
	 */
	@Override
	public int getType() throws SQLException {
		return rst.getType();
	}

	/**
	 * @see java.sql.ResultSet#getUnicodeStream(int)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
		return rst.getUnicodeStream(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getUnicodeStream(java.lang.String)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public InputStream getUnicodeStream(final String columnName) throws SQLException {
		return rst.getUnicodeStream(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getURL(int)
	 */
	@Override
	public URL getURL(final int columnIndex) throws SQLException {
		return rst.getURL(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#getURL(java.lang.String)
	 */
	@Override
	public URL getURL(final String columnName) throws SQLException {
		return rst.getURL(columnName);
	}

	/**
	 * @see java.sql.ResultSet#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return rst.getWarnings();
	}

	/**
	 * @see java.sql.ResultSet#insertRow()
	 */
	@Override
	public void insertRow() throws SQLException {
		rst.insertRow();
	}

	/**
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	@Override
	public boolean isAfterLast() throws SQLException {

		return rst.isAfterLast();
	}

	/**
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	@Override
	public boolean isBeforeFirst() throws SQLException {
		return rst.isBeforeFirst();
	}

	/**
	 * @return is cached
	 */
	public boolean isCached() {
		return rst.isCached();
	}

	/**
	 * @return has records
	 */
	public boolean isEmpty() {
		return rst.isEmpty();
	}

	/**
	 * @see java.sql.ResultSet#isFirst()
	 */
	@Override
	public boolean isFirst() throws SQLException {
		return rst.isFirst();
	}

	/**
	 * @see java.sql.ResultSet#isLast()
	 */
	@Override
	public boolean isLast() throws SQLException {
		return rst.isLast();
	}

	/**
	 * @return iterator for he keys
	 */
	public Iterator<Collection.Key> keyIterator() {
		return rst.keyIterator();
	}

	/**
	 * @return all keys of the Query
	 */
	@SuppressWarnings("deprecation")
	public Key[] keys() {
		return rst.keys();
	}

	/**
	 * @see java.sql.ResultSet#last()
	 */
	@Override
	public boolean last() throws SQLException {

		return rst.last();
	}

	/**
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	@Override
	public void moveToCurrentRow() throws SQLException {
		rst.moveToCurrentRow();
	}

	/**
	 * @see java.sql.ResultSet#moveToInsertRow()
	 */
	@Override
	public void moveToInsertRow() throws SQLException {
		rst.moveToInsertRow();
	}

	/**
	 * @see java.sql.ResultSet#next()
	 */
	@Override
	@SuppressWarnings("deprecation")
	public boolean next() {
		return rst.next();
	}

	/**
	 * @see java.sql.ResultSet#previous()
	 */
	@Override
	public boolean previous() throws SQLException {
		return rst.previous();
	}

	/**
	 * @see java.sql.ResultSet#refreshRow()
	 */
	@Override
	public void refreshRow() throws SQLException {
		rst.refreshRow();
	}

	/**
	 * @see java.sql.ResultSet#relative(int)
	 */
	@Override
	public boolean relative(final int rows) throws SQLException {
		return rst.relative(rows);
	}

	/**
	 * @see java.sql.ResultSet#rowDeleted()
	 */
	@Override
	public boolean rowDeleted() throws SQLException {
		return rst.rowDeleted();
	}

	/**
	 * @see java.sql.ResultSet#rowInserted()
	 */
	@Override
	public boolean rowInserted() throws SQLException {
		return rst.rowInserted();
	}

	/**
	 * @see java.sql.ResultSet#rowUpdated()
	 */
	@Override
	public boolean rowUpdated() throws SQLException {
		return rst.rowUpdated();
	}

	/**
	 * @see java.sql.ResultSet#setFetchDirection(int)
	 */
	@Override
	public void setFetchDirection(final int direction) throws SQLException {
		rst.setFetchDirection(direction);
	}

	/**
	 * @see java.sql.ResultSet#setFetchSize(int)
	 */
	@Override
	public void setFetchSize(final int rows) throws SQLException {
		rst.setFetchSize(rows);
	}

	/**
	 * @return the size of the query
	 */
	public int size() {
		return rst.size();
	}

	/**
	 * @param keyColumn name of the column to sort
	 * @param order order type
	 * @throws PageException thrown when sorting fails
	 */
	public synchronized void sort(final Key keyColumn, final int order) throws PageException {

		rst.sort(keyColumn, order);
	}

	/**
	 * @param column name of the column to sort
	 * @throws PageException thrown when sorting fails
	 */
	public void sort(final Key column) throws PageException {

		rst.sort(column);
	}

	/**
	 * @param strColumn name of the column to sort
	 * @param order order type
	 * @throws PageException thrown when sorting fails
	 */
	@SuppressWarnings("deprecation")
	public synchronized void sort(final String strColumn, final int order) throws PageException {

		rst.sort(strColumn, order);
	}

	/**
	 * @param column name of the column to sort
	 * @throws PageException thrown when sorting fails
	 */
	@SuppressWarnings("deprecation")
	public void sort(final String column) throws PageException {

		rst.sort(column);
	}

	/**
	 * @param pageContext page context object
	 * @param maxlevel max level shown
	 * @param dp property data
	 * @return generated DumpData
	 */
	public DumpData toDumpData(final PageContext pageContext, final int maxlevel, final DumpProperties dp) {
		return rst.toDumpData(pageContext, maxlevel, dp);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return rst.toString();
	}

	/**
	 * @see java.sql.ResultSet#updateArray(int, java.sql.Array)
	 */
	@Override
	public void updateArray(final int columnIndex, final Array x) throws SQLException {
		rst.updateArray(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateArray(java.lang.String, java.sql.Array)
	 */
	@Override
	public void updateArray(final String columnName, final Array x) throws SQLException {
		rst.updateArray(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, int)
	 */
	@Override
	public void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
		rst.updateAsciiStream(columnIndex, x, length);
	}

	/**
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream, int)
	 */
	@Override
	public void updateAsciiStream(final String columnName, final InputStream x, final int length) throws SQLException {

		rst.updateAsciiStream(columnName, x, length);
	}

	/**
	 * @see java.sql.ResultSet#updateBigDecimal(int, java.math.BigDecimal)
	 */
	@Override
	public void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException {

		rst.updateBigDecimal(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	@Override
	public void updateBigDecimal(final String columnName, final BigDecimal x) throws SQLException {

		rst.updateBigDecimal(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, int)
	 */
	@Override
	public void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException {

		rst.updateBinaryStream(columnIndex, x, length);
	}

	/**
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream, int)
	 */
	@Override
	public void updateBinaryStream(final String columnName, final InputStream x, final int length) throws SQLException {

		rst.updateBinaryStream(columnName, x, length);
	}

	/**
	 * @see java.sql.ResultSet#updateBlob(int, java.sql.Blob)
	 */
	@Override
	public void updateBlob(final int columnIndex, final Blob x) throws SQLException {

		rst.updateBlob(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.sql.Blob)
	 */
	@Override
	public void updateBlob(final String columnName, final Blob x) throws SQLException {

		rst.updateBlob(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateBoolean(int, boolean)
	 */
	@Override
	public void updateBoolean(final int columnIndex, final boolean x) throws SQLException {

		rst.updateBoolean(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateBoolean(java.lang.String, boolean)
	 */
	@Override
	public void updateBoolean(final String columnName, final boolean x) throws SQLException {

		rst.updateBoolean(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateByte(int, byte)
	 */
	@Override
	public void updateByte(final int columnIndex, final byte x) throws SQLException {

		rst.updateByte(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateByte(java.lang.String, byte)
	 */
	@Override
	public void updateByte(final String columnName, final byte x) throws SQLException {

		rst.updateByte(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateBytes(int, byte[])
	 */
	@Override
	public void updateBytes(final int columnIndex, final byte[] x) throws SQLException {

		rst.updateBytes(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateBytes(java.lang.String, byte[])
	 */
	@Override
	public void updateBytes(final String columnName, final byte[] x) throws SQLException {
		rst.updateBytes(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, int)
	 */
	@Override
	public void updateCharacterStream(final int columnIndex, final Reader reader, final int length) throws SQLException {

		rst.updateCharacterStream(columnIndex, reader, length);
	}

	/**
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader, int)
	 */
	@Override
	public void updateCharacterStream(final String columnName, final Reader reader, final int length) throws SQLException {
		rst.updateCharacterStream(columnName, reader, length);
	}

	/**
	 * @see java.sql.ResultSet#updateClob(int, java.sql.Clob)
	 */
	@Override
	public void updateClob(final int columnIndex, final Clob x) throws SQLException {
		rst.updateClob(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)
	 */
	@Override
	public void updateClob(final String columnName, final Clob x) throws SQLException {
		rst.updateClob(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateDate(int, java.sql.Date)
	 */
	@Override
	public void updateDate(final int columnIndex, final Date x) throws SQLException {
		rst.updateDate(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateDate(java.lang.String, java.sql.Date)
	 */
	@Override
	public void updateDate(final String columnName, final Date x) throws SQLException {
		rst.updateDate(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateDouble(int, double)
	 */
	@Override
	public void updateDouble(final int columnIndex, final double x) throws SQLException {
		rst.updateDouble(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateDouble(java.lang.String, double)
	 */
	@Override
	public void updateDouble(final String columnName, final double x) throws SQLException {
		rst.updateDouble(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateFloat(int, float)
	 */
	@Override
	public void updateFloat(final int columnIndex, final float x) throws SQLException {
		rst.updateFloat(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateFloat(java.lang.String, float)
	 */
	@Override
	public void updateFloat(final String columnName, final float x) throws SQLException {
		rst.updateFloat(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateInt(int, int)
	 */
	@Override
	public void updateInt(final int columnIndex, final int x) throws SQLException {
		rst.updateInt(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateInt(java.lang.String, int)
	 */
	@Override
	public void updateInt(final String columnName, final int x) throws SQLException {
		rst.updateInt(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateLong(int, long)
	 */
	@Override
	public void updateLong(final int columnIndex, final long x) throws SQLException {
		rst.updateLong(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateLong(java.lang.String, long)
	 */
	@Override
	public void updateLong(final String columnName, final long x) throws SQLException {
		rst.updateLong(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateNull(int)
	 */
	@Override
	public void updateNull(final int columnIndex) throws SQLException {
		rst.updateNull(columnIndex);
	}

	/**
	 * @see java.sql.ResultSet#updateNull(java.lang.String)
	 */
	@Override
	public void updateNull(final String columnName) throws SQLException {
		rst.updateNull(columnName);
	}

	/**
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object, int)
	 */
	@Override
	public void updateObject(final int columnIndex, final Object x, final int scale) throws SQLException {
		rst.updateObject(columnIndex, x, scale);
	}

	/**
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object)
	 */
	@Override
	public void updateObject(final int columnIndex, final Object x) throws SQLException {
		rst.updateObject(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object, int)
	 */
	@Override
	public void updateObject(final String columnName, final Object x, final int scale) throws SQLException {
		rst.updateObject(columnName, x, scale);
	}

	/**
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public void updateObject(final String columnName, final Object x) throws SQLException {
		rst.updateObject(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateRef(int, java.sql.Ref)
	 */
	@Override
	public void updateRef(final int columnIndex, final Ref x) throws SQLException {
		rst.updateRef(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateRef(java.lang.String, java.sql.Ref)
	 */
	@Override
	public void updateRef(final String columnName, final Ref x) throws SQLException {
		rst.updateRef(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateRow()
	 */
	@Override
	public void updateRow() throws SQLException {
		rst.updateRow();
	}

	/**
	 * @see java.sql.ResultSet#updateShort(int, short)
	 */
	@Override
	public void updateShort(final int columnIndex, final short x) throws SQLException {
		rst.updateShort(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateShort(java.lang.String, short)
	 */
	@Override
	public void updateShort(final String columnName, final short x) throws SQLException {
		rst.updateShort(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateString(int, java.lang.String)
	 */
	@Override
	public void updateString(final int columnIndex, final String x) throws SQLException {
		rst.updateString(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateString(java.lang.String, java.lang.String)
	 */
	@Override
	public void updateString(final String columnName, final String x) throws SQLException {
		rst.updateString(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateTime(int, java.sql.Time)
	 */
	@Override
	public void updateTime(final int columnIndex, final Time x) throws SQLException {
		rst.updateTime(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateTime(java.lang.String, java.sql.Time)
	 */
	@Override
	public void updateTime(final String columnName, final Time x) throws SQLException {
		rst.updateTime(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#updateTimestamp(int, java.sql.Timestamp)
	 */
	@Override
	public void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException {
		rst.updateTimestamp(columnIndex, x);
	}

	/**
	 * @see java.sql.ResultSet#updateTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	@Override
	public void updateTimestamp(final String columnName, final Timestamp x) throws SQLException {
		rst.updateTimestamp(columnName, x);
	}

	/**
	 * @see java.sql.ResultSet#wasNull()
	 */
	@Override
	public boolean wasNull() throws SQLException {
		return rst.wasNull();
	}

	public lucee.runtime.type.Query getQuery() {
		return rst;
	}

	@Override
	public int getHoldability() throws SQLException {
		throw notSupported();
	}

	@Override
	public Reader getNCharacterStream(final int arg0) throws SQLException {
		throw notSupported();
	}

	@Override
	public Reader getNCharacterStream(final String arg0) throws SQLException {
		throw notSupported();
	}

	@Override
	public String getNString(final int arg0) throws SQLException {
		throw notSupported();
	}

	@Override
	public String getNString(final String arg0) throws SQLException {
		throw notSupported();
	}

	@Override
	public boolean isClosed() throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateAsciiStream(final int arg0, final InputStream arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateAsciiStream(final String arg0, final InputStream arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateAsciiStream(final int arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateAsciiStream(final String arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateBinaryStream(final int arg0, final InputStream arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateBinaryStream(final String arg0, final InputStream arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateBinaryStream(final int arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateBinaryStream(final String arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateBlob(final int arg0, final InputStream arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateBlob(final String arg0, final InputStream arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateBlob(final int arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateBlob(final String arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateCharacterStream(final int arg0, final Reader arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateCharacterStream(final String arg0, final Reader arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateCharacterStream(final int arg0, final Reader arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateCharacterStream(final String arg0, final Reader arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateClob(final int arg0, final Reader arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateClob(final String arg0, final Reader arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateClob(final int arg0, final Reader arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateClob(final String arg0, final Reader arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateNCharacterStream(final int arg0, final Reader arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateNCharacterStream(final String arg0, final Reader arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateNCharacterStream(final int arg0, final Reader arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateNCharacterStream(final String arg0, final Reader arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateNClob(final int arg0, final Reader arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateNClob(final String arg0, final Reader arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateNClob(final int arg0, final Reader arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateNClob(final String arg0, final Reader arg1, final long arg2) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateNString(final int arg0, final String arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public void updateNString(final String arg0, final String arg1) throws SQLException {
		throw notSupported();
	}

	@Override
	public boolean isWrapperFor(final Class<?> arg0) throws SQLException {
		throw notSupported();
	}

	@Override
	public <T> T unwrap(final Class<T> arg0) throws SQLException {
		throw notSupported();
	}

	// JDK6: uncomment this for compiling with JDK6
	@Override
	public NClob getNClob(final int arg0) throws SQLException {
		return rst.getNClob(arg0);
	}

	@Override
	public NClob getNClob(final String arg0) throws SQLException {
		return rst.getNClob(arg0);
	}

	@Override
	public RowId getRowId(final int arg0) throws SQLException {
		return rst.getRowId(arg0);
	}

	@Override
	public RowId getRowId(final String arg0) throws SQLException {
		return rst.getRowId(arg0);
	}

	@Override
	public SQLXML getSQLXML(final int arg0) throws SQLException {
		return rst.getSQLXML(arg0);
	}

	@Override
	public SQLXML getSQLXML(final String arg0) throws SQLException {
		return rst.getSQLXML(arg0);
	}

	@Override
	public void updateNClob(final int arg0, final NClob arg1) throws SQLException {
		rst.updateNClob(arg0, arg1);
	}

	@Override
	public void updateNClob(final String arg0, final NClob arg1) throws SQLException {
		rst.updateNClob(arg0, arg1);
	}

	@Override
	public void updateRowId(final int arg0, final RowId arg1) throws SQLException {
		rst.updateRowId(arg0, arg1);
	}

	@Override
	public void updateRowId(final String arg0, final RowId arg1) throws SQLException {
		rst.updateRowId(arg0, arg1);
	}

	@Override
	public void updateSQLXML(final int arg0, final SQLXML arg1) throws SQLException {
		rst.updateSQLXML(arg0, arg1);
	}

	@Override
	public void updateSQLXML(final String arg0, final SQLXML arg1) throws SQLException {
		rst.updateSQLXML(arg0, arg1);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException {
		try {
			final Method m = rst.getClass().getMethod("getObject", new Class[] { int.class, Class.class });
			return (T) m.invoke(rst, new Object[] { columnIndex, type });
		}
		catch (final Throwable t) {}
		throw notSupported();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getObject(final String columnLabel, final Class<T> type) throws SQLException {
		try {
			final Method m = rst.getClass().getMethod("getObject", new Class[] { String.class, Class.class });
			return (T) m.invoke(rst, new Object[] { columnLabel, type });
		}
		catch (final Throwable t) {}
		throw notSupported();
	}

	private SQLException notSupported() {
		return new SQLException("this feature is not supported");
	}
}