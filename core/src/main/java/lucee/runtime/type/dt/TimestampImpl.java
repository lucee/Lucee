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
package lucee.runtime.type.dt;

import java.util.Date;
import java.util.TimeZone;

/**
 * DateTimeImpl for a value read from a JDBC DATE/TIME/TIMESTAMP column, preserving the driver's own
 * toString() representation instead of Lucee's {ts '...'} ODBC escape format - see LDEV-1344
 */
public final class TimestampImpl extends DateTimeImpl {

	private static final long serialVersionUID = 1L;

	private final String jdbcToString;

	public TimestampImpl(Date date, String jdbcToString) {
		super(date);
		this.jdbcToString = jdbcToString;
	}

	@Override
	public String castToString(TimeZone tz) {
		return jdbcToString;
	}

	@Override
	public String castToString() {
		return jdbcToString;
	}

	@Override
	public String castToString(String defaultValue) {
		return jdbcToString;
	}
}
