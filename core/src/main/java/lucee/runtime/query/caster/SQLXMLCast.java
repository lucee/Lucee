package lucee.runtime.query.caster;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;

public class SQLXMLCast implements Cast {

	@Override
	public Object toCFType(TimeZone tz, ResultSet rst, int columnIndex) throws SQLException {

		try {
			return rst.getSQLXML(columnIndex).getString();
		}
		catch (SQLException se) {
			throw se;
		}
		catch (Throwable t) {// must be a throwable because it throws for example a AbstractMethodError with JDTS, but could also
			// be other
			ExceptionUtil.rethrowIfNecessary(t);
			DatabaseMetaData md = rst.getStatement().getConnection().getMetaData();
			if (md.getJDBCMajorVersion() < 4) throw new PageRuntimeException(
					new DatabaseException("The data type [SQLXML] is not supported with this datasource.", "The datasource JDBC driver compatibility is up to the versions ["
							+ md.getJDBCMajorVersion() + "." + md.getJDBCMinorVersion() + "], but this feature needs at least [4.0]", null, null));
			throw new PageRuntimeException(Caster.toPageException(t));
		}
	}

}