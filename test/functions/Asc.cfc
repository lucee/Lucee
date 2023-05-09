component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {

		describe( title="Test suite for Asc()", body=function() {
			it(title="Checking Asc() function", body = function( currentSpec ) {
				expect( asc( "a" ) ).toBe( 97 );
				expect( asc( "A" ) ).toBe( 65 );
				expect( asc( "	" ) ).toBe( 9 ); // TAB
				expect( asc( " 	" ) ).toBe( 32 ); // SPACE
				expect( asc( " 	", 2 ) ).toBe( 9 ); // TAB
				expect( asc( "" ) ).toBe( 0 );
				expect( asc( "abc" ) ) .toBe ( 97 );
				expect( asc( "abc", 2 ) ) .toBe ( 98 );
			});
		});
	}
}