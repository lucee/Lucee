component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	function beforeAll() {
		variables.uri = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), '\/' )#/LDEV6293";
		variables.adminPassword = request.ServerAdminPassword;
		variables.serverDsName = "LDEV6293_server";
		variables.appDsName    = "LDEV6293_main";

		removeServerDatasource( variables.serverDsName );

		// 1) Server-level datasource registered via cfadmin.
		registerServerDatasource( variables.serverDsName, "jdbc:hsqldb:mem:LDEV6293_server;shutdown=true" );

		// 2) Application-level datasource defined in this main test's app.
		application action="update" datasources={
			"#variables.appDsName#" : {
				  class            : "org.hsqldb.jdbcDriver"
				, bundleName       : "org.lucee.hsqldb"
				, connectionString : "jdbc:hsqldb:mem:LDEV6293_main;shutdown=true"
				, username         : "sa"
				, password         : ""
			}
		};

		// Materialise pools.
		queryExecute(
			  "select 1 from information_schema.system_tables limit 1"
			, {}
			, { datasource: variables.serverDsName }
		);
		queryExecute(
			  "select 1 from information_schema.system_tables limit 1"
			, {}
			, { datasource: variables.appDsName }
		);

		// 3) Mini-app: separate Application.cfc + probe.cfm runs an app-level DS
		//    in another app context (LDEV6293_app), and a dynamic inline-struct DS.
		_internalRequest( template = uri & "/probe.cfm" );
	}

	function afterAll() {
		removeServerDatasource( variables.serverDsName );
	}

	function run( testResults, testBox ) {

		describe( "LDEV6293 getSystemMetrics() pool keying", function() {

			it( title="server-level datasource entry", body=function() {
				assertEntryShape( name="LDEV6293_server", expectedUser="sa" );
			});

			it( title="application-level datasource entry", body=function() {
				assertEntryShape( name="LDEV6293_main", expectedUser="sa" );
			});

			it( title="mini-app's application datasource entry", body=function() {
				assertEntryShape( name="LDEV6293_app", expectedUser="sa" );
			});

			it( title="dynamic inline-struct datasource entry (anonymous, lookup by connection string)", body=function() {
				var entries = getSystemMetrics().datasourceConnections;
				var matches = structFilter( entries, ( k, v ) => v.connectionString contains "LDEV6293_dynamic" );
				expect( structCount( matches ) ).toBe( 1 );

				var key   = structKeyArray( matches )[ 1 ];
				var entry = matches[ key ];
				expect( entry ).toHaveKey( "username" );
				expect( entry.username ).toBe( "sa" );
				expect( key ).toMatch( "^.*:sa:[0-9a-f]+$" );
			});

		});

	}

	private void function assertEntryShape( required string name, required string expectedUser ) {
		var dsName = arguments.name;
		var entries = getSystemMetrics().datasourceConnections;
		var matches = structFilter( entries, ( k, v ) => v.name == dsName );
		expect( structCount( matches ) ).toBe( 1, "exactly one entry expected for [#dsName#]; have keys: [#structKeyList( entries )#]" );

		var key   = structKeyArray( matches )[ 1 ];
		var entry = matches[ key ];
		expect( entry ).toHaveKey( "username" );
		expect( entry.username ).toBe( expectedUser );
		expect( key ).toMatch( "^#dsName#:#expectedUser#:[0-9a-f]+$" );
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
