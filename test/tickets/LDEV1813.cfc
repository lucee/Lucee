component extends="org.lucee.cfml.test.LuceeTestCase" labels="array" {

	function run( testResults, testBox ) {
		describe( "Test suite for LDEV-1813 array slice syntax arr[from:to:step]", function() {

			it( title="Array Slice Implementation", body=function( currentSpec ) {
				var a = [ 1, 2, 3, 4, 5, 6, 7, 8 ];
				var arr = [ "Aa", "Bb", "Cc", "Dd", "Ee" ];
				assertEquals( [ 1, 2, 3, 4, 5, 6, 7, 8 ], a[ : ] ); // entire array
				assertEquals( [ 1, 2, 3, 4, 5, 6 ], a[ 1:6 ] ); // index 1 to 6, the end is inclusive
				assertEquals( [ 1, 3, 5 ], a[ 1:6:2 ] ); // step 2
				assertEquals( [ 5, 6, 7, 8 ], a[ 5: ] ); // 5 to the end
				assertEquals( [ 1, 2, 3 ], a[ :3 ] ); // start to 3
				assertEquals( [ 1, 2, 3, 4 ], a[ :-5 ] ); // negative index counts from the end
				assertEquals( [ 1, 3, 5, 7 ], a[ :-2:2 ] );
				assertEquals( [ "Aa", "Cc", "Ee" ], arr[ ::2 ] );
				assertEquals( [ "Ee", "Dd", "Cc", "Bb", "Aa" ], arr[ -1:1:-1 ] );
				assertEquals( [ "Aa", "Cc" ], arr[ 1:-2:2 ] );
			});

			it( title="omitted parts and negative step", body=function( currentSpec ) {
				var a = [ 1, 2, 3, 4, 5 ];
				assertEquals( [ 1, 2, 3, 4, 5 ], a[ :: ] );
				assertEquals( [ 1, 2, 3, 4, 5 ], a[ 1: ] );
				assertEquals( [ 1, 2, 3, 4, 5 ], a[ :5 ] );
				assertEquals( [ 5, 4, 3, 2, 1 ], a[ ::-1 ] );
				assertEquals( [ 5, 3, 1 ], a[ ::-2 ] );
				assertEquals( [ 3, 2, 1 ], a[ 3::-1 ] );
				assertEquals( [ 5, 4, 3 ], a[ :3:-1 ] );
				assertEquals( [ 2, 4 ], a[ 2::2 ] );
				assertEquals( [ 2, 4 ], a[ 2:5:2 ] );
				assertEquals( [ 5 ], a[ -1: ] );
				assertEquals( [ 4, 5 ], a[ -2: ] );
				assertEquals( [ 3 ], a[ 3:3 ] );
				assertEquals( [ 1 ], a[ :1 ] );
				assertEquals( [], a[ 4:2 ] ); // start after end with a positive step
				assertEquals( [], a[ 2:4:-1 ] ); // start before end with a negative step
			});

			it( title="whitespace, expressions and variables as parts", body=function( currentSpec ) {
				var a = [ 10, 20, 30, 40, 50 ];
				var from = 2;
				var to = 4;
				var step = 2;
				var idx = [ 2, 4 ];
				assertEquals( [ 20, 30, 40 ], a[ 2 : 4 ] );
				assertEquals( [ 20, 40 ], a[ from:to:step ] );
				assertEquals( [ 20, 40 ], a[ from::2 ] ); // variable followed by ::
				assertEquals( [ 20, 30, 40 ], a[ idx[ 1 ]:idx[ 2 ] ] );
				assertEquals( [ 20, 30, 40 ], a[ from:to+0 ] );
				assertEquals( [ 40, 50 ], a[ arrayLen( a ) - 1: ] );
				assertEquals( [ 30, 40 ], a[ int( 3 ):int( 4 ) ] ); // function calls as parts
				assertEquals( [ 20, 30 ], a[ from : from + 1 ] );
				assertEquals( [ 20, 40 ], a[ ( from ):( to ):( step ) ] );
				assertEquals( [ 20, 30, 40 ], a[ true ? 2 : 3 : 4 ] ); // ternary as part
			});

			it( title="result is a new array, chaining and nesting", body=function( currentSpec ) {
				var a = [ 1, 2, 3, 4, 5 ];
				var s = a[ 2:4 ];
				s[ 1 ] = 99;
				assertEquals( [ 1, 2, 3, 4, 5 ], a ); // original is not changed
				assertEquals( 3, a[ 2:4 ][ 2 ] );
				assertEquals( 3, a[ 2:4 ].len() );
				assertEquals( [ 3, 4, 5 ], a[ 2:5 ][ 2: ] ); // slice of a slice
				assertEquals( [ 3 ], a[ a[ 2:3 ][ 2 ]:3 ] ); // nested index and slice
				var s2 = { arr: [ 1, 2, 3, 4 ] };
				assertEquals( [ 2, 3 ], s2.arr[ 2:3 ] );
				assertEquals( [ 2, 3 ], s2[ "arr" ][ 2:3 ] );
				var m = [ [ 1, 2, 3 ], [ 4, 5, 6 ], [ 7, 8, 9 ] ];
				assertEquals( [ [ 4, 5, 6 ], [ 7, 8, 9 ] ], m[ 2: ] );
				assertEquals( [ 5, 6 ], m[ 2:3 ][ 1 ][ 2: ] );
				var empty = [];
				assertEquals( [], empty[ : ] );
				assertEquals( [], empty[ ::-1 ] );
			});

			it( title="slice syntax in a function argument and in a ternary", body=function( currentSpec ) {
				var a = [ 1, 2, 3, 4 ];
				assertEquals( "2,3", arrayToList( a[ 2:3 ] ) );
				assertEquals( [ 2, 3 ], ( true ? a[ 2:3 ] : [] ) );
				assertEquals( [ 4 ], ( false ? [] : a[ 4: ] ) );
				assertEquals( "x", ( arrayLen( a[ 2:3 ] ) == 2 ? "x" : "y" ) );
				assertEquals( [ 2, 3 ], ( true ? a[ 2:3 ] : a[ 1:2 ] ) );
			});

			it( title="invalid slices throw", body=function( currentSpec ) {
				var a = [ 1, 2, 3 ];
				var str = "abcdef";
				expect( function() { var x = a[ 1:3:0 ]; } ).toThrow(); // step 0
				expect( function() { var x = a[ 0:2 ]; } ).toThrow();
				expect( function() { var x = a[ 1:4 ]; } ).toThrow();
				expect( function() { var x = a[ 4: ]; } ).toThrow();
				expect( function() { var x = a[ -4: ]; } ).toThrow();
				expect( function() { var x = str[ 1:2 ]; } ).toThrow();
				expect( function() { var x = {}[ 1:2 ]; } ).toThrow();
			});

			it( title="normal index access is not affected", body=function( currentSpec ) {
				var a = [ 1, 2, 3, 4, 5 ];
				var s = { a: 1, "b c": 2, "1": 3 };
				var i = 2;
				assertEquals( 1, a[ 1 ] );
				assertEquals( 5, a[ 5 ] );
				assertEquals( 3, a[ i + 1 ] );
				assertEquals( 2, a[ i ] );
				assertEquals( 4, a[ a[ 2 ] + 2 ] );
				assertEquals( 1, s[ "a" ] );
				assertEquals( 2, s[ "b c" ] );
				assertEquals( 3, s[ 1 ] );
				assertEquals( 1, s.a );
				assertEquals( 2, a[ true ? 2 : 3 ] );
				assertEquals( 3, a[ false ? 2 : 3 ] );
				assertEquals( 3, a[ i == 2 ? 3 : 4 ] );
				assertEquals( 2, a[ structCount( { a: 1, b: 2 } ) ] );
				assertEquals( 2, a[ int( 2 ) ] );
				assertEquals( "b", s.keyArray().len() == 3 ? "b" : "c" );
				expect( function() { var x = a[ 6 ]; } ).toThrow();
				expect( function() { var x = a[ 0 ]; } ).toThrow();
				var res = [ 1, 2 ];
				res[ 2 ] = 5;
				assertEquals( [ 1, 5 ], res );
			});

		});
	}

}
