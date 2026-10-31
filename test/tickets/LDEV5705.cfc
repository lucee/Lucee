component extends="org.lucee.cfml.test.LuceeTestCase" labels="datasource" {

	function beforeAll() {
		variables.adminPassword = request.ServerAdminPassword;
		variables.prefix = "LDEV5705_";
	}

	function afterAll() {
		removeServerDatasourceIfExists( variables.prefix & "admin" );
		removeServerDatasourceIfExists( variables.prefix & "import" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5705 per-datasource SQL restrictions are removed", function() {

			it( title="inline datasource with allow=0 no longer restricts statements", skip=isH2NotSupported(), body=function() {
				var ds = h2( "inline", { allow: 0 } );
				runAllStatements( ds, "t5705_inline" );
			});

			it( title="admin updateDatasource with allowed_*=false is accepted and does not restrict statements", skip=isH2NotSupported(), body=function() {
				var name = variables.prefix & "admin";
				registerServerDatasource( name, "jdbc:h2:mem:ldev5705admin;DB_CLOSE_DELAY=-1", false );
				runAllStatements( name, "t5705_admin" );
			});

			it( title="admin getDatasource no longer returns the allow flags", skip=isH2NotSupported(), body=function() {
				var name = variables.prefix & "admin";
				registerServerDatasource( name, "jdbc:h2:mem:ldev5705admin;DB_CLOSE_DELAY=-1", true );
				admin action="getDatasource" type="server" password="#variables.adminPassword#" name="#name#" returnVariable="local.ds";
				expect( ds.name ).toBe( name );
				for ( var key in [ "select", "insert", "update", "delete", "create", "drop", "grant", "revoke", "alter" ] ) {
					expect( ds ).notToHaveKey( key );
				}
			});

			it( title="configImport with a legacy allow bitmask is accepted and does not restrict statements", skip=isH2NotSupported(), body=function() {
				var name = variables.prefix & "import";
				// allow=0 used to mean "nothing is allowed" on a stored value; 1 only allowed select
				configImport( data={ "datasources": { "#name#": {
					"class": "org.h2.Driver",
					"bundleName": "org.lucee.h2",
					"dsn": "jdbc:h2:mem:ldev5705import;DB_CLOSE_DELAY=-1",
					"username": "sa",
					"password": "",
					"allow": 1
				} } }, type="server", password=variables.adminPassword );
				runAllStatements( name, "t5705_import" );
			});

			it( title="legacy allow* config keys are accepted", skip=isH2NotSupported(), body=function() {
				var name = variables.prefix & "import";
				configImport( data={ "datasources": { "#name#": {
					"class": "org.h2.Driver",
					"bundleName": "org.lucee.h2",
					"dsn": "jdbc:h2:mem:ldev5705import;DB_CLOSE_DELAY=-1",
					"username": "sa",
					"password": "",
					"allowSelect": false,
					"allowInsert": false,
					"allowUpdate": false,
					"allowDelete": false,
					"allowCreate": false,
					"allowDrop": false,
					"allowAlter": false,
					"allowGrant": false,
					"allowRevoke": false
				} } }, type="server", password=variables.adminPassword );
				runAllStatements( name, "t5705_import_legacy" );
			});

		});
	}

	// every statement type that used to be restrictable (grant and revoke are not supported by H2 in a meaningful way)
	private void function runAllStatements( required any datasource, required string table ) {
		var opts = { datasource: arguments.datasource };
		var t = arguments.table;
		try {
			queryExecute( "create table #t# ( id int primary key, name varchar(20) )", {}, opts );
			queryExecute( "insert into #t# ( id, name ) values ( 1, 'a' )", {}, opts );
			queryExecute( "update #t# set name = 'b' where id = 1", {}, opts );

			var q = queryExecute( "select id, name from #t#", {}, opts );
			expect( q.recordCount ).toBe( 1 );
			expect( q.name ).toBe( "b" );

			queryExecute( "alter table #t# add column extra int", {}, opts );
			queryExecute( "delete from #t# where id = 1", {}, opts );
			q = queryExecute( "select id from #t#", {}, opts );
			expect( q.recordCount ).toBe( 0 );
		}
		finally {
			queryExecute( "drop table if exists #t#", {}, opts );
		}
	}

	private struct function h2( required string id, struct extra={} ) {
		var ds = {
			class: "org.h2.Driver",
			bundleName: "org.lucee.h2",
			connectionString: "jdbc:h2:mem:ldev5705#arguments.id#;DB_CLOSE_DELAY=-1",
			username: "sa",
			password: ""
		};
		structAppend( ds, arguments.extra );
		return ds;
	}

	private void function registerServerDatasource( required string name, required string connectionString, required boolean allowed ) {
		admin
			action="updateDatasource"
			type="server"
			password="#variables.adminPassword#"
			name="#arguments.name#"
			newName="#arguments.name#"
			classname="org.h2.Driver"
			bundlename="org.lucee.h2"
			dsn="#arguments.connectionString#"
			dbusername="sa"
			dbpassword=""
			connectionLimit="5"
			connectionTimeout="0"
			blob="false"
			clob="false"
			validate="false"
			storage="false"
			allowed_select="#arguments.allowed#"
			allowed_insert="#arguments.allowed#"
			allowed_update="#arguments.allowed#"
			allowed_delete="#arguments.allowed#"
			allowed_alter="#arguments.allowed#"
			allowed_drop="#arguments.allowed#"
			allowed_revoke="#arguments.allowed#"
			allowed_create="#arguments.allowed#"
			allowed_grant="#arguments.allowed#";
	}

	private void function removeServerDatasourceIfExists( required string name ) {
		try {
			admin action="removeDatasource" type="server" password="#variables.adminPassword#" name="#arguments.name#";
		}
		catch ( any e ) {}
	}

	private boolean function isH2NotSupported() {
		try {
			var ds = server.getTestService( "h2", server._getTempDir( "LDEV5705" ) );
			return structIsEmpty( ds );
		}
		catch ( any e ) {
			return true;
		}
	}

}
