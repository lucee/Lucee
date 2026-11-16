component extends="org.lucee.cfml.test.LuceeTestCase" labels="sort" {

	function run( testResults, testBox ) {
		describe( "LDEV-3674 sort comparator returning a - b (only the sign counts)", function() {

			it( title="arraySort with integers", body=function( currentSpec ) {
				var arr = [ 3, 1, 2 ];
				arraySort( arr, function( a, b ) { return a - b; } );
				expect( arr ).toBe( [ 1, 2, 3 ] );
			});

			it( title="arraySort with decimals (difference between -1 and 1)", body=function( currentSpec ) {
				var arr = [ 1.5, 1.2, 1.9, 1.1 ];
				arraySort( arr, function( a, b ) { return a - b; } );
				expect( serializeJSON( arr ) ).toBe( "[1.1,1.2,1.5,1.9]" );
			});

			it( title="array.sort() member function with decimals, descending", body=function( currentSpec ) {
				var arr = [ 0.3, 0.1, 0.2 ];
				arr.sort( function( a, b ) { return b - a; } );
				expect( serializeJSON( arr ) ).toBe( "[0.3,0.2,0.1]" );
			});

			it( title="arraySort with a difference outside the int range", body=function( currentSpec ) {
				var arr = [ 5000000000, 1, -5000000000, 3000000000 ];
				arraySort( arr, function( a, b ) { return a - b; } );
				expect( serializeJSON( arr ) ).toBe( "[-5000000000,1,3000000000,5000000000]" );
			});

			it( title="structSort with decimals", body=function( currentSpec ) {
				var sct = [ a: 1.5, b: 1.2, c: 1.9, d: 1.1 ];
				// the structSort callback gets the keys
				var keys = structSort( sct, function( k1, k2 ) { return sct[ k1 ] - sct[ k2 ]; } );
				expect( arrayToList( keys ) ).toBe( "d,b,a,c" );
			});

			it( title="structToSorted with decimals", body=function( currentSpec ) {
				var sct = [ a: 1.5, b: 1.2, c: 1.9, d: 1.1 ];
				var sorted = structToSorted( sct, function( v1, v2, k1, k2 ) { return v1 - v2; } );
				expect( structKeyList( sorted ) ).toBe( "d,b,a,c" );
			});

			it( title="querySort with decimals", body=function( currentSpec ) {
				var qry = queryNew( "v", "decimal", [ [ 1.5 ], [ 1.2 ], [ 1.9 ], [ 1.1 ] ] );
				querySort( qry, function( r1, r2 ) { return r1.v - r2.v; } );
				expect( valueList( qry.v ) ).toBe( "1.1,1.2,1.5,1.9" );
			});

			it( title="a non numeric return value still throws", body=function( currentSpec ) {
				expect( function() {
					arraySort( [ 2, 1 ], function( a, b ) { return "abc"; } );
				}).toThrow();
			});
		});
	}
}
