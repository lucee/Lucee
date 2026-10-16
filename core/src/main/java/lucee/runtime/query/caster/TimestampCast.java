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
package lucee.runtime.query.caster;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import lucee.commons.date.JREDateTimeUtil;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.dt.TimestampImpl;

public final class TimestampCast implements Cast {

	private final boolean useTimeZone;

	public TimestampCast(boolean useTimeZone) {
		this.useTimeZone = useTimeZone;
	}

	@Override
	public Object toCFType(TimeZone tz, ResultSet rst, int columnIndex) throws SQLException, IOException {
		Timestamp ts = useTimeZone ? rst.getTimestamp(columnIndex, JREDateTimeUtil.getThreadCalendar(tz)) : rst.getTimestamp(columnIndex);
		if (ts == null) return null;
		return Cast.JDBC_DATETIME_FORMAT ? new TimestampImpl(ts, ts.toString()) : new DateTimeImpl(ts.getTime());
	}

	@Override
	public Object toCFType(TimeZone tz, ResultSet rst, int columnIndex, Calendar cal) throws SQLException, IOException {
		if (!useTimeZone || cal == null) {
			return toCFType( tz, rst, columnIndex );
		}
		Timestamp ts = rst.getTimestamp( columnIndex, cal );
		if (ts == null) return null;
		return new DateTimeImpl( ts.getTime() );
	}
}