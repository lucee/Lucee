component extends="org.lucee.cfml.test.LuceeTestCase" labels="string" {

	function run( testResults, testBox ) {
		describe( "LDEV-5857: replaceNoCase() with an empty substring returns the string unchanged", function() {

			it( title="replaceNoCase() with the default scope (one)", body=function( currentSpec ) {
				expect( replaceNoCase( "lucee", "", "test" ) ).toBe( "lucee" );
			} );

			it( title="replaceNoCase() with scope all", body=function( currentSpec ) {
				expect( replaceNoCase( "lucee", "", "test", "all" ) ).toBe( "lucee" );
			} );

			it( title="string.replaceNoCase() member function", body=function( currentSpec ) {
				expect( "lucee".replaceNoCase( "", "test" ) ).toBe( "lucee" );
			} );

			it( title="same result as replace()", body=function( currentSpec ) {
				expect( replaceNoCase( "lucee", "", "test", "all" ) ).toBe( replace( "lucee", "", "test", "all" ) );
			} );

		} );
	}

}
