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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import lucee.commons.date.JREDateTimeUtil;
import lucee.commons.io.res.type.datasource.Attr;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.SQLImpl;
import lucee.runtime.engine.ThreadLocalPageContext;

public abstract class CoreSupport implements Core {
	public static final Attr ATTR_ROOT = new Attr(0, null, null, true, Attr.TYPE_DIRECTORY, 0, 0, (short) 0777, (short) 0, 0);

	public static boolean isDirectory(int type) {
		return type == Attr.TYPE_DIRECTORY;
	}

	public static boolean isFile(int type) {
		return type == Attr.TYPE_FILE;
	}

	public static boolean isLink(int type) {
		return type == Attr.TYPE_LINK;
	}

	public static Calendar getCalendar() {
		return JREDateTimeUtil.getThreadCalendar(ThreadLocalPageContext.getTimeZone());
	}

	public static void log(String s1) {
	}

	public static void log(String s1, String s2) {
	}

	public static void log(String s1, String s2, String s3) {
	}

	public static void log(String s1, String s2, String s3, String s4) {
	}

	PreparedStatement prepareStatement(DatasourceConnection dc, String sql) throws SQLException {
		return dc.getPreparedStatement(new SQLImpl(sql), false, true);
	}

}