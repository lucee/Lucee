component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "ConfigSchema() — returns JSON Schema for Lucee configuration", function() {

			it( "returns a struct", function() {
				expect( isStruct( ConfigSchema() ) ).toBeTrue();
			} );

			it( "returned schema contains top-level '$schema' key", function() {
				expect( ConfigSchema()[ "$schema" ] ).toBe( "https://json-schema.org/draft/2020-12/schema" );
			} );

			it( "returned schema contains 'properties' key", function() {
				expect( structKeyExists( ConfigSchema(), "properties" ) ).toBeTrue();
			} );

		} );
	}
}