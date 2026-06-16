package lucee.runtime.db;

import java.sql.SQLException;

import org.osgi.framework.BundleException;

import lucee.commons.date.TimeZoneUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigFactoryImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.Prop;
import lucee.runtime.config.PropFactory;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.library.ClassDefinitionImpl;

public class DataSourceFactory implements PropFactory<DataSource> {

	private static DataSourceFactory instance;

	public static DataSourceFactory getInstance() {
		if (instance == null) {
			instance = new DataSourceFactory();
		}
		return instance;
	}

	@Override
	public DataSource evaluate(Config c, String name, Object val, short source) throws PageException {
		ConfigPro config = (ConfigPro) c;
		try {

			// When set to true, makes JDBC use a representation for DATE data that
			// is compatible with the Oracle8i database.
			System.setProperty("oracle.jdbc.V8Compatible", "true");

			// Databases

			// if(hasAccess) {
			JDBCDriver jdbc;
			ClassDefinition cd;
			String id;
			Struct dataSource = Caster.toStruct(val);

			if (!hasDataSourceDefinition(dataSource)) {
				return null;
			}

			try {
				// do we have an id?
				jdbc = config.getJDBCDriverById(ConfigFactoryImpl.getAttr(config, dataSource, "id"), null);
				if (jdbc != null && jdbc.cd != null) {
					cd = jdbc.cd;
				}
				else {
					cd = ConfigFactoryImpl.getClassDefinition(config, dataSource, "", config.getIdentification());
				}

				// we have no class
				if (!cd.hasClass()) {
					String type = ConfigFactoryImpl.getAttr(config, dataSource, "type");
					if (StringUtil.isEmpty(type, true)) type = ConfigFactoryImpl.getAttr(config, dataSource, "dbdriver");
					jdbc = config.getJDBCDriverById(type, null);
					if (jdbc != null && jdbc.cd != null) {
						cd = jdbc.cd;
					}
				}
				// we only have a class
				else if (!cd.isBundle() && !((ClassDefinitionImpl) cd).isMaven()) {
					jdbc = config.getJDBCDriverByClassName(cd.getClassName(), null);
					if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) cd = jdbc.cd;
				}

				// still no bundle!
				if (!cd.isBundle() && !((ClassDefinitionImpl) cd).isMaven()) cd = patchJDBCClass(config, cd);
				int idle = Caster.toIntValue(ConfigFactoryImpl.getAttr(config, dataSource, "idleTimeout"), -1);
				if (idle == -1) idle = Caster.toIntValue(ConfigFactoryImpl.getAttr(config, dataSource, "connectionTimeout"), -1);
				int defLive = 15;
				if (idle > 0) defLive = idle * 5;// for backward compatibility

				String dsn = ConfigFactoryImpl.getAttr(config, dataSource, "connectionString");
				if (StringUtil.isEmpty(dsn, true)) dsn = ConfigFactoryImpl.getAttr(config, dataSource, "dsn");
				if (StringUtil.isEmpty(dsn, true)) dsn = ConfigFactoryImpl.getAttr(config, dataSource, "connStr");
				if (StringUtil.isEmpty(dsn, true)) dsn = ConfigFactoryImpl.getAttr(config, dataSource, "url");
				if (StringUtil.isEmpty(dsn, true)) {
					if (jdbc == null && cd.hasClass()) {
						jdbc = config.getJDBCDriverByClassName(cd.getClassName(), null);
					}
					if (jdbc != null) {
						dsn = jdbc.connStr;
					}

				}
				String bundleName = ConfigFactoryImpl.getAttr(config, dataSource, "bundleName");
				String bundleVersion = ConfigFactoryImpl.getAttr(config, dataSource, "bundleVersion");

				return createDatasource(config, name, cd, ConfigFactoryImpl.getAttr(config, dataSource, "host"), ConfigFactoryImpl.getAttr(config, dataSource, "database"),
						Caster.toIntValue(ConfigFactoryImpl.getAttr(config, dataSource, "port"), -1), dsn, bundleName, bundleVersion,
						ConfigFactoryImpl.getAttr(config, dataSource, "username"), ConfigUtil.decrypt(ConfigFactoryImpl.getAttr(config, dataSource, "password")), null,
						Caster.toIntValue(ConfigFactoryImpl.getAttr(config, dataSource, "connectionLimit"), ConfigFactoryImpl.DEFAULT_MAX_CONNECTION), idle,
						Caster.toIntValue(ConfigFactoryImpl.getAttr(config, dataSource, "liveTimeout"), defLive),
						Caster.toIntValue(ConfigFactoryImpl.getAttr(config, dataSource, "minIdle"), 0),
						Caster.toIntValue(ConfigFactoryImpl.getAttr(config, dataSource, "maxIdle"), 0),
						Caster.toIntValue(ConfigFactoryImpl.getAttr(config, dataSource, "maxTotal"), 0),
						Caster.toLongValue(ConfigFactoryImpl.getAttr(config, dataSource, "metaCacheTimeout"), 60000),
						ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, dataSource, "blob"), true),
						ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, dataSource, "clob"), true),
						Caster.toIntValue(ConfigFactoryImpl.getAttr(config, dataSource, "allow"), DataSource.ALLOW_ALL),
						ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, dataSource, "validate"), false),
						ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, dataSource, "storage"), false),
						ConfigFactoryImpl.getAttr(config, dataSource, "timezone"), ConfigUtil.getAsStruct(config, dataSource, true, "custom"),
						ConfigFactoryImpl.getAttr(config, dataSource, "dbdriver"), ParamSyntaxImpl.toParamSyntax(dataSource, ParamSyntaxImpl.DEFAULT),
						ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, dataSource, "literalTimestampWithTSOffset"), false),
						ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, dataSource, "alwaysSetTimeout"), false),
						ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, dataSource, "requestExclusive"), false),
						ConfigFactoryImpl.toBoolean(ConfigFactoryImpl.getAttr(config, dataSource, "alwaysResetConnections"), false)

				);
			}
			catch (Throwable th) {
				ExceptionUtil.rethrowIfNecessary(th);
				throw Caster.toPageException(th);
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			ConfigFactoryImpl.log(config, t);
			throw Caster.toPageException(t);
		}
	}

	private static boolean hasDataSourceDefinition(Struct dataSource) {
		if (dataSource.containsKey(KeyConstants._database)) return true;
		if (dataSource.containsKey(KeyConstants._connectionString)) return true;
		if (dataSource.containsKey(KeyConstants._dsn)) return true;
		if (dataSource.containsKey(KeyConstants._url)) return true;
		if (dataSource.containsKey(KeyConstants._class)) return true;
		if (dataSource.containsKey(KeyConstants._type)) return true;
		if (dataSource.containsKey(KeyImpl.init("dbdriver"))) return true;
		if (dataSource.containsKey(KeyConstants._id)) return true;
		return false;
	}

	private static DataSourceImpl createDatasource(ConfigPro config, String datasourceName, ClassDefinition cd, String server, String databasename, int port, String dsn,
			String bundleName, String bundleVersion, String user, String pass, TagListener listener, int connectionLimit, int idleTimeout, int liveTimeout, int minIdle,
			int maxIdle, int maxTotal, long metaCacheTimeout, boolean blob, boolean clob, int allow, boolean validate, boolean storage, String timezone, Struct custom,
			String dbdriver, ParamSyntax ps, boolean literalTimestampWithTSOffset, boolean alwaysSetTimeout, boolean requestExclusive, boolean alwaysResetConnections)
			throws BundleException, ClassException, SQLException {

		return new DataSourceImpl(datasourceName, cd, server, dsn, bundleName, bundleVersion, databasename, port, user, pass, listener, connectionLimit, idleTimeout, liveTimeout,
				minIdle, maxIdle, maxTotal, metaCacheTimeout, blob, clob, allow, custom, false, validate, storage,
				StringUtil.isEmpty(timezone, true) ? null : TimeZoneUtil.toTimeZone(timezone, null), dbdriver, ps, literalTimestampWithTSOffset, alwaysSetTimeout, requestExclusive,
				alwaysResetConnections, ThreadLocalPageContext.getLog(config, "application"));

	}

	private static ClassDefinition patchJDBCClass(ConfigPro config, ClassDefinition cd) {
		// PATCH for MySQL driver that did change the className within the same extension, JDBC extension
		// expect that the className does not change.
		if ("org.gjt.mm.mysql.Driver".equals(cd.getClassName()) || "com.mysql.jdbc.Driver".equals(cd.getClassName()) || "com.mysql.cj.jdbc.Driver".equals(cd.getClassName())) {
			JDBCDriver jdbc = config.getJDBCDriverById("mysql", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("com.mysql.cj.jdbc.Driver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("com.mysql.jdbc.Driver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("org.gjt.mm.mysql.Driver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			ClassDefinitionImpl tmp = new ClassDefinitionImpl("com.mysql.cj.jdbc.Driver", "com.mysql.cj", null, config.getIdentification());
			if (tmp.getClazz(null) != null) return tmp;

			tmp = new ClassDefinitionImpl("com.mysql.jdbc.Driver", "com.mysql.jdbc", null, config.getIdentification());
			if (tmp.getClazz(null) != null) return tmp;
		}
		if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cd.getClassName())) {
			JDBCDriver jdbc = config.getJDBCDriverById("mssql", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			ClassDefinitionImpl tmp = new ClassDefinitionImpl("com.microsoft.sqlserver.jdbc.SQLServerDriver", cd.getName(), cd.getVersionAsString(), config.getIdentification());
			if (tmp.getClazz(null) != null) return tmp;
		}

		return cd;
	}

	@Override
	public Struct schema(Prop<DataSource> prop) {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._type, "object");

		Struct properties = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL(KeyConstants._properties, properties);

		// 1. Driver Identification (Standard ClassDefinition + 'type'/'dbdriver')
		PropFactory.appendClassDefinitionProps(properties);
		properties.setEL(KeyConstants._type, PropFactory.createSimple("string", "The JDBC driver type ID (e.g., 'mysql', 'mssql')."));
		properties.setEL("dbdriver", PropFactory.createSimple("string", "The database driver name."));

		// 2. Connection Details (Handles aliases found in evaluate)
		properties.setEL(KeyConstants._host, PropFactory.createSimple("string", "The server hostname or IP address."));
		properties.setEL(KeyConstants._port, PropFactory.createSimple("integer", "The database port number."));
		properties.setEL(KeyConstants._database, PropFactory.createSimple("string", "The database name."));

		Struct connStr = PropFactory.createSimple("string", "The JDBC connection string.");
		properties.setEL(KeyConstants._connectionString, connStr);
		properties.setEL(KeyConstants._dsn, connStr); // Alias
		properties.setEL(KeyConstants._url, connStr); // Alias

		// 3. Authentication
		properties.setEL(KeyConstants._username, PropFactory.createSimple("string", "The database user."));
		properties.setEL(KeyConstants._password, PropFactory.createSimple("string", "The database password (can be encrypted)."));

		// 4. Pooling & Timeouts
		properties.setEL("connectionLimit", PropFactory.createSimple("integer", "Max number of connections."));
		properties.setEL("connectionTimeout", PropFactory.createSimple("integer", "Alias for idleTimeout."));
		properties.setEL("idleTimeout", PropFactory.createSimple("integer", "Time in minutes before an idle connection is closed."));
		properties.setEL("liveTimeout", PropFactory.createSimple("integer", "Max life of a connection in minutes."));
		properties.setEL("minIdle", PropFactory.createSimple("integer", "Minimum number of idle connections."));
		properties.setEL("maxIdle", PropFactory.createSimple("integer", "Maximum number of idle connections."));
		properties.setEL("maxTotal", PropFactory.createSimple("integer", "Maximum total connections."));

		// 5. Lucee Flags & Compatibility
		properties.setEL(KeyConstants._storage, PropFactory.createSimple("boolean", "Use this datasource for client/session storage."));
		properties.setEL(KeyConstants._validate, PropFactory.createSimple("boolean", "Validate connection before use."));
		properties.setEL(KeyConstants._blob, PropFactory.createSimple("boolean", "Support BLOB data. Default is true."));
		properties.setEL(KeyConstants._clob, PropFactory.createSimple("boolean", "Support CLOB data. Default is true."));
		properties.setEL(KeyConstants._timezone, PropFactory.createSimple("string", "The timezone for the connection."));
		properties.setEL("alwaysSetTimeout", PropFactory.createSimple("boolean", "Always set a query timeout."));

		// 6. Custom/Driver Specific
		Struct custom = new StructImpl(Struct.TYPE_LINKED);
		custom.setEL(KeyConstants._type, "object");
		custom.setEL("additionalProperties", true);
		properties.setEL(KeyConstants._custom, custom);

		return sct;
	}

	@Override
	public Object resolvedValue(DataSource value) {
		return value;
	}

}
