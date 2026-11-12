/**
 * LDEV-6908 datasource definition matrix for the installed JDBC extensions.
 * Builds every supported way to define a datasource and checks that it resolves to a driver
 * that can be loaded (OSGi bundle or Maven coordinates) and, when a database is available, that it connects.
 * No TestBox dependency, so it can also be run from a plain page.
 */
component {

	// connection details per JDBC driver id, an empty struct means no database is available
	variables.counter = 0;

	public function init( struct services = {}, string tempDir = getTempDirectory(), string adminPassword = "" ) {
		variables.services = arguments.services;
		variables.tempDir = arguments.tempDir;
		variables.adminPassword = arguments.adminPassword;
		variables.created = [];
		return this;
	}

	/** all JDBC drivers registered by installed extensions: [{extension, extensionVersion, id, class, bundleName, bundleVersion, maven, connectionString}] */
	public array function getDrivers() {
		var result = [];
		var exts = getPageContext().getConfig().getAllRHExtensions();
		if ( isNull( exts ) ) return result;
		var it = exts.iterator();
		while ( it.hasNext() ) {
			var ext = it.next();
			var md = ext.getMetadata();
			var jdbcs = md.getJdbcs();
			if ( isNull( jdbcs ) ) continue;
			for ( var map in jdbcs ) {
				var m = {};
				for ( var k in [ "id", "class", "classname", "bundleName", "bundleVersion", "maven", "connectionString" ] ) {
					var v = map.get( k );
					m[ k ] = isNull( v ) ? "" : toString( v );
				}
				var d = {
					extension: md.getName(),
					extensionVersion: ext.getVersion(),
					id: m.id,
					class: len( m.class ) ? m.class : m.classname,
					bundleName: m.bundleName,
					bundleVersion: m.bundleVersion,
					maven: m.maven,
					connectionString: m.connectionString
				};
				if ( len( d.class ) ) arrayAppend( result, d );
			}
		}
		return result;
	}

	/** connection details for a driver: {available, connectionString, host, port, database, username, password} */
	public struct function getConnection( required struct driver ) {
		var id = arguments.driver.id;
		var dir = variables.tempDir & "ldev6908/" & id & "/";
		if ( listFindNoCase( "h2,hsqldb,duckdb", id ) && !directoryExists( dir ) ) directoryCreate( dir, true );
		switch ( id ) {
			case "h2":
				return { available: true, connectionString: "jdbc:h2:" & dir & "db{n};MODE=MySQL", path: dir, database: "db", host: "", port: "", username: "", password: "" };
			case "hsqldb":
				return { available: true, connectionString: "jdbc:hsqldb:file:" & dir & "db{n}", path: dir, database: "db", host: "", port: "", username: "SA", password: "" };
			case "duckdb":
				return { available: true, connectionString: "jdbc:duckdb:" & dir & "db{n}.duckdb", path: dir, database: "db.duckdb", host: "", port: "", username: "", password: "" };
		}
		var svcName = { postgresql: "postgres", mysql: "mysql", mssql: "mssql", jtds: "mssql", oracle: "oracle" }[ id ] ?: "";
		var svc = len( svcName ) ? ( variables.services[ svcName ] ?: {} ) : {};
		if ( structCount( svc ) == 0 ) return { available: false };
		var c = { available: true, host: svc.server, port: svc.port, database: svc.database, username: svc.username, password: svc.password, path: "" };
		switch ( id ) {
			case "postgresql": c.connectionString = "jdbc:postgresql://#c.host#:#c.port#/#c.database#"; break;
			case "mysql": c.connectionString = "jdbc:mysql://#c.host#:#c.port#/#c.database#?useSSL=false&allowPublicKeyRetrieval=true"; break;
			case "mssql": c.connectionString = "jdbc:sqlserver://#c.host#:#c.port#;databaseName=#c.database#;trustServerCertificate=true"; break;
			case "jtds": c.connectionString = "jdbc:jtds:sqlserver://#c.host#:#c.port#/#c.database#"; break;
			case "oracle": c.connectionString = "jdbc:oracle:thin:@#c.host#:#c.port#/#c.database#"; break;
		}
		return c;
	}

	/** older bundle versions that exist for a driver, used for the [bundleVersion old] style */
	public string function getOldBundleVersion( required struct driver ) {
		return { postgresql: "42.7.9", mysql: "9.6.0" }[ arguments.driver.id ] ?: "";
	}

	/**
	 * all definition styles for a driver: [{style, scope (app|server|admin), definition, supported}]
	 * supported=false marks styles Lucee does not support for that scope (reported, not expected to work)
	 */
	public array function getCases( required struct driver ) {
		var d = arguments.driver;
		var c = getConnection( d );
		var connStr = c.connectionString ?: placeholderConnectionString( d );
		var login = { username: c.username ?: "", password: c.password ?: "" };
		var hostStyle = { host: c.host ?: "localhost", port: c.port ?: "", database: c.database ?: "ldev6908" };
		if ( len( c.path ?: "" ) ) hostStyle.path = c.path;
		var cases = [];
		var add = function( style, def, scopes = "app,server", supported = true ) {
			for ( var scope in listToArray( scopes ) ) {
				var def2 = uniqueFiles( duplicate( def ) );
				structAppend( def2, login, false );
				// server datasources in .CFConfig.json need a [database] key to be loaded
				if ( scope == "server" && !structKeyExists( def2, "database" ) ) def2.database = hostStyle.database;
				arrayAppend( cases, { style: style, scope: scope, definition: def2, supported: supported } );
			}
		};
		add( "class only", { class: d.class, connectionString: connStr } );
		add( "class + bundleName", { class: d.class, bundleName: d.bundleName, connectionString: connStr } );
		if ( len( d.bundleVersion ) ) add( "class + bundleName + bundleVersion", { class: d.class, bundleName: d.bundleName, bundleVersion: d.bundleVersion, connectionString: connStr } );
		var old = getOldBundleVersion( d );
		if ( len( old ) ) add( "class + bundleName + bundleVersion (old #old#)", { class: d.class, bundleName: d.bundleName, bundleVersion: old, connectionString: connStr } );
		if ( len( d.maven ) ) add( "class + maven", { class: d.class, maven: d.maven, connectionString: connStr } );
		// type: the app definition only knows a fixed list of types, server config resolves any registered driver id
		var appTypes = "h2,mssql,mysql,oracle,postgresql";
		if ( len( d.id ) ) {
			var typeDef = duplicate( hostStyle );
			typeDef.type = d.id;
			add( "type", typeDef, "server" );
			add( "type", typeDef, "app", listFindNoCase( appTypes, d.id ) > 0 );
			add( "id + connectionString", { id: d.id, connectionString: connStr }, "server" );
			add( "id + connectionString", { id: d.id, connectionString: connStr }, "app", false );
		}
		add( "connectionString only", { connectionString: connStr }, "app,server", false );
		var hp = duplicate( hostStyle );
		hp.class = d.class;
		add( "class + host/port/database", hp, "server" );
		add( "class + host/port/database", hp, "app", false );
		// admin created (cfadmin action=updateDatasource), what the Lucee admin does
		arrayAppend( cases, { style: "admin: classname + dsn", scope: "admin", definition: { classname: d.class, dsn: uniqueFiles( { s: connStr } ).s, database: hostStyle.database, host: hostStyle.host, port: hostStyle.port, dbusername: login.username, dbpassword: login.password, verify: c.available }, supported: true } );
		arrayAppend( cases, { style: "admin: classname + dsn + bundle", scope: "admin", definition: { classname: d.class, dsn: uniqueFiles( { s: connStr } ).s, bundleName: d.bundleName, bundleVersion: d.bundleVersion, database: hostStyle.database, host: hostStyle.host, port: hostStyle.port, dbusername: login.username, dbpassword: login.password, verify: c.available }, supported: len( d.bundleName ) > 0 } );
		return cases;
	}

	/**
	 * should a connection be expected for that case?
	 * - [class + bundleName] without version and older bundle versions depend on what the update provider offers
	 * - file based drivers (h2, hsqldb, duckdb) need {path}/{mode} in the connection string template, the host/port/database and type styles do not fill them
	 */
	public boolean function expectConnection( required struct driver, required struct testCase ) {
		if ( !arguments.testCase.supported ) return false;
		if ( !getConnection( arguments.driver ).available ) return false;
		var style = arguments.testCase.style;
		if ( style == "class + bundleName" || find( "(old ", style ) ) return false;
		if ( listFindNoCase( "h2,hsqldb,duckdb", arguments.driver.id ) && ( style == "type" || style == "class + host/port/database" ) ) return false;
		return true;
	}

	/** resolves (and connects if a database is available) a case: {resolved, classDefinition, connected, error} */
	public struct function runCase( required struct driver, required struct testCase, boolean connect = true ) {
		var r = { resolved: false, classDefinition: "", connected: "", error: "" };
		var ds = "";
		try {
			if ( arguments.testCase.scope == "app" ) {
				ds = arguments.testCase.definition;
				var dso = createObject( "java", "lucee.runtime.listener.AppListenerUtil" ).toDataSource( getPageContext().getConfig(), "ldev6908app", duplicate( ds ), nullValue() );
				var cd = dso.getClassDefinition();
				r.classDefinition = describe( isNull( cd ) ? nullValue() : cd );
				r.resolved = hasLoadInfo( isNull( cd ) ? nullValue() : cd );
			}
			else {
				ds = createServerDatasource( arguments.testCase );
				var dso = getPageContext().getConfig().getDataSource( ds, nullValue() );
				if ( isNull( dso ) ) throw "datasource [#ds#] was not created";
				var cd = dso.getClassDefinition();
				r.classDefinition = describe( isNull( cd ) ? nullValue() : cd );
				r.resolved = hasLoadInfo( isNull( cd ) ? nullValue() : cd );
			}
		}
		catch ( any e ) {
			r.error = e.message;
			return r;
		}
		if ( arguments.connect && getConnection( arguments.driver ).available ) {
			try {
				dbinfo datasource=ds name="local.v" type="version";
				r.connected = true;
			}
			catch ( any e ) {
				r.connected = false;
				r.error = e.message;
			}
		}
		return r;
	}

	public void function cleanup() {
		for ( var name in variables.created ) {
			try {
				cfadmin( action="removeDatasource", type="server", password=variables.adminPassword, name=name );
			}
			catch ( any e ) {}
		}
		variables.created = [];
	}

	public boolean function hasLoadInfo( cd ) {
		if ( isNull( arguments.cd ) ) return false;
		if ( !arguments.cd.hasClass() ) return false;
		if ( arguments.cd.isBundle() ) return true;
		return isInstanceOf( arguments.cd, "lucee.transformer.library.ClassDefinitionImpl" ) && arguments.cd.isMaven();
	}

	// embedded databases get their own files per case, two driver instances must not share a database file
	private struct function uniqueFiles( required struct def ) {
		for ( var k in arguments.def ) {
			if ( isSimpleValue( arguments.def[ k ] ) && find( "{n}", arguments.def[ k ] ) ) {
				variables.counter++;
				arguments.def[ k ] = replace( arguments.def[ k ], "{n}", variables.counter, "all" );
			}
		}
		return arguments.def;
	}

	private string function describe( cd ) {
		if ( isNull( arguments.cd ) ) return "";
		var s = arguments.cd.toString();
		return isNull( s ) ? "" : s;
	}

	private string function createServerDatasource( required struct testCase ) {
		variables.counter++;
		var name = "ldev6908_#variables.counter#";
		if ( arguments.testCase.scope == "admin" ) {
			var a = duplicate( arguments.testCase.definition );
			a.action = "updateDatasource";
			a.type = "server";
			a.password = variables.adminPassword;
			a.name = name;
			a.newName = name;
			cfadmin( attributeCollection=a );
		}
		else {
			configImport( { "datasources": { "#name#": arguments.testCase.definition } }, "server", variables.adminPassword );
		}
		arrayAppend( variables.created, name );
		return name;
	}

	private string function placeholderConnectionString( required struct driver ) {
		var s = arguments.driver.connectionString;
		s = replaceNoCase( s, "{host}", "localhost", "all" );
		s = replaceNoCase( s, "{port}", "1", "all" );
		s = replaceNoCase( s, "{database}", "ldev6908", "all" );
		s = replaceNoCase( s, "{path}", variables.tempDir, "all" );
		s = replaceNoCase( s, "{mode}", "MySQL", "all" );
		return len( s ) ? s : "jdbc:unknown:ldev6908";
	}
}
