component extends="org.lucee.cfml.test.LuceeTestCase" labels="admin" {

	function beforeAll() {
		variables.adminPassword = request.ServerAdminPassword;
		variables.dsNoAuth = "LDEV6066_noauth";
		variables.dsWithAuth = "LDEV6066_withauth";

		// cleanup any existing datasources from previous runs
		removeDatasource( variables.dsNoAuth );
		removeDatasource( variables.dsWithAuth );

		// setup datasources
		if ( !isH2NotSupported() ) {
			var h2 = server.getTestService( "h2", server._getTempDir( "LDEV6066_noauth" ) );
			createDatasource( variables.dsNoAuth, h2, "", "" );
		}
		if ( !isMySqlNotSupported() ) {
			var mysql = mySqlCredentials();
			createDatasource( variables.dsWithAuth, mysql, mysql.username, mysql.password );
		}
	}

	function afterAll() {
		// leave artifacts for inspection per test approach
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6066 verifyDatasource without dbusername/dbpassword", function() {

			describe( "datasource without authentication (H2)", function() {

				it( title="verify with no credentials passed", skip=isH2NotSupported(), body=function() {
					admin
						action="verifyDatasource"
						type="server"
						password="#variables.adminPassword#"
						name="#variables.dsNoAuth#";
				});

				it( title="verify with empty credentials passed", skip=isH2NotSupported(), body=function() {
					admin
						action="verifyDatasource"
						type="server"
						password="#variables.adminPassword#"
						name="#variables.dsNoAuth#"
						dbusername=""
						dbpassword="";
				});

			});

			describe( "datasource with authentication (MySQL)", function() {

				it( title="verify with no credentials passed - should use stored", skip=isMySqlNotSupported(), body=function() {
					admin
						action="verifyDatasource"
						type="server"
						password="#variables.adminPassword#"
						name="#variables.dsWithAuth#";
				});

				it( title="verify with correct credentials passed", skip=isMySqlNotSupported(), body=function() {
					var mysql = mySqlCredentials();
					admin
						action="verifyDatasource"
						type="server"
						password="#variables.adminPassword#"
						name="#variables.dsWithAuth#"
						dbusername="#mysql.username#"
						dbpassword="#mysql.password#";
				});

				// empty string credentials fall back to stored credentials at the connection pool layer
				// see DatasourceConnectionFactory.java line 35: if (StringUtil.isEmpty(username))
				it( title="verify with empty credentials uses stored credentials", skip=isMySqlNotSupported(), body=function() {
					admin
						action="verifyDatasource"
						type="server"
						password="#variables.adminPassword#"
						name="#variables.dsWithAuth#"
						dbusername=""
						dbpassword="";
				});

				it( title="verify with wrong credentials should fail", skip=isMySqlNotSupported(), body=function() {
					expect( function() {
						admin
							action="verifyDatasource"
							type="server"
							password="#variables.adminPassword#"
							name="#variables.dsWithAuth#"
							dbusername="wronguser"
							dbpassword="wrongpass";
					}).toThrow();
				});

			});

		});
	}

	// Skip functions

	function isH2NotSupported() {
		return structIsEmpty( server.getTestService( "h2", server._getTempDir( "LDEV6066_noauth" ) ) );
	}

	function isMySqlNotSupported() {
		return structIsEmpty( mySqlCredentials() );
	}

	private struct function mySqlCredentials() {
		return server.getDatasource( service="mysql" );
	}

	// Helper functions

	private void function createDatasource( required string name, required struct config, required string username, required string password ) {
		admin
			action="updateDatasource"
			type="server"
			password="#variables.adminPassword#"
			name="#arguments.name#"
			newName="#arguments.name#"
			classname="#arguments.config.class#"
			bundlename="#arguments.config.bundleName#"
			bundleversion="#arguments.config.bundleVersion#"
			dsn="#arguments.config.connectionString#"
			dbusername="#arguments.username#"
			dbpassword="#arguments.password#"
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

	private void function removeDatasource( required string name ) {
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
