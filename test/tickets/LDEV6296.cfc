component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	function beforeAll() {
		variables.uri = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), '\/' )#/LDEV6296";
		variables.adminPassword = request.ServerAdminPassword;
		variables.serverDsName  = "LDEV6296_server";
		variables.appDsName     = "LDEV6296_main";
		variables.miniAppDsName = "LDEV6296_app";

		removeServerDatasource( variables.serverDsName );

		// 1) Server-level datasource registered via cfadmin.
		registerServerDatasource( variables.serverDsName, "jdbc:hsqldb:mem:LDEV6296_server;shutdown=true" );

		// 2) Application-level datasource defined in this main test's app.
		application action="update" datasources={
			"#variables.appDsName#" : {
				  class            : "org.hsqldb.jdbcDriver"
				, bundleName       : "org.lucee.hsqldb"
				, connectionString : "jdbc:hsqldb:mem:LDEV6296_main;shutdown=true"
				, username         : "sa"
				, password         : ""
			}
		};

		// Three-phase pool exercise per ds: bare creds, matching creds, different creds.
		exerciseDatasource( variables.serverDsName );
		exerciseDatasource( variables.appDsName );

		// 3) Mini-app exercises a separate Application.cfc-scoped datasource.
		_internalRequest( template = uri & "/probe.cfm" );
	}

	function afterAll() {
		removeServerDatasource( variables.serverDsName );
	}

	function run( testResults, testBox ) {

		describe( "LDEV6296 cfquery matching credentials should not create a duplicate pool", function() {

			it( title="server-level datasource: matching-cred cfquery collapses onto the server-cred pool", body=function() {
				assertPoolCount( variables.serverDsName );
			});

			it( title="application-level datasource: matching-cred cfquery collapses onto the server-cred pool", body=function() {
				assertPoolCount( variables.appDsName );
			});

			it( title="mini-app application datasource: matching-cred cfquery collapses onto the server-cred pool", body=function() {
				assertPoolCount( variables.miniAppDsName );
			});

		});

	}

	private void function assertPoolCount( required string dsName ) {
		// pre-fix: 3 pools (server-cred + matching-cred shadow + override)
		// post-fix: 2 pools (server-cred-and-matching-cred collapsed + override)
		expect( poolCountFor( arguments.dsName ) ).toBe(
			  2
			, "expected 2 pools for [#arguments.dsName#] (server-cred + override), got #poolCountFor( arguments.dsName )#"
		);
	}

	private numeric function poolCountFor( required string dsName ) {
		var pools = getPageContext().getConfig().getDatasourceConnectionPools();
		var iter = pools.iterator();
		var n = 0;
		while ( iter.hasNext() ) {
			var p = iter.next();
			if ( p.getFactory().getDatasource().getName() == arguments.dsName ) n++;
		}
		return n;
	}

	private void function exerciseDatasource( required string dsName ) {
		// Phase 1: bare cfquery — server-cred pool created.
		queryExecute(
			  "select 1 from information_schema.system_tables limit 1"
			, {}
			, { datasource: arguments.dsName }
		);

		// Phase 2: matching-cred cfquery. Pre-fix this creates a duplicate
		// (shadow) pool because createId() doesn't normalise; post-fix it
		// reuses Phase 1's pool.
		queryExecute(
			  "select 1 from information_schema.system_tables limit 1"
			, {}
			, { datasource: arguments.dsName, username: "sa", password: "" }
		);

		// Phase 3: different-cred cfquery — legitimate override; auth fails
		// at HSQLDB but the pool is registered before the connection attempt.
		try {
			queryExecute(
				  "select 1 from information_schema.system_tables limit 1"
				, {}
				, { datasource: arguments.dsName, username: "other", password: "other" }
			);
		}
		catch ( any e ) {
			// expected — HSQLDB rejects unknown user
		}
	}

	private void function registerServerDatasource( required string name, required string connectionString ) {
		admin
			action="updateDatasource"
			type="server"
			password="#variables.adminPassword#"
			name="#arguments.name#"
			newName="#arguments.name#"
			classname="org.hsqldb.jdbcDriver"
			bundlename="org.lucee.hsqldb"
			dsn="#arguments.connectionString#"
			dbusername="sa"
			dbpassword=""
			connectionLimit="10"
			connectionTimeout="0"
			blob="false"
			clob="false"
			validate="false"
			storage="false"
			allowed_select="true"
			allowed_insert="true"
			allowed_update="true"
			allowed_delete="true"
			allowed_alter="true"
			allowed_drop="true"
			allowed_revoke="true"
			allowed_create="true"
			allowed_grant="true";
	}

	private void function removeServerDatasource( required string name ) {
		try {
			admin
				action="removeDatasource"
				type="server"
				password="#variables.adminPassword#"
				name="#arguments.name#";
		}
		catch ( any e ) {}
	}

}
