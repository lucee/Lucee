package lucee.runtime.query.caster;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

/**
 * Cast implementation for VARCHAR, CHAR, LONGVARCHAR and other string types.
 * Calls ResultSet.getString() directly instead of getObject() to avoid overhead.
 */
public final class VarcharCast implements Cast {

	@Override
	public Object toCFType(TimeZone tz, ResultSet rst, int columnIndex) throws SQLException, IOException {
		return rst.getString( columnIndex );
	}
}
