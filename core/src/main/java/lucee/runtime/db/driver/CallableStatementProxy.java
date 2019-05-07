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
package lucee.runtime.db.driver;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;

public class CallableStatementProxy extends PreparedStatementProxy implements CallableStatement {

	protected CallableStatement stat;

	public CallableStatementProxy(ConnectionProxy conn, CallableStatement prepareCall, String sql) {
		super(conn, prepareCall, sql);
		this.stat = prepareCall;
	}

	@Override
	public Array getArray(int parameterIndex) throws SQLException {
		return stat.getArray(parameterIndex);
	}

	@Override
	public Array getArray(String parameterName) throws SQLException {
		return stat.getArray(parameterName);
	}

	@Override
	public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
		return stat.getBigDecimal(parameterIndex);
	}

	@Override
	public BigDecimal getBigDecimal(String parameterName) throws SQLException {
		return stat.getBigDecimal(parameterName);
	}

	@Override
	public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
		return stat.getBigDecimal(parameterIndex, scale);
	}

	@Override
	public Blob getBlob(int parameterIndex) throws SQLException {
		return stat.getBlob(parameterIndex);
	}

	@Override
	public Blob getBlob(String parameterName) throws SQLException {
		return stat.getBlob(parameterName);
	}

	@Override
	public boolean getBoolean(int parameterIndex) throws SQLException {
		return stat.getBoolean(parameterIndex);
	}

	@Override
	public boolean getBoolean(String parameterName) throws SQLException {
		return stat.getBoolean(parameterName);
	}

	@Override
	public byte getByte(int parameterIndex) throws SQLException {
		return stat.getByte(parameterIndex);
	}

	@Override
	public byte getByte(String parameterName) throws SQLException {
		return stat.getByte(parameterName);
	}

	@Override
	public byte[] getBytes(int parameterIndex) throws SQLException {
		return stat.getBytes(parameterIndex);
	}

	@Override
	public byte[] getBytes(String parameterName) throws SQLException {
		return stat.getBytes(parameterName);
	}

	@Override
	public Reader getCharacterStream(int parameterIndex) throws SQLException {
		return stat.getCharacterStream(parameterIndex);
	}

	@Override
	public Reader getCharacterStream(String parameterName) throws SQLException {
		return stat.getCharacterStream(parameterName);
	}

	@Override
	public Clob getClob(int parameterIndex) throws SQLException {
		return stat.getClob(parameterIndex);
	}

	@Override
	public Clob getClob(String parameterName) throws SQLException {
		return stat.getClob(parameterName);
	}

	@Override
	public Date getDate(int parameterIndex) throws SQLException {
		return stat.getDate(parameterIndex);
	}

	@Override
	public Date getDate(String parameterName) throws SQLException {
		return stat.getDate(parameterName);
	}

	@Override
	public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
		return stat.getDate(parameterIndex, cal);
	}

	@Override
	public Date getDate(String parameterName, Calendar cal) throws SQLException {
		return stat.getDate(parameterName, cal);
	}

	@Override
	public double getDouble(int parameterIndex) throws SQLException {
		return stat.getDouble(parameterIndex);
	}

	@Override
	public double getDouble(String parameterName) throws SQLException {
		return stat.getDouble(parameterName);
	}

	@Override
	public float getFloat(int parameterIndex) throws SQLException {
		return stat.getFloat(parameterIndex);
	}

	@Override
	public float getFloat(String parameterName) throws SQLException {
		return stat.getFloat(parameterName);
	}

	@Override
	public int getInt(int parameterIndex) throws SQLException {
		return stat.getInt(parameterIndex);
	}

	@Override
	public int getInt(String parameterName) throws SQLException {
		return stat.getInt(parameterName);
	}

	@Override
	public long getLong(int parameterIndex) throws SQLException {
		return stat.getLong(parameterIndex);
	}

	@Override
	public long getLong(String parameterName) throws SQLException {
		return stat.getLong(parameterName);
	}

	@Override
	public Reader getNCharacterStream(int parameterIndex) throws SQLException {
		return stat.getNCharacterStream(parameterIndex);
	}

	@Override
	public Reader getNCharacterStream(String parameterName) throws SQLException {
		return stat.getNCharacterStream(parameterName);
	}

	@Override
	public NClob getNClob(int parameterIndex) throws SQLException {
		return stat.getNClob(parameterIndex);
	}

	@Override
	public NClob getNClob(String parameterName) throws SQLException {
		return stat.getNClob(parameterName);
	}

	@Override
	public String getNString(int parameterIndex) throws SQLException {
		return stat.getNString(parameterIndex);
	}

	@Override
	public String getNString(String parameterName) throws SQLException {
		return stat.getNString(parameterName);
	}

	@Override
	public Object getObject(int parameterIndex) throws SQLException {
		return stat.getObject(parameterIndex);
	}

	@Override
	public Object getObject(String parameterName) throws SQLException {
		return stat.getObject(parameterName);
	}

	@Override
	public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
		return stat.getObject(parameterIndex, map);
	}

	@Override
	public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
		return stat.getObject(parameterName, map);
	}

	@Override
	public Ref getRef(int parameterIndex) throws SQLException {
		return stat.getRef(parameterIndex);
	}

	@Override
	public Ref getRef(String parameterName) throws SQLException {
		return stat.getRef(parameterName);
	}

	@Override
	public RowId getRowId(int parameterIndex) throws SQLException {
		return stat.getRowId(parameterIndex);
	}

	@Override
	public RowId getRowId(String parameterName) throws SQLException {
		return stat.getRowId(parameterName);
	}

	@Override
	public SQLXML getSQLXML(int parameterIndex) throws SQLException {
		return stat.getSQLXML(parameterIndex);
	}

	@Override
	public SQLXML getSQLXML(String parameterName) throws SQLException {
		return stat.getSQLXML(parameterName);
	}

	@Override
	public short getShort(int parameterIndex) throws SQLException {
		return stat.getShort(parameterIndex);
	}

	@Override
	public short getShort(String parameterName) throws SQLException {
		return stat.getShort(parameterName);
	}

	@Override
	public String getString(int parameterIndex) throws SQLException {
		return stat.getString(parameterIndex);
	}

	@Override
	public String getString(String parameterName) throws SQLException {
		return stat.getString(parameterName);
	}

	@Override
	public Time getTime(int parameterIndex) throws SQLException {
		return stat.getTime(parameterIndex);
	}

	@Override
	public Time getTime(String parameterName) throws SQLException {
		return stat.getTime(parameterName);
	}

	@Override
	public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
		return stat.getTime(parameterIndex, cal);
	}

	@Override
	public Time getTime(String parameterName, Calendar cal) throws SQLException {
		return stat.getTime(parameterName, cal);
	}

	@Override
	public Timestamp getTimestamp(int parameterIndex) throws SQLException {
		return stat.getTimestamp(parameterIndex);
	}

	@Override
	public Timestamp getTimestamp(String parameterName) throws SQLException {
		return stat.getTimestamp(parameterName);
	}

	@Override
	public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
		return stat.getTimestamp(parameterIndex, cal);
	}

	@Override
	public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
		return stat.getTimestamp(parameterName, cal);
	}

	@Override
	public URL getURL(int parameterIndex) throws SQLException {
		return stat.getURL(parameterIndex);
	}

	@Override
	public URL getURL(String parameterName) throws SQLException {
		return stat.getURL(parameterName);
	}

	@Override
	public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
		stat.registerOutParameter(parameterIndex, sqlType);
	}

	@Override
	public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
		stat.registerOutParameter(parameterName, sqlType);
	}

	@Override
	public void registerOutParameter(int parameterIndex, int sqlType, int typeName) throws SQLException {
		stat.registerOutParameter(parameterIndex, sqlType, typeName);
	}

	@Override
	public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
		stat.registerOutParameter(parameterIndex, sqlType, typeName);
	}

	@Override
	public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
		stat.registerOutParameter(parameterName, sqlType, scale);
	}

	@Override
	public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
		stat.registerOutParameter(parameterName, sqlType, typeName);
	}

	@Override
	public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
		stat.setAsciiStream(parameterName, x);
	}

	@Override
	public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
		stat.setAsciiStream(parameterName, x, length);
	}

	@Override
	public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
		stat.setAsciiStream(parameterName, x, length);
	}

	@Override
	public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
		stat.setBigDecimal(parameterName, x);
	}

	@Override
	public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
		stat.setBinaryStream(parameterName, x);
	}

	@Override
	public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
		stat.setBinaryStream(parameterName, x, length);
	}

	@Override
	public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
		stat.setBinaryStream(parameterName, x, length);
	}

	@Override
	public void setBlob(String parameterName, Blob x) throws SQLException {
		stat.setBlob(parameterName, x);
	}

	@Override
	public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
		stat.setBlob(parameterName, inputStream);
	}

	@Override
	public void setBlob(String parameterName, InputStream is, long length) throws SQLException {
		stat.setBlob(parameterName, is, length);
	}

	@Override
	public void setBoolean(String parameterName, boolean x) throws SQLException {
		stat.setBoolean(parameterName, x);
	}

	@Override
	public void setByte(String parameterName, byte x) throws SQLException {
		stat.setByte(parameterName, x);
	}

	@Override
	public void setBytes(String parameterName, byte[] x) throws SQLException {
		stat.setBytes(parameterName, x);
	}

	@Override
	public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
		stat.setCharacterStream(parameterName, reader);
	}

	@Override
	public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
		stat.setCharacterStream(parameterName, reader, length);
	}

	@Override
	public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
		stat.setCharacterStream(parameterName, reader, length);
	}

	@Override
	public void setClob(String parameterName, Clob x) throws SQLException {
		stat.setClob(parameterName, x);
	}

	@Override
	public void setClob(String parameterName, Reader reader) throws SQLException {
		stat.setClob(parameterName, reader);
	}

	@Override
	public void setClob(String parameterName, Reader reader, long length) throws SQLException {
		stat.setClob(parameterName, reader, length);
	}

	@Override
	public void setDate(String parameterName, Date x) throws SQLException {
		stat.setDate(parameterName, x);
	}

	@Override
	public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
		stat.setDate(parameterName, x, cal);
	}

	@Override
	public void setDouble(String parameterName, double x) throws SQLException {
		stat.setDouble(parameterName, x);
	}

	@Override
	public void setFloat(String parameterName, float x) throws SQLException {
		stat.setFloat(parameterName, x);
	}

	@Override
	public void setInt(String parameterName, int x) throws SQLException {
		stat.setInt(parameterName, x);
	}

	@Override
	public void setLong(String parameterName, long x) throws SQLException {
		stat.setLong(parameterName, x);
	}

	@Override
	public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
		stat.setNCharacterStream(parameterName, value);
	}

	@Override
	public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
		stat.setNCharacterStream(parameterName, value, length);
	}

	@Override
	public void setNClob(String parameterName, NClob value) throws SQLException {
		stat.setNClob(parameterName, value);
	}

	@Override
	public void setNClob(String parameterName, Reader reader) throws SQLException {
		stat.setNClob(parameterName, reader);
	}

	@Override
	public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
		stat.setNClob(parameterName, reader, length);
	}

	@Override
	public void setNString(String parameterName, String value) throws SQLException {
		stat.setNString(parameterName, value);
	}

	@Override
	public void setNull(String parameterName, int sqlType) throws SQLException {
		stat.setNull(parameterName, sqlType);
	}

	@Override
	public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
		stat.setNull(parameterName, sqlType, typeName);
	}

	@Override
	public void setObject(String parameterName, Object x) throws SQLException {
		stat.setObject(parameterName, x);
	}

	@Override
	public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
		stat.setObject(parameterName, x, targetSqlType);
	}

	@Override
	public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
		stat.setObject(parameterName, x, targetSqlType, scale);
	}

	@Override
	public void setRowId(String parameterName, RowId x) throws SQLException {
		stat.setRowId(parameterName, x);
	}

	@Override
	public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
		stat.setSQLXML(parameterName, xmlObject);
	}

	@Override
	public void setShort(String parameterName, short x) throws SQLException {
		stat.setShort(parameterName, x);
	}

	@Override
	public void setString(String parameterName, String x) throws SQLException {
		stat.setString(parameterName, x);
	}

	@Override
	public void setTime(String parameterName, Time x) throws SQLException {
		stat.setTime(parameterName, x);
	}

	@Override
	public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
		stat.setTime(parameterName, x, cal);
	}

	@Override
	public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
		stat.setTimestamp(parameterName, x);
	}

	@Override
	public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
		stat.setTimestamp(parameterName, x, cal);
	}

	@Override
	public void setURL(String parameterName, URL val) throws SQLException {
		stat.setURL(parameterName, val);
	}

	@Override
	public boolean wasNull() throws SQLException {
		return stat.wasNull();
	}

	// used only with java 7, do not set @Override
	public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
		// used reflection to make sure this work with Java 5 and 6
		try {
			return (T) stat.getClass().getMethod("getObject", new Class[] { int.class, Class.class }).invoke(stat, new Object[] { parameterIndex, type });
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (t instanceof InvocationTargetException && ((InvocationTargetException) t).getTargetException() instanceof SQLException)
				throw (SQLException) ((InvocationTargetException) t).getTargetException();
			throw new PageRuntimeException(Caster.toPageException(t));
		}
	}

	// used only with java 7, do not set @Override
	public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
		// used reflection to make sure this work with Java 5 and 6
		try {
			return (T) stat.getClass().getMethod("getObject", new Class[] { String.class, Class.class }).invoke(stat, new Object[] { parameterName, type });
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (t instanceof InvocationTargetException && ((InvocationTargetException) t).getTargetException() instanceof SQLException)
				throw (SQLException) ((InvocationTargetException) t).getTargetException();
			throw new PageRuntimeException(Caster.toPageException(t));
		}
	}
}