package lucee.runtime.query.caster;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

/**
 * Cast implementation for DOUBLE, FLOAT, REAL types.
 * Calls ResultSet.getDouble() directly instead of getObject() to avoid overhead.
 */
public final class DoubleCast implements Cast {

	@Override
	public Object toCFType(TimeZone tz, ResultSet rst, int columnIndex) throws SQLException, IOException {
		double value = rst.getDouble( columnIndex );
		// ResultSet.getDouble() returns 0.0 for NULL values, need to check wasNull()
		return rst.wasNull() ? null : Double.valueOf( value );
	}
}
