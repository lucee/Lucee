component extends="org.lucee.cfml.test.LuceeTestCase" labels="array" {

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-6801 - evaluate() with the array slice syntax", function() {

			it( title="evaluate() returns the same slice as the compiled code", body=function( currentSpec ) {
				var a = [ 10, 20, 30, 40, 50 ];
				expect( evaluate( "a[2:3]" ) ).toBe( a[ 2:3 ] );
				expect( evaluate( "a[:]" ) ).toBe( a[ : ] );
				expect( evaluate( "a[::]" ) ).toBe( a[ :: ] );
				expect( evaluate( "a[2:]" ) ).toBe( a[ 2: ] );
				expect( evaluate( "a[:3]" ) ).toBe( a[ :3 ] );
				expect( evaluate( "a[::2]" ) ).toBe( a[ ::2 ] );
				expect( evaluate( "a[2::2]" ) ).toBe( a[ 2::2 ] );
				expect( evaluate( "a[1:5:2]" ) ).toBe( a[ 1:5:2 ] );
				expect( evaluate( "a[-2:]" ) ).toBe( a[ -2: ] );
				expect( evaluate( "a[::-1]" ) ).toBe( a[ ::-1 ] );
				expect( evaluate( "a[ 1 + 1 : len( a ) - 1 ]" ) ).toBe( a[ 1 + 1 : len( a ) - 1 ] );
				expect( evaluate( "a[2:4][1]" ) ).toBe( 20 );
			});

			it( title="evaluate() with a slice on a value that is not an array throws", body=function( currentSpec ) {
				var s = { k: 1 };
				expect( function() {
					evaluate( "s[1:2]" );
				} ).toThrow( regex="can only be used with arrays" );
			});

			it( title="evaluate() with an index without a slice works as before", body=function( currentSpec ) {
				var a = [ 10, 20, 30 ];
				var s = { k: "v" };
				expect( evaluate( "a[2]" ) ).toBe( 20 );
				expect( evaluate( "a[true ? 1 : 3]" ) ).toBe( 10 );
				expect( evaluate( "a[notDefinedLDEV6801 ?: 3]" ) ).toBe( 30 );
				expect( evaluate( 's["k"]' ) ).toBe( "v" );
			});
		});
	}
}
