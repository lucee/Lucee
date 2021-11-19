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
package lucee.runtime.type.sql;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

/**
 * Implementation of the Interface java.sql.Blob
 */
public final class BlobImpl implements java.sql.Blob, Serializable {
	byte[] binaryData = null;

	/**
	 * constructor of the class
	 * 
	 * @param data
	 */
	private BlobImpl(byte[] data) {
		binaryData = data;
	}

	@Override
	public long length() throws SQLException {
		return binaryData.length;
	}

	@Override
	public byte[] getBytes(long pos, int length) throws SQLException {
		byte[] newData = new byte[length];
		System.arraycopy(binaryData, (int) (pos - 1), newData, 0, length);
		return newData;
	}

	@Override
	public java.io.InputStream getBinaryStream() throws SQLException {
		return new ByteArrayInputStream(binaryData);
	}

	@Override
	public java.io.InputStream getBinaryStream(long pos, long length) {
		// TODO impl this
		return new ByteArrayInputStream(binaryData);
	}

	@Override
	public long position(byte pattern[], long start) throws SQLException {
		return (new String(binaryData)).indexOf(new String(pattern), (int) start);
	}

	@Override
	public long position(java.sql.Blob pattern, long start) throws SQLException {
		return position(pattern.getBytes(0, (int) pattern.length()), start);
	}

	@Override
	public int setBytes(long pos, byte[] bytes) throws SQLException {
		// TODO impl.
		throw new SQLException("JDBC 3.0 Method setBytes not implemented");
	}

	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
		// TODO impl.
		throw new SQLException("JDBC 3.0 Method setBytes not implemented");
	}

	@Override
	public java.io.OutputStream setBinaryStream(long pos) throws SQLException {
		// TODO impl.
		throw new SQLException("JDBC 3.0 Method setBinaryStream not implemented");
	}

	@Override
	public void truncate(long len) throws SQLException {
		// TODO impl.
		throw new SQLException("JDBC 3.0 Method truncate not implemented");
	}

	public static Blob toBlob(Object value) throws PageException {
		if (value instanceof Blob) return (Blob) value;
		return new BlobImpl(Caster.toBinary(value));
	}

	/*
	 * public static Blob toBlob(byte[] value) {
	 * 
	 * Class blobClass = ClassUtil.loadClass("oracle.sql.BLOB",null); if(blobClass!=null){ try { //BLOB
	 * blob = BLOB.getEmptyBLOB(); Method getEmptyBLOB = blobClass.getMethod("getEmptyBLOB",new
	 * Class[]{}); Object blob = getEmptyBLOB.invoke(null, ArrayUtil.OBJECT_EMPTY);
	 * 
	 * //blob.setBytes(value); Method setBytes = blobClass.getMethod("setBytes", new
	 * Class[]{byte[].class}); setBytes.invoke(blob, new Object[]{value});
	 * 
	 * return (Blob) blob; } catch (Exception e) {} } return new BlobImpl(value); }
	 */

	@Override
	public void free() {
		binaryData = new byte[0];
	}
}