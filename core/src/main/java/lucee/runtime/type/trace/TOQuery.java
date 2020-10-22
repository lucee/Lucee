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

import java.io.InputStream;
import java.io.Reader;
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
import java.util.Map;

import lucee.runtime.db.SQL;
import lucee.runtime.debug.Debugger;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.it.ForEachQueryIterator;
import lucee.runtime.type.util.QueryUtil;

public class TOQuery extends TOCollection implements Query, com.allaire.cfx.Query {

	private Query qry;

	protected TOQuery(Debugger debugger, Query qry, int type, String category, String text) {
		super(debugger, qry, type, category, text);
		this.qry = qry;
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

	public int removeRow(int row) throws PageException {
		return qry.removeRow(row);
	}

	@Override

	public int removeRowEL(int row) {

		return qry.removeRowEL(row);
	}

	@Override

	public QueryColumn removeColumn(String key) throws PageException {
		log(key);
		return qry.removeColumn(key);
	}

	@Override

	public QueryColumn removeColumn(Key key) throws PageException {
		log(key.getString());
		return qry.removeColumn(key);
	}

	@Override

	public QueryColumn removeColumnEL(String key) {
		log(key);
		return qry.removeColumnEL(key);
	}

	@Override

	public QueryColumn removeColumnEL(Key key) {
		log(key.getString());
		return qry.removeColumnEL(key);
	}

	@Override

	public Object setAt(String key, int row, Object value) throws PageException {
		log(key);
		return qry.setAt(key, row, value);
	}

	@Override

	public Object setAt(Key key, int row, Object value) throws PageException {
		log(key.getString());
		return qry.setAt(key, row, value);
	}

	@Override

	public Object setAtEL(String key, int row, Object value) {
		log(key);
		return qry.setAtEL(key, row, value);
	}

	@Override

	public Object setAtEL(Key key, int row, Object value) {
		log(key.getString());
		return qry.setAtEL(key, row, value);
	}

	@Override
	public Object setAtIndex(int index, int row, Object value, boolean trustType) throws PageException{	
		return qry.setAtIndex(index, row, value, trustType);
	}

	@Override

	public boolean next() {
		log();
		return qry.next();
	}

	@Override

	public boolean next(int pid) throws PageException {
		log();
		return qry.next(pid);
	}

	@Override

	public void reset() throws PageException {
		log();
		qry.reset();
	}

	@Override

	public void reset(int pid) throws PageException {
		log();
		qry.reset(pid);
	}

	@Override

	public int getRecordcount() {
		log();
		return qry.getRecordcount();
	}

	@Override
	public int getCurrentrow(int pid) {
		log();
		return qry.getCurrentrow(pid);
	}

	@Override

	public boolean go(int index, int pid) throws PageException {
		log();
		return qry.go(index, pid);
	}

	@Override

	public boolean isEmpty() {
		log();
		return qry.isEmpty();
	}

	@Override

	public void sort(String column) throws PageException {
		log(column);
		qry.sort(column);
	}

	@Override

	public void sort(Key column) throws PageException {
		log(column.getString());
		qry.sort(column);
	}

	@Override

	public void sort(String strColumn, int order) throws PageException {
		log(strColumn);
		qry.sort(strColumn, order);
	}

	@Override

	public void sort(Key keyColumn, int order) throws PageException {
		log(keyColumn.getString());
		qry.sort(keyColumn, order);
	}

	@Override

	public boolean addRow(int count) {
		log("" + count);
		return qry.addRow(count);
	}

	@Override

	public boolean addColumn(String columnName, Array content) throws PageException {
		log(columnName);
		return qry.addColumn(columnName, content);
	}

	@Override

	public boolean addColumn(Key columnName, Array content) throws PageException {
		log(columnName.getString());
		return qry.addColumn(columnName, content);
	}

	@Override

	public boolean addColumn(String columnName, Array content, int type) throws PageException {
		log(columnName);
		return qry.addColumn(columnName, content, type);
	}

	@Override

	public boolean addColumn(Key columnName, Array content, int type) throws PageException {
		log();
		return qry.addColumn(columnName, content, type);
	}

	@Override

	public int[] getTypes() {
		log();
		return qry.getTypes();
	}

	@Override

	public Map getTypesAsMap() {
		log();
		return qry.getTypesAsMap();
	}

	@Override

	public QueryColumn getColumn(String key) throws PageException {
		log(key);
		return qry.getColumn(key);
	}

	@Override

	public QueryColumn getColumn(Key key) throws PageException {
		log(key.getString());
		return qry.getColumn(key);
	}

	@Override

	public void rename(Key columnName, Key newColumnName) throws PageException {
		log(columnName + ":" + newColumnName);
		qry.rename(columnName, newColumnName);
	}

	@Override

	public QueryColumn getColumn(String key, QueryColumn defaultValue) {
		log(key);
		return qry.getColumn(key, defaultValue);
	}

	@Override

	public QueryColumn getColumn(Key key, QueryColumn defaultValue) {
		log(key.getString());
		return qry.getColumn(key, defaultValue);
	}

	@Override
	public void setExecutionTime(long exeTime) {
		log();
		qry.setExecutionTime(exeTime);
	}

	@Override

	public void setCached(boolean isCached) {
		log("" + isCached);
		qry.setCached(isCached);
	}

	@Override

	public boolean isCached() {
		log();
		return qry.isCached();
	}

	@Override

	public int addRow() {
		log();
		return qry.addRow();
	}

	@Override

	public int getColumnIndex(String coulmnName) {
		log(coulmnName);
		return qry.getColumnIndex(coulmnName);
	}

	@Override

	public String[] getColumns() {
		log();
		return qry.getColumns();
	}

	@Override

	public Key[] getColumnNames() {
		log();
		return qry.getColumnNames();
	}

	@Override

	public String[] getColumnNamesAsString() {
		log();
		return qry.getColumnNamesAsString();
	}

	@Override

	public String getData(int row, int col) throws IndexOutOfBoundsException {
		log(row + ":" + col);
		return qry.getData(row, col);
	}

	@Override

	public String getName() {
		log();
		return qry.getName();
	}

	@Override

	public int getRowCount() {
		log();
		return qry.getRowCount();
	}

	@Override

	public void setData(int row, int col, String value) throws IndexOutOfBoundsException {
		log("" + row);
		qry.setData(row, col, value);
	}

	@Override

	public Array getMetaDataSimple() {
		log();
		return qry.getMetaDataSimple();
	}

	@Override

	public Object getObject(String columnName) throws SQLException {
		log(columnName);
		return qry.getObject(columnName);
	}

	@Override

	public Object getObject(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getObject(columnIndex);
	}

	@Override

	public String getString(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getString(columnIndex);
	}

	@Override

	public String getString(String columnName) throws SQLException {
		log(columnName);
		return qry.getString(columnName);
	}

	@Override

	public boolean getBoolean(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getBoolean(columnIndex);
	}

	@Override

	public boolean getBoolean(String columnName) throws SQLException {
		log(columnName);
		return qry.getBoolean(columnName);
	}

	@Override

	public boolean wasNull() throws SQLException {
		log();
		return qry.wasNull();
	}

	@Override

	public boolean absolute(int row) throws SQLException {
		log();
		return qry.absolute(row);
	}

	@Override

	public void afterLast() throws SQLException {
		log();
		qry.afterLast();
	}

	@Override

	public void beforeFirst() throws SQLException {
		log();
		qry.beforeFirst();
	}

	@Override

	public void cancelRowUpdates() throws SQLException {
		log();
		qry.cancelRowUpdates();
	}

	@Override

	public void clearWarnings() throws SQLException {
		log();
		qry.clearWarnings();
	}

	@Override

	public void close() throws SQLException {
		log();
		qry.close();
	}

	@Override

	public void deleteRow() throws SQLException {
		log();
		qry.deleteRow();
	}

	@Override

	public int findColumn(String columnName) throws SQLException {
		log();
		return qry.findColumn(columnName);
	}

	@Override

	public boolean first() throws SQLException {
		log();
		return qry.first();
	}

	@Override

	public java.sql.Array getArray(int i) throws SQLException {
		log("" + i);
		return qry.getArray(i);
	}

	@Override

	public java.sql.Array getArray(String colName) throws SQLException {
		log(colName);
		return qry.getArray(colName);
	}

	@Override

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getAsciiStream(columnIndex);
	}

	@Override

	public InputStream getAsciiStream(String columnName) throws SQLException {
		log(columnName);
		return qry.getAsciiStream(columnName);
	}

	@Override

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getBigDecimal(columnIndex);
	}

	@Override

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		log(columnName);
		return qry.getBigDecimal(columnName);
	}

	@Override

	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		log("" + columnIndex);
		return qry.getBigDecimal(columnIndex, scale);
	}

	@Override

	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
		log(columnName);
		return qry.getBigDecimal(columnName, scale);
	}

	@Override

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getBinaryStream(columnIndex);
	}

	@Override

	public InputStream getBinaryStream(String columnName) throws SQLException {
		log(columnName);
		return qry.getBinaryStream(columnName);
	}

	@Override

	public Blob getBlob(int i) throws SQLException {
		log("" + i);
		return qry.getBlob(i);
	}

	@Override

	public Blob getBlob(String colName) throws SQLException {
		log(colName);
		return qry.getBlob(colName);
	}

	@Override

	public byte getByte(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getByte(columnIndex);
	}

	@Override

	public byte getByte(String columnName) throws SQLException {
		log("" + columnName);
		return qry.getByte(columnName);
	}

	@Override

	public byte[] getBytes(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getBytes(columnIndex);
	}

	@Override

	public byte[] getBytes(String columnName) throws SQLException {
		log(columnName);
		return qry.getBytes(columnName);
	}

	@Override

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getCharacterStream(columnIndex);
	}

	@Override

	public Reader getCharacterStream(String columnName) throws SQLException {
		log(columnName);
		return qry.getCharacterStream(columnName);
	}

	@Override

	public Clob getClob(int i) throws SQLException {
		log("" + i);
		return qry.getClob(i);
	}

	@Override

	public Clob getClob(String colName) throws SQLException {
		log(colName);
		return qry.getClob(colName);
	}

	@Override

	public int getConcurrency() throws SQLException {
		log();
		return qry.getConcurrency();
	}

	@Override

	public String getCursorName() throws SQLException {
		log();
		return qry.getCursorName();
	}

	@Override

	public Date getDate(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getDate(columnIndex);
	}

	@Override

	public Date getDate(String columnName) throws SQLException {
		log(columnName);
		return qry.getDate(columnName);
	}

	@Override

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		log(columnIndex + "");
		return qry.getDate(columnIndex, cal);
	}

	@Override

	public Date getDate(String columnName, Calendar cal) throws SQLException {
		log(columnName);
		return qry.getDate(columnName, cal);
	}

	@Override

	public double getDouble(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getDouble(columnIndex);
	}

	@Override

	public double getDouble(String columnName) throws SQLException {
		log(columnName);
		return qry.getDouble(columnName);
	}

	@Override

	public int getFetchDirection() throws SQLException {
		log();
		return qry.getFetchDirection();
	}

	@Override

	public int getFetchSize() throws SQLException {
		log();
		return qry.getFetchSize();
	}

	@Override

	public float getFloat(int columnIndex) throws SQLException {
		log(columnIndex + "");
		return qry.getFloat(columnIndex);
	}

	@Override

	public float getFloat(String columnName) throws SQLException {
		log(columnName);
		return qry.getFloat(columnName);
	}

	@Override

	public int getInt(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getInt(columnIndex);
	}

	@Override

	public int getInt(String columnName) throws SQLException {
		log(columnName);
		return qry.getInt(columnName);
	}

	@Override

	public long getLong(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getLong(columnIndex);
	}

	@Override

	public long getLong(String columnName) throws SQLException {
		log(columnName);
		return qry.getLong(columnName);
	}

	@Override

	public Ref getRef(int i) throws SQLException {
		log("" + i);
		return qry.getRef(i);
	}

	@Override

	public Ref getRef(String colName) throws SQLException {
		log(colName);
		return qry.getRef(colName);
	}

	@Override

	public int getRow() throws SQLException {
		log();
		return qry.getRow();
	}

	@Override

	public short getShort(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getShort(columnIndex);
	}

	@Override

	public short getShort(String columnName) throws SQLException {
		log(columnName);
		return qry.getShort(columnName);
	}

	@Override

	public Statement getStatement() throws SQLException {
		log();
		return qry.getStatement();
	}

	@Override

	public Time getTime(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getTime(columnIndex);
	}

	@Override

	public Time getTime(String columnName) throws SQLException {
		log(columnName);
		return qry.getTime(columnName);
	}

	@Override

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		log("" + columnIndex);
		return qry.getTime(columnIndex, cal);
	}

	@Override

	public Time getTime(String columnName, Calendar cal) throws SQLException {
		log(columnName);
		return qry.getTime(columnName, cal);
	}

	@Override

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getTimestamp(columnIndex);
	}

	@Override

	public Timestamp getTimestamp(String columnName) throws SQLException {
		log(columnName);
		return qry.getTimestamp(columnName);
	}

	@Override

	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		log("" + columnIndex);
		return qry.getTimestamp(columnIndex, cal);
	}

	@Override

	public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
		log(columnName);
		return qry.getTimestamp(columnName, cal);
	}

	@Override

	public int getType() throws SQLException {
		log();
		return qry.getType();
	}

	@Override

	public URL getURL(int columnIndex) throws SQLException {
		log();
		return qry.getURL(columnIndex);
	}

	@Override

	public URL getURL(String columnName) throws SQLException {
		log();
		return qry.getURL(columnName);
	}

	@Override

	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		log();
		return qry.getUnicodeStream(columnIndex);
	}

	@Override

	public InputStream getUnicodeStream(String columnName) throws SQLException {
		log();
		return qry.getUnicodeStream(columnName);
	}

	@Override

	public SQLWarning getWarnings() throws SQLException {
		log();
		return qry.getWarnings();
	}

	@Override

	public void insertRow() throws SQLException {
		log();
		qry.insertRow();
	}

	@Override

	public boolean isAfterLast() throws SQLException {
		log();
		return qry.isAfterLast();
	}

	@Override

	public boolean isBeforeFirst() throws SQLException {
		log();
		return qry.isBeforeFirst();
	}

	@Override

	public boolean isFirst() throws SQLException {
		log();
		return qry.isFirst();
	}

	@Override

	public boolean isLast() throws SQLException {
		log();
		return qry.isLast();
	}

	@Override

	public boolean last() throws SQLException {
		log();
		return qry.last();
	}

	@Override

	public void moveToCurrentRow() throws SQLException {
		log();
		qry.moveToCurrentRow();
	}

	@Override

	public void moveToInsertRow() throws SQLException {
		log();
		qry.moveToInsertRow();
	}

	@Override

	public boolean previous() throws SQLException {
		log();
		return qry.previous();
	}

	@Override

	public boolean previous(int pid) {
		log();
		return qry.previous(pid);
	}

	@Override

	public void refreshRow() throws SQLException {
		log();
		qry.refreshRow();
	}

	@Override

	public boolean relative(int rows) throws SQLException {
		log();
		return qry.relative(rows);
	}

	@Override

	public boolean rowDeleted() throws SQLException {
		log();
		return qry.rowDeleted();
	}

	@Override

	public boolean rowInserted() throws SQLException {
		log();
		return qry.rowInserted();
	}

	@Override

	public boolean rowUpdated() throws SQLException {
		log();
		return qry.rowUpdated();
	}

	@Override

	public void setFetchDirection(int direction) throws SQLException {
		log();
		qry.setFetchDirection(direction);
	}

	@Override

	public void setFetchSize(int rows) throws SQLException {
		log("" + rows);
		qry.setFetchSize(rows);
	}

	@Override

	public void updateArray(int columnIndex, java.sql.Array x) throws SQLException {
		log(columnIndex + "");
		qry.updateArray(columnIndex, x);
	}

	@Override

	public void updateArray(String columnName, java.sql.Array x) throws SQLException {
		log(columnName);
		qry.updateArray(columnName, x);
	}

	@Override

	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		log("" + columnIndex);
		qry.updateAsciiStream(columnIndex, x, length);
	}

	@Override

	public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
		log(columnName);
		qry.updateAsciiStream(columnName, x, length);
	}

	@Override

	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		log("" + columnIndex);
		qry.updateBigDecimal(columnIndex, x);
	}

	@Override

	public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
		log(columnName);
		qry.updateBigDecimal(columnName, x);
	}

	@Override

	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		log("" + columnIndex);
		qry.updateBinaryStream(columnIndex, x, length);
	}

	@Override

	public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
		log(columnName);
		qry.updateBinaryStream(columnName, x, length);
	}

	@Override

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		log("" + columnIndex);
		qry.updateBlob(columnIndex, x);
	}

	@Override

	public void updateBlob(String columnName, Blob x) throws SQLException {
		log(columnName);
		qry.updateBlob(columnName, x);
	}

	@Override

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		log("" + columnIndex);
		qry.updateBoolean(columnIndex, x);
	}

	@Override

	public void updateBoolean(String columnName, boolean x) throws SQLException {
		log(columnName);
		qry.updateBoolean(columnName, x);
	}

	@Override

	public void updateByte(int columnIndex, byte x) throws SQLException {
		log("" + columnIndex);
		qry.updateByte(columnIndex, x);
	}

	@Override

	public void updateByte(String columnName, byte x) throws SQLException {
		log(columnName);
		qry.updateByte(columnName, x);
	}

	@Override

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		log("" + columnIndex);
		qry.updateBytes(columnIndex, x);
	}

	@Override

	public void updateBytes(String columnName, byte[] x) throws SQLException {
		log(columnName);
		qry.updateBytes(columnName, x);
	}

	@Override

	public void updateCharacterStream(int columnIndex, Reader reader, int length) throws SQLException {
		log("" + columnIndex);
		qry.updateCharacterStream(columnIndex, reader, length);
	}

	@Override

	public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
		log(columnName);
		qry.updateCharacterStream(columnName, reader, length);
	}

	@Override

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		log("" + columnIndex);
		qry.updateClob(columnIndex, x);
	}

	@Override

	public void updateClob(String columnName, Clob x) throws SQLException {
		log(columnName);
		qry.updateClob(columnName, x);
	}

	@Override

	public void updateDate(int columnIndex, Date x) throws SQLException {
		log("" + columnIndex);
		qry.updateDate(columnIndex, x);
	}

	@Override

	public void updateDate(String columnName, Date x) throws SQLException {
		log(columnName);
		qry.updateDate(columnName, x);
	}

	@Override

	public void updateDouble(int columnIndex, double x) throws SQLException {
		log("" + columnIndex);
		qry.updateDouble(columnIndex, x);
	}

	@Override

	public void updateDouble(String columnName, double x) throws SQLException {
		log(columnName);
		qry.updateDouble(columnName, x);
	}

	@Override

	public void updateFloat(int columnIndex, float x) throws SQLException {
		log("" + columnIndex);
		qry.updateFloat(columnIndex, x);
	}

	@Override

	public void updateFloat(String columnName, float x) throws SQLException {
		log(columnName);
		qry.updateFloat(columnName, x);
	}

	@Override

	public void updateInt(int columnIndex, int x) throws SQLException {
		log("" + columnIndex);
		qry.updateInt(columnIndex, x);
	}

	@Override

	public void updateInt(String columnName, int x) throws SQLException {
		log(columnName);
		qry.updateInt(columnName, x);
	}

	@Override

	public void updateLong(int columnIndex, long x) throws SQLException {
		log("" + columnIndex);
		qry.updateLong(columnIndex, x);
	}

	@Override

	public void updateLong(String columnName, long x) throws SQLException {
		log(columnName);
		qry.updateLong(columnName, x);
	}

	@Override

	public void updateNull(int columnIndex) throws SQLException {
		log("" + columnIndex);
		qry.updateNull(columnIndex);
	}

	@Override

	public void updateNull(String columnName) throws SQLException {
		log(columnName);
		qry.updateNull(columnName);
	}

	@Override

	public void updateObject(int columnIndex, Object x) throws SQLException {

		qry.updateObject(columnIndex, x);
	}

	@Override

	public void updateObject(String columnName, Object x) throws SQLException {
		log(columnName);
		qry.updateObject(columnName, x);
	}

	@Override

	public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
		log("" + columnIndex);
		qry.updateObject(columnIndex, x, scale);
	}

	@Override

	public void updateObject(String columnName, Object x, int scale) throws SQLException {
		log(columnName);
		qry.updateObject(columnName, x, scale);
	}

	@Override

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		log("" + columnIndex);
		qry.updateRef(columnIndex, x);
	}

	@Override

	public void updateRef(String columnName, Ref x) throws SQLException {
		log(columnName);
		qry.updateRef(columnName, x);
	}

	@Override

	public void updateRow() throws SQLException {
		log();
		qry.updateRow();
	}

	@Override

	public void updateShort(int columnIndex, short x) throws SQLException {
		log("" + columnIndex);
		qry.updateShort(columnIndex, x);
	}

	@Override

	public void updateShort(String columnName, short x) throws SQLException {
		log(columnName);
		qry.updateShort(columnName, x);
	}

	@Override

	public void updateString(int columnIndex, String x) throws SQLException {
		log("" + columnIndex);
		qry.updateString(columnIndex, x);
	}

	@Override

	public void updateString(String columnName, String x) throws SQLException {
		log(columnName);
		qry.updateString(columnName, x);
	}

	@Override

	public void updateTime(int columnIndex, Time x) throws SQLException {
		log("" + columnIndex);
		qry.updateTime(columnIndex, x);
	}

	@Override

	public void updateTime(String columnName, Time x) throws SQLException {
		log(columnName);
		qry.updateTime(columnName, x);
	}

	@Override

	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		log("" + columnIndex);
		qry.updateTimestamp(columnIndex, x);
	}

	@Override

	public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
		log(columnName);
		qry.updateTimestamp(columnName, x);
	}

	@Override

	public ResultSetMetaData getMetaData() throws SQLException {
		log();
		return qry.getMetaData();
	}

	@Override

	public int getHoldability() throws SQLException {
		log();
		return qry.getHoldability();
	}

	@Override

	public boolean isClosed() throws SQLException {
		log();
		return qry.isClosed();
	}

	@Override

	public void updateNString(int columnIndex, String nString) throws SQLException {
		log("" + columnIndex);
		qry.updateNString(columnIndex, nString);
	}

	@Override

	public void updateNString(String columnLabel, String nString) throws SQLException {
		log(columnLabel);
		qry.updateNString(columnLabel, nString);
	}

	@Override

	public String getNString(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getNString(columnIndex);
	}

	@Override

	public String getNString(String columnLabel) throws SQLException {
		log(columnLabel);
		return qry.getNString(columnLabel);
	}

	@Override

	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		log("" + columnIndex);
		return qry.getNCharacterStream(columnIndex);
	}

	@Override

	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		log(columnLabel);
		return qry.getNCharacterStream(columnLabel);
	}

	@Override

	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		log("" + columnIndex);
		qry.updateNCharacterStream(columnIndex, x, length);
	}

	@Override

	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		log(columnLabel);
		qry.updateNCharacterStream(columnLabel, reader, length);
	}

	@Override

	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		log("" + columnIndex);
		qry.updateAsciiStream(columnIndex, x, length);
	}

	@Override

	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		log(columnIndex + "");
		qry.updateBinaryStream(columnIndex, x, length);
	}

	@Override

	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		log(columnIndex + "");
		qry.updateCharacterStream(columnIndex, x, length);
	}

	@Override

	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		log(columnLabel);
		qry.updateAsciiStream(columnLabel, x, length);
	}

	@Override

	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		log(columnLabel);
		qry.updateBinaryStream(columnLabel, x, length);
	}

	@Override

	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		log(columnLabel);
		qry.updateCharacterStream(columnLabel, reader, length);
	}

	@Override

	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		log("" + columnIndex);
		qry.updateBlob(columnIndex, inputStream, length);
	}

	@Override

	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		log(columnLabel);
		qry.updateBlob(columnLabel, inputStream, length);
	}

	@Override

	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		log("" + columnIndex);
		qry.updateClob(columnIndex, reader, length);
	}

	@Override

	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		log(columnLabel);
		qry.updateClob(columnLabel, reader, length);
	}

	@Override

	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		log("" + columnIndex);
		qry.updateNClob(columnIndex, reader, length);
	}

	@Override

	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		log(columnLabel);
		qry.updateNClob(columnLabel, reader, length);
	}

	@Override

	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		log("" + columnIndex);
		qry.updateNCharacterStream(columnIndex, x);
	}

	@Override

	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		log(columnLabel);
		qry.updateNCharacterStream(columnLabel, reader);
	}

	@Override

	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		log("" + columnIndex);
		qry.updateAsciiStream(columnIndex, x);
	}

	@Override

	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		log("" + columnIndex);
		qry.updateBinaryStream(columnIndex, x);
	}

	@Override

	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		log("" + columnIndex);
		qry.updateCharacterStream(columnIndex, x);
	}

	@Override

	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		log(columnLabel);
		qry.updateAsciiStream(columnLabel, x);
	}

	@Override

	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		log(columnLabel);
		qry.updateBinaryStream(columnLabel, x);
	}

	@Override

	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		log(columnLabel);
		qry.updateCharacterStream(columnLabel, reader);
	}

	@Override

	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		log("" + columnIndex);
		qry.updateBlob(columnIndex, inputStream);
	}

	@Override

	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		log(columnLabel);
		qry.updateBlob(columnLabel, inputStream);
	}

	@Override

	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		log("" + columnIndex);
		qry.updateClob(columnIndex, reader);
	}

	@Override

	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		log(columnLabel);
		qry.updateClob(columnLabel, reader);
	}

	@Override

	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		log("" + columnIndex);
		qry.updateNClob(columnIndex, reader);
	}

	@Override

	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		log(columnLabel);
		qry.updateNClob(columnLabel, reader);
	}

	@Override

	public <T> T unwrap(Class<T> iface) throws SQLException {
		log();
		return qry.unwrap(iface);
	}

	@Override

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		log();
		return qry.isWrapperFor(iface);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		log();
		return new TOQuery(debugger, (Query) Duplicator.duplicate(qry, deepCopy), type, category, text);
	}

	@Override
	public NClob getNClob(int arg0) throws SQLException {
		log("" + arg0);
		return qry.getNClob(arg0);
	}

	@Override
	public NClob getNClob(String arg0) throws SQLException {
		log(arg0);
		return qry.getNClob(arg0);
	}

	@Override
	public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException {
		log("" + arg0);
		return qry.getObject(arg0, arg1);
	}

	@Override
	public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException {
		log(arg0);
		return qry.getObject(arg0, arg1);
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
	public RowId getRowId(int arg0) throws SQLException {
		log("" + arg0);
		return qry.getRowId(arg0);
	}

	@Override
	public RowId getRowId(String arg0) throws SQLException {
		log(arg0);
		return qry.getRowId(arg0);
	}

	@Override
	public SQLXML getSQLXML(int arg0) throws SQLException {
		log("" + arg0);
		return qry.getSQLXML(arg0);
	}

	@Override
	public SQLXML getSQLXML(String arg0) throws SQLException {
		log(arg0);
		return qry.getSQLXML(arg0);
	}

	@Override
	public void updateNClob(int arg0, NClob arg1) throws SQLException {
		log("" + arg0);
		qry.updateNClob(arg0, arg1);
	}

	@Override
	public void updateNClob(String arg0, NClob arg1) throws SQLException {
		log(arg0);
		qry.updateNClob(arg0, arg1);
	}

	@Override
	public void updateRowId(int arg0, RowId arg1) throws SQLException {
		log("" + arg0);
		qry.updateRowId(arg0, arg1);
	}

	@Override
	public void updateRowId(String arg0, RowId arg1) throws SQLException {
		log(arg0);
		qry.updateRowId(arg0, arg1);
	}

	@Override
	public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
		log(arg0 + "");
		qry.updateSQLXML(arg0, arg1);
	}

	@Override
	public void updateSQLXML(String columnIndex, SQLXML x) throws SQLException {
		log(columnIndex);
		qry.updateSQLXML(columnIndex, x);
	}

	@Override
	public SQL getSql() {
		log();
		return qry.getSql();
	}

	@Override
	public String getTemplate() {
		log();
		return qry.getTemplate();
	}

	@Override
	public long getExecutionTime() {
		log();
		return qry.getExecutionTime();
	}

	@Override
	public java.util.Iterator getIterator() {
		log();
		return new ForEachQueryIterator(null, this, ThreadLocalPageContext.get().getId());
	}

	@Override
	public String getCacheType() {
		log();
		return qry.getCacheType();
	}

	@Override
	public void setCacheType(String cacheType) {
		log(cacheType);
		qry.setCacheType(cacheType);
	}

	@Override
	public int getColumnCount() {
		log();
		return qry.getColumnCount();
	}

	@Override
	public void enableShowQueryUsage() {
		log();
		qry.enableShowQueryUsage();
	}
}