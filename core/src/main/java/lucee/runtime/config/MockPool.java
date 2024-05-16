package lucee.runtime.config;

import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
// only exists for the Hibernate extension
import lucee.runtime.exp.PageException;

public class MockPool {

	public DatasourceConnection getDatasourceConnection(Config config, DataSource ds, String user, String pass) throws PageException {
		return ((ConfigPro) config).getDatasourceConnectionPool(ds, user, pass).borrowObject();
	}

}
// Method getDatasourceConnection = pool.getClass().getMethod("getDatasourceConnection",
// GET_CONN_ARGS);
// return (DatasourceConnection) getDatasourceConnection.invoke(pool, new Object[] { config, ds,
// null, null });