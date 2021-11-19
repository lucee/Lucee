package lucee.runtime.config;

import java.util.Collection;

import org.apache.commons.pool2.impl.BaseObjectPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceConnectionFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class DatasourceConnPool extends GenericObjectPool<DatasourceConnection> {

	/*
	 * public DatasourceConnPool(DatasourceConnectionFactory factory) { super(factory); this.factory =
	 * factory; // TODO Auto-generated constructor stub }
	 */

	public DatasourceConnPool(Config config, DataSource ds, String user, String pass, String logName, GenericObjectPoolConfig<DatasourceConnection> genericObjectPoolConfig) {
		super(new DatasourceConnectionFactory(config, ds, user, pass, logName), genericObjectPoolConfig);
		getFactory().setPool(this);
	}

	@Override
	public DatasourceConnection borrowObject() throws PageException {
		// TODO is there a better way to do this?
		try {
			return super.borrowObject();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public DatasourceConnectionFactory getFactory() {
		return (DatasourceConnectionFactory) super.getFactory();
	}

	public static Struct meta(Collection<DatasourceConnPool> pools) {
		// MUST do more data
		DataSource ds;
		Struct sct;
		Struct arr = new StructImpl();
		DatasourceConnectionFactory fac;
		for (DatasourceConnPool pool: pools) {
			fac = pool.getFactory();
			ds = fac.getDatasource();
			sct = new StructImpl();
			try {
				sct.setEL(KeyConstants._name, ds.getName());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			try {
				sct.setEL("connectionLimit", ds.getConnectionLimit());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			try {
				sct.setEL("connectionTimeout", ds.getConnectionTimeout());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			try {
				sct.setEL("connectionString", ds.getConnectionStringTranslated());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}

			try {
				int idle = pool.getNumIdle();
				int active = pool.getNumActive();
				int waiters = pool.getNumWaiters();
				sct.setEL("openConnections", active + idle);
				sct.setEL("activeConnections", active);
				sct.setEL("idleConnections", idle);
				sct.setEL("waitingForConn", waiters);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			try {
				sct.setEL(KeyConstants._database, ds.getDatabase());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			if (sct.size() > 0) arr.setEL(ds.getName(), sct);
		}
		return arr;
	}

	public static GenericObjectPoolConfig<DatasourceConnection> createPoolConfig(Boolean blockWhenExhausted, Boolean fairness, Boolean lifo, int minIdle, int maxIdle, int maxTotal,
			long maxWaitMillis, long minEvictableIdleTimeMillis, long timeBetweenEvictionRunsMillis, long softMinEvictableIdleTimeMillis, int numTestsPerEvictionRun,
			String evictionPolicyClassName) {
		GenericObjectPoolConfig<DatasourceConnection> config = new GenericObjectPoolConfig<DatasourceConnection>();

		config.setBlockWhenExhausted(blockWhenExhausted != null ? blockWhenExhausted.booleanValue() : false); // TODO make it config
		config.setFairness(fairness != null ? fairness.booleanValue() : true); // TODO make configurable
		config.setLifo(lifo != null ? lifo.booleanValue() : BaseObjectPoolConfig.DEFAULT_LIFO);
		config.setMinIdle(minIdle > 0 ? minIdle : GenericObjectPoolConfig.DEFAULT_MIN_IDLE);
		config.setMaxIdle(maxIdle > 0 ? maxIdle : GenericObjectPoolConfig.DEFAULT_MAX_IDLE);
		config.setMaxTotal(maxTotal > 0 ? maxTotal : GenericObjectPoolConfig.DEFAULT_MAX_TOTAL);
		config.setMaxWaitMillis(maxWaitMillis > 0 ? maxWaitMillis : GenericObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS);
		// TDOo merge with idleTimeout
		config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis > 0 ? minEvictableIdleTimeMillis : GenericObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
		// TODO this was done so far by the controler
		config.setTimeBetweenEvictionRunsMillis(
				timeBetweenEvictionRunsMillis > 0 ? timeBetweenEvictionRunsMillis : GenericObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
		// TDOD maybe make ii not congigurable
		config.setNumTestsPerEvictionRun(numTestsPerEvictionRun > 0 ? numTestsPerEvictionRun : 30);
		// config.setSoftMinEvictableIdleTimeMillis(
		// softMinEvictableIdleTimeMillis > 0 ? softMinEvictableIdleTimeMillis :
		// GenericObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS);

		if (!StringUtil.isEmpty(evictionPolicyClassName)) config.setEvictionPolicyClassName(evictionPolicyClassName);
		config.setTestOnCreate(false);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(false);
		config.setTestWhileIdle(true);
		config.setTestWhileIdle(true);

		return config;
	}

}
