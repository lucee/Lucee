component extends="org.lucee.cfml.test.LuceeTestCase" labels="function" {

	function run( testResults, testBox ) {
		describe( "LDEV-1669 argument and return type integer", function() {

			it( "accepts whole numbers", function() {
				expect( argInt( 8 ) ).toBe( 8 );
				expect( argInt( 0 ) ).toBe( 0 );
				expect( argInt( -3 ) ).toBe( -3 );
				expect( argInt( 16 / 2 ) ).toBe( 8 );
				expect( argInt( 2147483647 ) ).toBe( 2147483647 );
			});

			it( "accepts a numeric string with a whole number", function() {
				expect( argInt( "8" ) ).toBe( 8 );
				expect( argInt( "-12" ) ).toBe( -12 );
			});

			// ACF rejects the literal 8.0, in Lucee 8 and 8.0 are the same number so it is accepted
			it( "accepts 8.0, it is the same number as 8", function() {
				expect( argInt( 8.0 ) ).toBe( 8 );
			});

			it( "rejects numbers with decimals", function() {
				expect( function() { argInt( 8.3 ); } ).toThrow( "expression", "integer" );
				expect( function() { argInt( "8.3" ); } ).toThrow( "expression", "integer" );
				expect( function() { argInt( -0.5 ); } ).toThrow( "expression", "integer" );
			});

			it( "rejects values that are not a number", function() {
				expect( function() { argInt( "abc" ); } ).toThrow( "expression", "integer" );
				expect( function() { argInt( "" ); } ).toThrow( "expression", "integer" );
				expect( function() { argInt( true ); } ).toThrow( "expression", "integer" );
				expect( function() { argInt( [ 1 ] ); } ).toThrow( "expression", "integer" );
				expect( function() { argInt( { a: 1 } ); } ).toThrow( "expression", "integer" );
			});

			it( "rejects numbers outside of the integer range", function() {
				expect( function() { argInt( 2147483648 ); } ).toThrow( "expression", "integer" );
				expect( function() { argInt( -2147483649 ); } ).toThrow( "expression", "integer" );
			});

			it( "works with the type attribute of cfargument", function() {
				var c = new LDEV1669.Tag();
				expect( c.test( 5 ) ).toBe( 5 );
				expect( function() { c.test( 5.5 ); } ).toThrow( "expression", "integer" );
			});

			it( "works for the return type", function() {
				expect( retInt( 8 ) ).toBe( 8 );
				expect( retInt( "8" ) ).toBe( 8 );
				expect( function() { retInt( 8.3 ); } ).toThrow( "expression", "integer" );
				expect( function() { retInt( "x" ); } ).toThrow( "expression", "integer" );
			});

			it( "works for arrays of integers", function() {
				expect( argInts( [ 1, 2, "3" ] ) ).toHaveLength( 3 );
				expect( function() { argInts( [ 1, 2.5 ] ); } ).toThrow( "expression", "integer" );
			});

			it( "works for an optional argument with a default value", function() {
				expect( optInt() ).toBe( 7 );
				expect( optInt( 9 ) ).toBe( 9 );
				expect( function() { optInt( 9.5 ); } ).toThrow( "expression", "integer" );
			});

			it( "is not case sensitive", function() {
				expect( argIntUpper( 3 ) ).toBe( 3 );
				expect( function() { argIntUpper( 3.5 ); } ).toThrow( "expression", "integer" );
			});

			it( "numeric still accepts decimals", function() {
				expect( argNum( 8.3 ) ).toBe( 8.3 );
			});
		});
	}

	private function argInt( required integer id ) { return arguments.id; }
	private function argIntUpper( required Integer id ) { return arguments.id; }
	private function argNum( required numeric id ) { return arguments.id; }
	private function argInts( required integer[] ids ) { return arguments.ids; }
	private function optInt( integer id = 7 ) { return arguments.id; }
	private integer function retInt( required any v ) { return arguments.v; }
}
