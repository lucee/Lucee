component extends="org.lucee.cfml.test.LuceeTestCase" labels="datasource,jdbc" {

	// LDEV-6908 every way to define a datasource must resolve to a JDBC driver that can be loaded,
	// also when the JDBC extension registers its driver as Maven artifact instead of an OSGi bundle (e.g. PostgreSQL 42.7.11).
	// The cases are built from the installed JDBC extensions, see LDEV6908/Matrix.cfc

	private function createMatrix() {
		var services = {};
		if ( structKeyExists( server, "getDatasource" ) ) {
			for ( var svc in [ "postgres", "mysql", "mssql", "oracle" ] ) {
				var cfg = server.getDatasource( service=svc, onlyConfig=true );
				if ( isStruct( cfg ) && structCount( cfg ) ) services[ svc ] = cfg;
			}
		}
		return new LDEV6908.Matrix(
			services = services,
			tempDir = getTempDirectory() & "ldev6908-" & createUUID() & "/",
			adminPassword = request.serverAdminPassword ?: ""
		);
	}

	function afterAll() {
		if ( structKeyExists( variables, "matrix" ) ) variables.matrix.cleanup();
	}

	function run( testResults, testBox ) {
		// the cases are built here (specs are declared from the installed extensions), the matrix also runs them
		variables.matrix = createMatrix();
		var m = variables.matrix;
		var drivers = m.getDrivers();

		describe( "LDEV-6908 JDBC extensions", function() {
			it( title="at least one JDBC extension with a registered driver is installed", body=function( currentSpec ) {
				expect( arrayLen( drivers ) ).toBeGT( 0 );
			});
		});

		for ( var driver in drivers ) {
			describe( title="LDEV-6908 #driver.extension# #driver.extensionVersion# [#driver.class#]", body=function() {
				for ( var c in m.getCases( driver ) ) {
					if ( !c.supported ) continue;
					it( title="#c.scope#: #c.style#", data={ driver: driver, testCase: c }, body=function( data ) {
						var r = variables.matrix.runCase( data.driver, data.testCase, variables.matrix.expectConnection( data.driver, data.testCase ) );
						expect( r.resolved ).toBeTrue( "definition [#serializeJSON( data.testCase.definition )#] resolved to [#r.classDefinition#] without bundle or maven coordinates #r.error#" );
						if ( variables.matrix.expectConnection( data.driver, data.testCase ) ) {
							expect( r.connected ).toBeTrue( "connection failed: #r.error#" );
						}
					});
				}
			});
		}
	}
}
