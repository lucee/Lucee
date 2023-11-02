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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import lucee.commons.db.DBUtil;
import lucee.commons.io.res.type.datasource.Attr;
import lucee.commons.lang.StringUtil;
import lucee.runtime.db.DatasourceConnection;

public class MSSQL extends CoreSupport {

	private static final int DEFAULT_MODE = 0777;
	private static final int DEFAULT_ATTRS = 0;

	public MSSQL(DatasourceConnection dc, String prefix) throws SQLException {
		Connection conn = dc.getConnection();
		Statement stat1 = null;
		ResultSet rs = null;
		boolean installAttrs = true;
		boolean installData = true;

		// check attr
		String sql = "select count(*) as cnt from dbo.sysobjects where id = object_id(N'[dbo].[" + prefix + "attrs]') and OBJECTPROPERTY(id, N'IsUserTable') = 1";
		try {
			stat1 = conn.createStatement();
			rs = stat1.executeQuery(sql);
			if (rs.next()) {
				if (rs.getInt(1) > 0) installAttrs = false;
			}
		}
		finally {
			DBUtil.closeEL(rs);
			DBUtil.closeEL(stat1);
		}

		// check data
		sql = "select count(*) as CNT from dbo.sysobjects where id = object_id(N'[dbo].[" + prefix + "data]') and OBJECTPROPERTY(id, N'IsUserTable') = 1";
		try {
			stat1 = conn.createStatement();
			rs = stat1.executeQuery(sql);
			if (rs.next()) {
				if (rs.getInt(1) > 0) installData = false;
			}
		}
		finally {
			DBUtil.closeEL(rs);
			DBUtil.closeEL(stat1);
		}

		if (installAttrs) {
			execute(conn,
					"CREATE TABLE [dbo].[" + prefix + "attrs] (" + "[rdr_id] [int] IDENTITY (1, 1) NOT NULL ," + "[rdr_name] [varchar] (255) COLLATE Latin1_General_CI_AS NULL ,"
							+ "[rdr_path_hash] [int] NULL ," + "[rdr_full_path_hash] [int] NULL ," + "[rdr_path] [varchar] (2048) COLLATE Latin1_General_CI_AS NULL ,"
							+ "[rdr_type] [int] NULL ," + "[rdr_last_modified] [datetime] NULL ," + "[rdr_mode] [int] NULL ," + "[rdr_attributes] [int] NULL ,"
							+ "[rdr_data] [int] NULL ," + "[rdr_length] [int] NULL" + ") ON [PRIMARY]");

			execute(conn,
					"ALTER TABLE [dbo].[" + prefix + "attrs] WITH NOCHECK ADD " + "CONSTRAINT [PK_" + prefix + "attrs] PRIMARY KEY  CLUSTERED " + "([rdr_id])  ON [PRIMARY] ");

			execute(conn, "ALTER TABLE [dbo].[" + prefix + "attrs] ADD " + "CONSTRAINT [DF_" + prefix + "attrs_rdr_mode] DEFAULT (0) FOR [rdr_mode]," + "CONSTRAINT [DF_" + prefix
					+ "attrs_rdr_attributes] DEFAULT (0) FOR [rdr_attributes]," + "CONSTRAINT [DF_" + prefix + "attrs_rdr_length] DEFAULT (0) FOR [rdr_length]");

			execute(conn, "CREATE  INDEX [IDX_name] ON [dbo].[" + prefix + "attrs]([rdr_name]) ON [PRIMARY]");
			execute(conn, "CREATE  INDEX [IDX_id] ON [dbo].[" + prefix + "attrs]([rdr_data]) ON [PRIMARY]");
			execute(conn, "CREATE  INDEX [idx_path] ON [dbo].[" + prefix + "attrs]([rdr_path_hash]) ON [PRIMARY]");
			execute(conn, "CREATE  INDEX [idx_full_path] ON [dbo].[" + prefix + "attrs]([rdr_full_path_hash]) ON [PRIMARY]");
		}

		if (installData) {
			execute(conn, "CREATE TABLE [dbo].[" + prefix + "data] (" + "[rdr_id] [int] IDENTITY (1, 1) NOT NULL ," + "[rdr_data] [image] NULL"
					+ ") ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]");

			execute(conn, "ALTER TABLE [dbo].[" + prefix + "data] WITH NOCHECK ADD " + "CONSTRAINT [PK_" + prefix + "data] PRIMARY KEY  CLUSTERED " + "([rdr_id])  ON [PRIMARY] ");
		}
	}

	private void execute(Connection conn, String sql) throws SQLException {
		log(sql);
		Statement stat = null;
		try {
			stat = conn.createStatement();
			stat.executeUpdate(sql);
		}
		finally {
			DBUtil.closeEL(stat);
		}
	}

	@Override
	public Attr getAttr(DatasourceConnection dc, String prefix, int fullPathHash, String path, String name) throws SQLException {
		// ROOT
		if (StringUtil.isEmpty(path)) return ATTR_ROOT;

		String sql = "select rdr_id,rdr_type,rdr_length,rdr_last_modified,rdr_mode,rdr_attributes,rdr_data from " + prefix
				+ "attrs where rdr_full_path_hash=? and rdr_path=? and rdr_name=?";
		PreparedStatement stat = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
		stat.setInt(1, fullPathHash);
		stat.setString(2, path);
		stat.setString(3, name);
		log(sql, fullPathHash + "", path, name);

		ResultSet rs = stat.executeQuery();
		try {
			if (!rs.next()) return null;

			return new Attr(rs.getInt(1), name, path, true, rs.getInt(2), rs.getInt(3), rs.getTimestamp(4, getCalendar()).getTime(), rs.getShort(5), rs.getShort(6), rs.getInt(7));
		}
		finally {
			DBUtil.closeEL(rs);
			// DBUtil.closeEL(stat);
		}
	}

	@Override
	public List getAttrs(DatasourceConnection dc, String prefix, int pathHash, String path) throws SQLException {
		String sql = "select rdr_id,rdr_name,rdr_type,rdr_length,rdr_last_modified,rdr_mode,rdr_attributes,rdr_data from " + prefix
				+ "attrs where rdr_path_hash=? and rdr_path=? order by rdr_name";
		PreparedStatement stat = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
		stat.setInt(1, pathHash);
		stat.setString(2, path);
		log(sql, pathHash + "", path);

		ResultSet rs = stat.executeQuery();

		try {
			List attrs = new ArrayList();
			// hashCode=(path+name).hashCode();
			while (rs.next()) {
				attrs.add(new Attr(rs.getInt(1), rs.getString(2), path, true, rs.getInt(3), rs.getInt(4), rs.getTimestamp(5, getCalendar()).getTime(), rs.getShort(6),
						rs.getShort(7), rs.getInt(8)));
			}
			return attrs;
		}
		finally {
			DBUtil.closeEL(rs);
			// DBUtil.closeEL(stat);
		}
	}

	@Override
	public void create(DatasourceConnection dc, String prefix, int fullPatHash, int pathHash, String path, String name, int type) throws SQLException {
		String sql = "insert into " + prefix + "attrs(rdr_type,rdr_path,rdr_name,rdr_full_path_hash,rdr_path_hash,rdr_last_modified,rdr_mode,rdr_attributes,rdr_data,rdr_length) "
				+ "values(?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement stat = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
		log(sql);
		stat.setInt(1, type);
		stat.setString(2, path);
		stat.setString(3, name);
		stat.setInt(4, fullPatHash);
		stat.setInt(5, pathHash);
		stat.setTimestamp(6, new Timestamp(System.currentTimeMillis()), getCalendar());
		stat.setInt(7, DEFAULT_MODE);
		stat.setInt(8, DEFAULT_ATTRS);
		stat.setInt(9, 0);
		stat.setInt(10, 0);
		// try{
		stat.executeUpdate();
		/*
		 * } finally { //DBUtil.closeEL(stat); }
		 */
	}

	@Override
	public boolean delete(DatasourceConnection dc, String prefix, Attr attr) throws SQLException {
		boolean rst = false;
		if (attr != null) {
			String sql = "delete from " + prefix + "attrs where rdr_id=?";
			log(sql, attr.getId() + "");
			PreparedStatement stat = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
			stat.setInt(1, attr.getId());

			// try{
			rst = stat.executeUpdate() > 0;
			/*
			 * } finally { //DBUtil.closeEL(stat); }
			 */

			if (attr.getData() > 0) {
				sql = "delete from " + prefix + "data where rdr_id=?";
				log(sql, attr.getData() + "");
				stat = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
				stat.setInt(1, attr.getData());
				// try{
				stat.executeUpdate();
				/*
				 * } finally { //DBUtil.closeEL(stat); }
				 */
			}
		}
		return rst;
	}

	@Override
	public InputStream getInputStream(DatasourceConnection dc, String prefix, Attr attr) throws SQLException, IOException {
		if (attr == null || attr.getData() == 0) return new ByteArrayInputStream(new byte[0]);

		String sql = "select rdr_data from " + prefix + "data where rdr_id=?";
		log(sql, attr.getData() + "");
		PreparedStatement stat = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
		stat.setInt(1, attr.getData());

		ResultSet rs = null;
		try {
			rs = stat.executeQuery();
			if (!rs.next()) {
				throw new IOException("Can't read data from [" + attr.getParent() + attr.getName() +"]");
			}
			return rs.getBlob(1).getBinaryStream();
		}
		finally {
			DBUtil.closeEL(rs);
			// DBUtil.closeEL(stat);
		}
	}

	@Override
	public void write(DatasourceConnection dc, String prefix, Attr attr, InputStream is, boolean append) throws SQLException {
		if (attr.getData() == 0) {
			writeInsert(dc, prefix, attr, is);
		}
		else writeUpdate(dc, prefix, attr, is, append);
	}

	private void writeUpdate(DatasourceConnection dc, String prefix, Attr attr, InputStream is, boolean append) throws SQLException {
		String sql;

		if (append) {
			sql = "DECLARE @ptrval binary(16);" + "DECLARE @iLen int;" + "SELECT @ptrval = TEXTPTR(rdr_data), @iLen = dataLength(rdr_data)" + "FROM " + prefix + "data "
					+ "WHERE rdr_id = ? " + "UPDATETEXT " + prefix + "data.rdr_data @ptrval @iLen 0 ?;";
		}
		else {
			sql = "update " + prefix + "data set rdr_data=? where rdr_id=?";
		}

		log(sql);
		PreparedStatement stat1 = null;
		PreparedStatement stat2 = null;
		PreparedStatement stat3 = null;
		ResultSet rs = null;
		// try{
		// Connection conn = dc.getConnection();
		stat1 = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
		if (append) {
			stat1.setInt(1, attr.getData());
			stat1.setBinaryStream(2, is, -1);
		}
		else {
			stat1.setBinaryStream(1, is, -1);
			stat1.setInt(2, attr.getData());
		}
		stat1.executeUpdate();

		// select
		sql = "select dataLength(rdr_data) as DataLen from " + prefix + "data where rdr_id=?";
		log(sql);
		stat2 = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
		stat2.setInt(1, attr.getData());
		rs = stat2.executeQuery();

		if (rs.next()) {
			sql = "update " + prefix + "attrs set rdr_length=? where rdr_id=?";
			log(sql);
			stat3 = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
			stat3.setInt(1, rs.getInt(1));
			stat3.setInt(2, attr.getId());
			stat3.executeUpdate();
		}
		/*
		 * } finally { //DBUtil.closeEL(stat1); //DBUtil.closeEL(stat2); //DBUtil.closeEL(stat3); }
		 */
	}

	private void writeInsert(DatasourceConnection dc, String prefix, Attr attr, InputStream is) throws SQLException {
		PreparedStatement stat1 = null;
		PreparedStatement stat2 = null;
		PreparedStatement stat3 = null;
		ResultSet rs = null;
		try {
			// Insert
			String sql = "insert into " + prefix + "data (rdr_data) values(?)";
			log(sql);
			// Connection conn = dc.getConnection();
			stat1 = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
			stat1.setBinaryStream(1, is, -1);
			stat1.execute();

			// select
			sql = "select TOP 1 rdr_id,dataLength(rdr_data) as DataLen from " + prefix + "data order by rdr_id desc";
			log(sql);
			stat2 = prepareStatement(dc, sql);// conn.createStatement();
			rs = stat2.executeQuery();

			// update
			if (rs.next()) {
				sql = "update " + prefix + "attrs set rdr_data=?,rdr_length=? where rdr_id=?";
				log(sql);
				stat3 = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
				stat3.setInt(1, rs.getInt(1));
				stat3.setInt(2, rs.getInt(2));
				stat3.setInt(3, attr.getId());
				stat3.executeUpdate();
			}

		}
		finally {
			DBUtil.closeEL(rs);
			// DBUtil.closeEL(stat1);
			// DBUtil.closeEL(stat2);
			// DBUtil.closeEL(stat3);
		}
	}

	@Override
	public void setLastModified(DatasourceConnection dc, String prefix, Attr attr, long time) throws SQLException {
		String sql = "update " + prefix + "attrs set rdr_last_modified=? where rdr_id=?";
		log(sql);
		PreparedStatement stat = null;
		// try{
		stat = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
		stat.setTimestamp(1, new Timestamp(time), getCalendar());
		stat.setInt(2, attr.getId());
		stat.executeUpdate();
		/*
		 * } finally { //DBUtil.closeEL(stat); }
		 */
	}

	@Override
	public void setMode(DatasourceConnection dc, String prefix, Attr attr, int mode) throws SQLException {
		String sql = "update " + prefix + "attrs set rdr_mode=? where rdr_id=?";
		log(sql);
		PreparedStatement stat = null;
		// try{
		stat = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
		stat.setInt(1, mode);
		stat.setInt(2, attr.getId());
		stat.executeUpdate();
		/*
		 * } finally { //DBUtil.closeEL(stat); }
		 */
	}

	@Override
	public void setAttributes(DatasourceConnection dc, String prefix, Attr attr, int attributes) throws SQLException {
		String sql = "update " + prefix + "attrs set rdr_attributes=? where rdr_id=?";
		log(sql);
		PreparedStatement stat = null;
		// try{
		stat = prepareStatement(dc, sql);// dc.getConnection().prepareStatement(sql);
		stat.setInt(1, attributes);
		stat.setInt(2, attr.getId());
		stat.executeUpdate();
		/*
		 * } finally { //DBUtil.closeEL(stat); }
		 */
	}

	@Override
	public boolean concatSupported() {
		return true;
	}
}