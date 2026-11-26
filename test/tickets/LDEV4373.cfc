component extends="org.lucee.cfml.test.LuceeTestCase" labels="string" {

	function run( testResults, textbox ) {
		describe( "Testcase for LDEV-4373", function() {

			it( title="extract a character of a string using the array notation", body=function( currentSpec ) {
				var mystring = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
				expect( mystring[ 4 ] ).toBe( "D" );
				expect( mystring[ 1 ] ).toBe( "A" );
				expect( mystring[ 26 ] ).toBe( "Z" );
			});

			it( title="negative index counts from the end of the string, like arrays", body=function( currentSpec ) {
				var mystring = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
				expect( mystring[ -3 ] ).toBe( "X" );
				expect( mystring[ -1 ] ).toBe( "Z" );
				expect( mystring[ -26 ] ).toBe( "A" );
				var arr = [ 1, 2, 3 ];
				expect( arr[ -1 ] ).toBe( 3 );
			});

			it( title="negative index on a string stored in a struct", body=function( currentSpec ) {
				var sct = { str: "Lucee" };
				expect( sct.str[ -1 ] ).toBe( "e" );
				expect( sct.str[ -5 ] ).toBe( "L" );
			});

			it( title="index 0 and indexes out of range still throw", body=function( currentSpec ) {
				var mystring = "ABC";
				expect( function() { return mystring[ 0 ]; } ).toThrow();
				expect( function() { return mystring[ 4 ]; } ).toThrow();
				expect( function() { return mystring[ -4 ]; } ).toThrow();
			});

		});
	}

}
