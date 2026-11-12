component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "Double boxing through the small Double cache", function() {

			it( title="count-returning BIFs return java.lang.Double", body=function( currentSpec ) {
				expect( typeOf( len( "abc" ) ) ).toBe( "java.lang.Double" );
				expect( typeOf( listLen( "a,b" ) ) ).toBe( "java.lang.Double" );
				expect( typeOf( listFindNoCase( "a,b", "B" ) ) ).toBe( "java.lang.Double" );
				expect( typeOf( structCount( { a: 1 } ) ) ).toBe( "java.lang.Double" );
				expect( len( "abc" ) ).toBe( 3 );
				expect( len( repeatString( "a", 20000 ) ) ).toBe( 20000 );
				expect( typeOf( len( repeatString( "a", 20000 ) ) ) ).toBe( "java.lang.Double" );
			});

			it( title="arithmetic results return java.lang.Double", body=function( currentSpec ) {
				var i = 0;
				i = i + 1;
				expect( typeOf( i ) ).toBe( "java.lang.Double" );
				expect( i ).toBe( 1 );
				var big = 12345;
				big = big * 3;
				expect( typeOf( big ) ).toBe( "java.lang.Double" );
				expect( big ).toBe( 37035 );
			});

			it( title="negative zero keeps its sign bit", body=function( currentSpec ) {
				var zero = 0;
				expect( signBit( zero * -1 ) ).toBeTrue();
				expect( signBit( 0 * -1 ) ).toBeTrue();
				expect( signBit( zero * 1 ) ).toBeFalse();
				expect( signBit( zero + 0 ) ).toBeFalse();
			});

			it( title="closure iteration index is java.lang.Double", body=function( currentSpec ) {
				var idx = [];
				arrayEach( [ "a", "b" ], function( v, i ) {
					arrayAppend( idx, i );
				});
				assertIndexes( idx, [ 1, 2 ] );

				assertIndexes( arrayMap( [ "a", "b" ], function( v, i ) { return i; } ), [ 1, 2 ] );

				idx = [];
				listEach( "a,b", function( v, i ) {
					arrayAppend( idx, i );
				});
				assertIndexes( idx, [ 1, 2 ] );

				idx = [];
				arrayReduce( [ "a", "b" ], function( acc, v, i ) {
					arrayAppend( idx, i );
					return acc;
				}, 0 );
				assertIndexes( idx, [ 1, 2 ] );

				idx = [];
				arrayFilter( [ "a", "b" ], function( v, i ) {
					arrayAppend( idx, i );
					return true;
				});
				assertIndexes( idx, [ 1, 2 ] );

				var qry = queryNew( "a", "varchar", [ [ "x" ], [ "y" ] ] );
				idx = [];
				queryEach( qry, function( row, i ) {
					arrayAppend( idx, i );
				});
				assertIndexes( idx, [ 1, 2 ] );

				idx = [];
				queryEvery( qry, function( row, i ) {
					arrayAppend( idx, i );
					return true;
				});
				assertIndexes( idx, [ 1, 2 ] );

				idx = [];
				querySome( qry, function( row, i ) {
					arrayAppend( idx, i );
					return false;
				});
				assertIndexes( idx, [ 1, 2 ] );
			});

		});
	}

	private boolean function signBit( required numeric n ) {
		return createObject( "java", "java.lang.Double" ).doubleToRawLongBits( n ) != 0;
	}

	private string function typeOf( required any v ) {
		return arguments.v.getClass().getName();
	}

	private void function assertIndexes( required array actual, required array expected ) {
		expect( arrayLen( arguments.actual ) ).toBe( arrayLen( arguments.expected ) );
		for ( var i = 1; i <= arrayLen( arguments.expected ); i++ ) {
			expect( typeOf( arguments.actual[ i ] ) ).toBe( "java.lang.Double" );
			expect( arguments.actual[ i ] ).toBe( arguments.expected[ i ] );
		}
	}

}
