package lucee.runtime.functions.query;

import java.sql.Connection;

import lucee.runtime.PageContext;
import lucee.runtime.db.DatasourceManagerImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;

public final class GetTransactionIsolation extends BIF {

	private static final long serialVersionUID = -1L;

	public static String call(PageContext pc) {
		return call(pc, null);
	}

	public static String call(PageContext pc, String datasource) {
		DatasourceManagerImpl manager = (DatasourceManagerImpl) pc.getDataSourceManager();
		int isolation = manager.getIsolation();

		// if no explicit isolation set and a datasource was provided, return the datasource default
		if (isolation == Connection.TRANSACTION_NONE && datasource != null) {
			try {
				int defaultIsolation = pc.getDataSource(datasource).getDefaultTransactionIsolation();
				return toIsolationString(defaultIsolation);
			}
			catch (PageException e) {
				return "";
			}
		}

		return toIsolationString(isolation);
	}

	private static String toIsolationString(int isolation) {
		switch (isolation) {
			case Connection.TRANSACTION_READ_UNCOMMITTED:
				return "read_uncommitted";
			case Connection.TRANSACTION_READ_COMMITTED:
				return "read_committed";
			case Connection.TRANSACTION_REPEATABLE_READ:
				return "repeatable_read";
			case Connection.TRANSACTION_SERIALIZABLE:
				return "serializable";
			default:
				return "";
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		else if (args.length == 1) return call(pc, args[0] == null ? null : args[0].toString());
		else throw new FunctionException(pc, "GetTransactionIsolation", 0, 1, args.length);
	}

}
