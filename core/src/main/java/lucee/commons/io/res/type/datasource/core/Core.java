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
package lucee.commons.io.res.type.datasource.core;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import lucee.commons.io.res.type.datasource.Attr;
import lucee.runtime.db.DatasourceConnection;

public interface Core {

	/**
	 * @return return true if this core support concatenation of existing data with new data
	 *         (getOutputStream(append:true))
	 */
	public boolean concatSupported();

	/**
	 * return a single Attr, if Attr does not exist it returns null
	 * 
	 * @param dc
	 * @param path
	 * @param name
	 * @param name2
	 * @return
	 * @throws SQLException
	 */
	public abstract Attr getAttr(DatasourceConnection dc, String prefix, int fullPathHash, String path, String name) throws SQLException;

	/**
	 * return all child Attrs of a given path
	 * 
	 * @param dc
	 * @param prefix
	 * @param path
	 * @return
	 * @throws SQLException
	 */
	public abstract List getAttrs(DatasourceConnection dc, String prefix, int pathHash, String path) throws SQLException;

	/**
	 * create a new entry (file or directory)
	 * 
	 * @param dc
	 * @param prefix
	 * @param path
	 * @param name
	 * @param type
	 * @throws SQLException
	 */
	public abstract void create(DatasourceConnection dc, String prefix, int fullPatHash, int pathHash, String path, String name, int type) throws SQLException;

	/**
	 * deletes an entry (file or directory)
	 * 
	 * @param dc
	 * @param prefix
	 * @param attr
	 * @return
	 * @throws SQLException
	 */
	public abstract boolean delete(DatasourceConnection dc, String prefix, Attr attr) throws SQLException;

	/**
	 * returns an inputStream to an entry data
	 * 
	 * @param dc
	 * @param prefix
	 * @param attr
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public abstract InputStream getInputStream(DatasourceConnection dc, String prefix, Attr attr) throws SQLException, IOException;

	public abstract void write(DatasourceConnection dc, String prefix, Attr attr, InputStream is, boolean append) throws SQLException;

	public abstract void setLastModified(DatasourceConnection dc, String prefix, Attr attr, long time) throws SQLException;

	public abstract void setMode(DatasourceConnection dc, String prefix, Attr attr, int mode) throws SQLException;

	public abstract void setAttributes(DatasourceConnection dc, String prefix, Attr attr, int attributes) throws SQLException;

}