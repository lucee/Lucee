component extends="org.lucee.cfml.test.LuceeTestCase" labels="mssql,datasource" {

	function run( testResults, testBox ) {
		describe( "LDEV-6443 creating a MSSQL datasource with the admin (verify) uses the driver of the extension", function() {

			it( title="verify loads the driver and fails on the connection, not on the OSGi bundle", body=function( currentSpec ) {
				var id = "ldev6443_" & replace( lCase( createUUID() ), "-", "", "all" );
				// port 1 on localhost, nothing is listening there, so the driver has to be loaded to get the connection error
				try {
					expect( function() {
						createDatasource( id, "127.0.0.1", 1, "master", "sa", "nopassword" );
					}).toThrow( regex="TCP/IP connection" );
					expect( datasourceExists( id ) ).toBeFalse();
				}
				finally {
					removeDatasource( id );
				}
			});

			it( title="create, verify and use a MSSQL datasource like the admin does", skip=noMssql(), body=function( currentSpec ) {
				var id = "ldev6443_" & replace( lCase( createUUID() ), "-", "", "all" );
				var cfg = server.getTestService( service="mssql", onlyConfig=true );
				try {
					createDatasource( id, cfg.SERVER, cfg.PORT, cfg.DATABASE, cfg.USERNAME, cfg.PASSWORD );
					expect( datasourceExists( id ) ).toBeTrue();
					var qry = queryExecute( "SELECT 6443 AS nr", {}, { datasource: id } );
					expect( qry.nr ).toBe( 6443 );
				}
				finally {
					removeDatasource( id );
				}
			});

		});
	}

	// the same attributes as the admin (services.datasource.create.cfm) with the MSSQL driver of the extension
	private function createDatasource( required string name, required string host, required numeric port, required string database, required string username, required string password ) {
		admin
			action="updateDatasource" type="server" password=request.SERVERADMINPASSWORD
			id="mssql" dbdriver="MSSQL"
			classname="com.microsoft.sqlserver.jdbc.SQLServerDriver" bundlename="org.lucee.mssql" bundleversion=""
			dsn="jdbc:sqlserver://{host}:{port}"
			name=arguments.name newName=arguments.name
			host=arguments.host database=arguments.database port=arguments.port
			dbusername=arguments.username dbpassword=arguments.password
			custom={ DATABASENAME: arguments.database, sendStringParametersAsUnicode: "true", SelectMethod: "direct", trustServerCertificate: "true" }
			connectionLimit="10" connectionTimeout="1" blob="false" clob="false" validate="false" storage="false"
			allowed_select="true" allowed_insert="true" allowed_update="true" allowed_delete="true" allowed_alter="true"
			allowed_drop="true" allowed_revoke="true" allowed_create="true" allowed_grant="true"
			verify=true;
	}

	private boolean function datasourceExists( required string name ) {
		admin action="getDatasources" type="server" password=request.SERVERADMINPASSWORD returnVariable="local.dss";
		return queryColumnData( dss, "name" ).findNoCase( arguments.name ) > 0;
	}

	private function removeDatasource( required string name ) {
		if ( datasourceExists( arguments.name ) ) {
			admin action="removeDatasource" type="server" password=request.SERVERADMINPASSWORD name=arguments.name;
		}
	}

	private boolean function noMssql() {
		return structIsEmpty( server.getTestService( service="mssql", onlyConfig=true ) );
	}
}
