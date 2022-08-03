package lucee.commons.io.log.log4j2.appender;

import java.sql.Types;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceUtil;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.SQL;
import lucee.runtime.db.SQLImpl;
import lucee.runtime.db.SQLItem;
import lucee.runtime.db.SQLItemImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.dt.DateTimeImpl;

public class DatasourceAppender extends AbstractAppender {

	// private DatasourceManagerImpl manager;
	private DataSource datasource;
	private final String datasourceName;
	private final String username;
	private final String password;
	private final String tableName;
	private final Config config;
	private String custom;
	private Appender fallback;
	private boolean isInit;
	private Object token = new Object();

	public DatasourceAppender(Config config, Appender fallback, String name, Filter filter, String datasource, String username, String password, String tableName, String custom)
			throws PageException {
		super(name, filter, null);
		this.datasourceName = datasource;
		this.username = username;
		this.password = password;
		this.config = config;
		this.tableName = tableName;
		this.custom = custom;
		this.fallback = fallback;
	}

	public String getTableName() {
		return tableName;
	}

	private DatasourceConnPool pool;

	@Override
	public void append(LogEvent event) {
		if (!isInit) {
			init();
			if (!isInit) {
				fallback.append(event);
				return;
			}
		}

		DatasourceConnection conn = null;
		try {

			// ththreadId=
			String threadId = event.getThreadName();
			if (threadId.length() > 64) threadId = threadId.substring(0, 63);

			// split application->message
			String application;
			String msg = Caster.toString(event.getMessage(), null);
			int index = msg.indexOf("->");
			if (index > -1) {
				application = msg.substring(0, index);
				if (application.length() > 64) application = application.substring(0, 63);
				msg = msg.substring(index + 2);
			}
			else application = "";

			if (msg.length() > 512) msg = msg.substring(0, 508) + "...";

			// get Exception
			String exception = "";
			Throwable t = event.getThrown();
			if (t != null) {
				String em = ExceptionUtil.getMessage(t);
				if (StringUtil.isEmpty(msg)) msg = em;
				else msg += ";" + em;

				exception = ExceptionUtil.getStacktrace(t, false);
				if (exception == null) exception = "";
				else if (exception.length() > 2048) exception = exception.substring(0, 2044) + "...";
			}

			// id
			String id = "";
			Config c = ThreadLocalPageContext.getConfig();
			if (c != null) {
				if (c instanceof ConfigWeb) id = ((ConfigWeb) c).getLabel();

				else id = c.getIdentification().getId();
			}

			conn = getConnection();
			PageContext optionalPC = ThreadLocalPageContext.get();

			SQLImpl sql = new SQLImpl("INSERT INTO " + tableName + " (id,name,severity,threadid,time,application,message,exception,custom) values(?,?,?,?,?,?,?,?,?)",
					new SQLItem[] {

							new SQLItemImpl(id, Types.VARCHAR)

							, new SQLItemImpl(getName(), Types.VARCHAR)

							, new SQLItemImpl(event.getLevel().name(), Types.VARCHAR)

							, new SQLItemImpl(threadId, Types.VARCHAR)

							, new SQLItemImpl(new DateTimeImpl(event.getTimeMillis(), false), Types.TIMESTAMP)

							, new SQLItemImpl(application, Types.VARCHAR)

							, new SQLItemImpl(msg, Types.VARCHAR)

							, new SQLItemImpl(exception, Types.VARCHAR)

							, new SQLItemImpl(custom == null ? "" : custom, Types.VARCHAR)

					});
			new QueryImpl(optionalPC, conn, sql, -1, -1, null, "query");
		}
		catch (PageException pe) {
			LogUtil.logGlobal(config, "log-loading", pe);
		}
		finally {
			try {
				relConnection(conn);
			}
			catch (PageException pee) {
				LogUtil.logGlobal(config, "log-loading", pee);
			}
		}
	}

	private void init() {
		synchronized (token) {
			if (!isInit) {
				DatasourceConnection conn = null;
				PageContext optionalPC = ThreadLocalPageContext.get();
				SQLImpl sql = new SQLImpl("select 1 from " + tableName + " where 1=0");
				try {
					conn = getConnection();
					try {
						new QueryImpl(optionalPC, conn, sql, -1, -1, null, "query");
						isInit = true;
					}
					catch (PageException pe) {
						// SystemOut.printDate(pe);
						try {
							new QueryImpl(optionalPC, conn, createSQL(conn), -1, -1, null, "query");
							isInit = true;
						}
						catch (Exception e2) {
							// SystemOut.printDate(e2);
							throw pe;
						}
					}
					finally {
						relConnection(conn);
					}
				}
				catch (PageException pe) {
					LogUtil.logGlobal(config, "log-loading", pe);
					isInit = false;
				}
			}
		}
	}

	private SQL createSQL(DatasourceConnection dc) {
		StringBuilder sb = new StringBuilder("CREATE TABLE ");
		if (DataSourceUtil.isMSSQL(dc)) sb.append("dbo.");
		sb.append(tableName).append(" ( ");

		if (DataSourceUtil.isMSSQL(dc)) sb.append("pid INT PRIMARY KEY IDENTITY (1, 1), ");
		else if (DataSourceUtil.isMySQL(dc)) sb.append("pid INT AUTO_INCREMENT PRIMARY KEY, ");
		else if (DataSourceUtil.isHSQLDB(dc)) sb.append("pid INTEGER IDENTITY PRIMARY KEY, ");
		else if (DataSourceUtil.isPostgres(dc)) sb.append("id SERIAL PRIMARY KEY, ");

		sb.append("id varchar(32) NOT NULL, ");
		sb.append("name varchar(128) NOT NULL, ");
		sb.append("severity varchar(16) NOT NULL, ");
		sb.append("threadid varchar(64) NOT NULL, ");
		sb.append("time datetime NOT NULL, ");
		sb.append("application varchar(64) NOT NULL, ");
		sb.append("message varchar(512) NOT NULL, ");
		sb.append("exception varchar(2048) NOT NULL, ");
		sb.append("custom varchar(2048) NOT NULL ");
		sb.append(");");
		return new SQLImpl(sb.toString());

	}

	private DatasourceConnPool pool() throws PageException {
		if (pool == null) {
			if (datasource == null) datasource = config.getDataSource(datasourceName);
			this.pool = ((ConfigPro) config).getDatasourceConnectionPool(datasource, username, password);
		}
		return pool;
	}

	private DatasourceConnection getConnection() throws PageException {
		return pool().borrowObject();
	}

	protected void relConnection(DatasourceConnection conn) throws PageException {
		pool().returnObject(conn);
	}
}
