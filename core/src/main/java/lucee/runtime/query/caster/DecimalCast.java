package lucee.runtime.query.caster;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

/**
 * Cast implementation for DECIMAL, NUMERIC types.
 * Calls ResultSet.getBigDecimal() directly instead of getObject() to avoid overhead.
 */
public final class DecimalCast implements Cast {

	@Override
	public Object toCFType(TimeZone tz, ResultSet rst, int columnIndex) throws SQLException, IOException {
		return rst.getBigDecimal( columnIndex );
	}
}
