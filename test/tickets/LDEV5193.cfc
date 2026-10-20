component extends="org.lucee.cfml.test.LuceeTestCase" labels="javacast,cast" {

	function run( testResults, testBox ) {
		describe( "LDEV-5193 casting to short/byte/int checks for overflow", function() {

			it( title="javaCast short out of range throws instead of wrapping", body=function() {
				expect( function(){ javaCast( "short", 40000 ); } ).toThrow();
				expect( function(){ javaCast( "short", -40000 ); } ).toThrow();
				expect( function(){ javaCast( "short", "32768" ); } ).toThrow();
			});

			it( title="javaCast byte out of range throws instead of wrapping", body=function() {
				expect( function(){ javaCast( "byte", 300 ); } ).toThrow();
				expect( function(){ javaCast( "byte", -129 ); } ).toThrow();
				expect( function(){ javaCast( "byte", "128" ); } ).toThrow();
			});

			it( title="javaCast int out of range throws instead of clamping or wrapping", body=function() {
				expect( function(){ javaCast( "int", 3000000000 ); } ).toThrow();
				expect( function(){ javaCast( "int", -3000000000 ); } ).toThrow();
				expect( function(){ javaCast( "int", javaCast( "long", 3000000000 ) ); } ).toThrow();
			});

			it( title="javaCast short[] with an out of range element throws", body=function() {
				expect( function(){ javaCast( "short[]", [ 1, 2, 40000 ] ); } ).toThrow();
			});

			it( title="javaCast at the type boundaries still works", body=function() {
				expect( javaCast( "short", 32767 ) ).toBe( 32767 );
				expect( javaCast( "short", -32768 ) ).toBe( -32768 );
				expect( javaCast( "short", "32767" ) ).toBe( 32767 );
				expect( javaCast( "byte", 127 ) ).toBe( 127 );
				expect( javaCast( "byte", -128 ) ).toBe( -128 );
				expect( javaCast( "int", 2147483647 ) ).toBe( 2147483647 );
				expect( javaCast( "int", -2147483648 ) ).toBe( -2147483648 );
				expect( getMetadata( javaCast( "short", 1 ) ).getName() ).toBe( "java.lang.Short" );
			});

			it( title="javaCast still truncates a fraction within range", body=function() {
				expect( javaCast( "int", 1.7 ) ).toBe( 1 );
				expect( javaCast( "short", 1.7 ) ).toBe( 1 );
				expect( javaCast( "byte", -1.7 ) ).toBe( -1 );
			});
		});
	}
}
