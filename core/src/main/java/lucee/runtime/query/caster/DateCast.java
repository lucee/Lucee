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
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

import lucee.commons.date.JREDateTimeUtil;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.dt.TimestampImpl;

public final class DateCast implements Cast {

	private boolean useTimeZone;

	public DateCast(boolean useTimeZone) {
		this.useTimeZone = useTimeZone;
	}

	@Override
	public Object toCFType(TimeZone tz, ResultSet rst, int columnIndex) throws SQLException, IOException {
		Date d = useTimeZone ? rst.getDate(columnIndex, JREDateTimeUtil.getThreadCalendar(tz)) : rst.getDate(columnIndex);
		if (d == null) return null;
		return Cast.JDBC_DATETIME_FORMAT ? new TimestampImpl(d, d.toString()) : new DateTimeImpl(d.getTime());

	}

	@Override
	public Object toCFType(TimeZone tz, ResultSet rst, int columnIndex, Calendar cal) throws SQLException, IOException {
		if (!useTimeZone || cal == null) {
			return toCFType( tz, rst, columnIndex );
		}
		Date d = rst.getDate( columnIndex, cal );
		if (d == null) return null;
		return new DateTimeImpl( d.getTime() );
	}

}