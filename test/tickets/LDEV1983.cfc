component extends="org.lucee.cfml.test.LuceeTestCase" labels="array" {

	function run( testResults, testBox ) {
		describe( "LDEV-1983 duplicate() on an array that ends with null values", function() {

			it( title="array of only null values keeps its length", body=function( currentSpec ) {
				var arr = [ javacast( "null", "" ), javacast( "null", "" ), javacast( "null", "" ) ];
				var dup = duplicate( arr );
				expect( arrayLen( arr ) ).toBe( 3 );
				expect( arrayLen( dup ) ).toBe( 3 );
				expect( arrayIsEmpty( dup ) ).toBeFalse();
			});

			it( title="trailing null is kept", body=function( currentSpec ) {
				var arr = [ 1, javacast( "null", "" ) ];
				var dup = duplicate( arr );
				expect( arrayLen( dup ) ).toBe( 2 );
				expect( dup[ 1 ] ).toBe( 1 );
				expect( isNull( dup[ 2 ] ) ).toBeTrue();
			});

			it( title="leading null is kept", body=function( currentSpec ) {
				var arr = [ javacast( "null", "" ), 1 ];
				var dup = duplicate( arr );
				expect( arrayLen( dup ) ).toBe( 2 );
				expect( isNull( dup[ 1 ] ) ).toBeTrue();
				expect( dup[ 2 ] ).toBe( 1 );
			});

			it( title="padding added with arrayResize is kept", body=function( currentSpec ) {
				var arr = [ 1, 2, 3 ];
				arrayResize( arr, 5 );
				expect( arrayLen( duplicate( arr ) ) ).toBe( arrayLen( arr ) );
			});

			it( title="shallow duplicate keeps the trailing null", body=function( currentSpec ) {
				var arr = [ 1, javacast( "null", "" ) ];
				expect( arrayLen( duplicate( arr, false ) ) ).toBe( 2 );
			});

			it( title="nested array keeps the trailing null", body=function( currentSpec ) {
				var st = { a: [ 1, javacast( "null", "" ) ] };
				expect( arrayLen( duplicate( st ).a ) ).toBe( 2 );
			});

		});
	}

}
