/**
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
import java.util.TimeZone;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;

public class OracleBlobCast implements Cast {

	private static final Object[] ZERO_ARGS = new Object[0];

	@Override
	public Object toCFType(TimeZone tz, ResultSet rst, int columnIndex) throws SQLException, IOException {
		Object o = rst.getObject(columnIndex);
		if (o == null) return null;

		// we do not have oracle.sql.CLOB in the core, so we need reflection for this
		try {
			return Caster.toBytes(Reflector.callMethod(o, "binaryStreamValue", ZERO_ARGS), null);
		}
		catch (PageException pe) {
			throw ExceptionUtil.toIOException(pe);
		}
	}
}