package lucee.runtime.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;

public class DatasourceConnectionFactory extends BasePooledObjectFactory<DatasourceConnection> {
	private static final ConcurrentHashMap<String, String> tokens = new ConcurrentHashMap<String, String>();

	private DatasourceConnPool pool;
	private final Config config;
	private final DataSource datasource;
	private final String username;
	private final String password;
	private final String logName;

	public DatasourceConnectionFactory(Config config, DataSource datasource, String username, String password, String logName) {
		this.config = config;
		this.datasource = datasource;

		if (StringUtil.isEmpty(username)) {
			this.username = datasource.getUsername();
			this.password = datasource.getPassword();
		}
		else {
			this.username = username;
			this.password = (password == null) ? "" : password;
		}

		this.logName = StringUtil.isEmpty(logName) ? null : logName;
		// TODO use socketTimeout
	}

	public void setPool(DatasourceConnPool pool) {
		this.pool = pool;
	}

	@Override
	public DatasourceConnection create() throws IOException {
		LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "create datasource connectrion:" + datasource.getName());

		Connection conn = null;
		try {
			conn = ((DataSourcePro) datasource).getConnection(config, username, password);
		}
		catch (SQLException e) {
			throw new IOException(e);
		}
		catch (Exception e) {
			throw ExceptionUtil.toIOException(e);
		}
		return new DatasourceConnectionImpl(pool, conn, (DataSourcePro) datasource, username, password);
	}

	/**
	 * Use the default PooledObject implementation.
	 */
	@Override
	public PooledObject<DatasourceConnection> wrap(DatasourceConnection dc) {
		return new DefaultPooledObject<DatasourceConnection>(dc);
	}

	@Override
	public boolean validateObject(PooledObject<DatasourceConnection> p) {
		LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "validate datasource connectrion:" + datasource.getName());
		DatasourceConnection dc = p.getObject();

		DataSourcePro dsp = (DataSourcePro) dc.getDatasource();

		if (dc.isTimeout()) {
			LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "reached idle timeout for datasource connectrion:" + datasource.getName());
			return false;
		}

		if (dc.isLifecycleTimeout()) {
			LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "reached live timeout for datasource connectrion:" + datasource.getName());
			return false;
		}

		try {
			if (dc.getConnection().isClosed()) return false;
		}
		catch (Exception e) {
			LogUtil.log(config, logName, "connection", e, Log.LEVEL_ERROR);
			return false;
		}

		try {
			if (dc.getDatasource().validate() && !DataSourceUtil.isValid(dc, 1000)) return false;
		}
		catch (Exception e) {
			LogUtil.log(config, logName, "connection", e, Log.LEVEL_ERROR);
		}
		return true;
	}

	@Override
	public void activateObject(PooledObject<DatasourceConnection> p) throws PageException {
		LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "activate datasource connectrion:" + datasource.getName());
		((DatasourceConnectionImpl) p.getObject()).using();
	}

	@Override
	public void destroyObject(PooledObject<DatasourceConnection> p) throws PageException {
		LogUtil.log(config, Log.LEVEL_DEBUG, logName, "connection", "destroy datasource connectrion:" + datasource.getName());
		DatasourceConnection dc = null;
		try {
			dc = p.getObject();
			dc.close();
		}
		catch (SQLException e) {
			throw new DatabaseException(e, dc);
		}
	}

	public static String createId(DataSource datasource, String user, String pass) {
		String str = new StringBuilder().append(datasource.id()).append("::").append(user).append(":").append(pass).toString();
		String lock = tokens.putIfAbsent(str, str);
		if (lock == null) {
			lock = str;
		}
		return lock;
	}

	public DataSource getDatasource() {
		return datasource;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
