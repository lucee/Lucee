component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	function beforeAll() {
		variables.adminPassword = request.ServerAdminPassword;
		variables.dsName = "ldev6553.dots.test";
		removeServerDatasource( variables.dsName );
	}

	function afterAll() {
		removeServerDatasource( variables.dsName );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6553 datasource names with dots", function() {

			it( title="the admin accepts a datasource name with dots", body=function() {
				registerServerDatasource( variables.dsName, "jdbc:hsqldb:mem:ldev6553_dots;shutdown=true" );

				admin
					action="getDatasource"
					type="server"
					password="#variables.adminPassword#"
					name="#variables.dsName#"
					returnVariable="local.ds";
				expect( ds.name ).toBe( variables.dsName );
			});

			it( title="a datasource name with dots can be queried", body=function() {
				var qry = queryExecute(
					  "select 1 as one from information_schema.system_tables limit 1"
					, {}
					, { datasource: variables.dsName }
				);
				expect( qry.one ).toBe( 1 );
			});

			it( title="other special characters are still rejected", body=function() {
				expect( function() {
					registerServerDatasource( "ldev6553/invalid", "jdbc:hsqldb:mem:ldev6553_invalid;shutdown=true" );
				}).toThrow();
			});

		});
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
