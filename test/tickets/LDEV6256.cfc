component extends="org.lucee.cfml.test.LuceeTestCase" labels="function,alias" {

	function run( testResults, testBox ) {
		describe( "LDEV-6256: Function-level aliases", function() {

			it( "multi-value aliases should resolve individually", function() {
				// arrayAppend has <alias>array,arr</alias>
				var primary = getFunctionData( "arrayappend" );
				expect( primary.name ).toBe( "arrayappend" );

				var alias1 = getFunctionData( "array" );
				expect( alias1.name ).toBe( "array" );
			});

			it( "multi-value aliases should not create comma-separated keys in getFunctionList", function() {
				var functions = getFunctionList();
				for ( var fname in functions ) {
					expect( find( ",", fname ) ).toBe( 0, "function name [#fname#] should not contain a comma" );
				}
			});

			it( "single-value aliases should resolve", function() {
				// AIGetMetaData has <alias>LuceeAIGetMetaData</alias>
				var primary = getFunctionData( "aigetmetadata" );
				expect( primary.name ).toBe( "aigetmetadata" );

				var alias = getFunctionData( "luceeaigetmetadata" );
				expect( alias.name ).toBe( "luceeaigetmetadata" );
			});

			it( "primary entry should expose alias field with original case", function() {
				var data = getFunctionData( "createaisession" );
				expect( data ).toHaveKey( "alias" );
				expect( data.alias ).toIncludeWithCase( "aiCreateSession" );
			});

			it( "alias copy should expose aliasOf field pointing to primary", function() {
				var data = getFunctionData( "aicreatesession" );
				expect( data ).toHaveKey( "aliasOf" );
				expect( data.aliasOf ).toBe( "CreateAISession" );
			});

			it( "primary entry should not have aliasOf field", function() {
				var data = getFunctionData( "createaisession" );
				if ( structKeyExists( data, "aliasOf" ) ) {
					expect( data.aliasOf ).toBe( "" );
				}
			});

		});
	}
}
