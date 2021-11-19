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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.TimeZone;

public class OtherCast implements Cast {

	private final int type;

	public OtherCast(int type) {
		this.type = type;
	}

	@Override
	public Object toCFType(TimeZone tz, ResultSet rst, int columnIndex) throws SQLException {
		if (type != Types.SMALLINT) return rst.getObject(columnIndex);

		try {
			return rst.getObject(columnIndex);
		}
		// workaround for MSSQL Driver, in some situation getObject throws a cast exception using getString
		// avoids this
		catch (SQLException e) {
			try {
				return rst.getString(columnIndex);
			}
			catch (SQLException e2) {
				throw e;
			}
		}
	}

}