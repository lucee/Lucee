component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "GetSystemPropOrEnvVarInfo() — returns a query of all supported system settings", function() {

			it( "returns a query", function() {
				expect( isQuery( GetSystemPropOrEnvVarInfo() ) ).toBeTrue();
			} );

			it( "query has column 'description'", function() {
				expect( queryColumnExists( GetSystemPropOrEnvVarInfo(), "description" ) ).toBeTrue();
			} );

			it( "query has column 'systemProperties'", function() {
				expect( queryColumnExists( GetSystemPropOrEnvVarInfo(), "systemProperties" ) ).toBeTrue();
			} );

			it( "query has column 'environmentVariables'", function() {
				expect( queryColumnExists( GetSystemPropOrEnvVarInfo(), "environmentVariables" ) ).toBeTrue();
			} );

			it( "query has at least one row", function() {
				expect( GetSystemPropOrEnvVarInfo().recordCount ).toBeGT( 0 );
			} );

		} );
	}

}
