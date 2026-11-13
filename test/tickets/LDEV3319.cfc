component extends="org.lucee.cfml.test.LuceeTestCase" labels="array" {

	function run( testResults, testBox ) {
		describe( "LDEV-3319 arrayFirst() / arrayLast() with an undefined element", function() {

			it( "arrayFirst() does not throw when position 1 is undefined", function() {
				var arr = [];
				arrayResize( arr, 15 );
				arr[ 15 ] = 1;
				var res = arrayFirst( arr );
				expect( isNull( res ) ).toBeTrue();
			});

			it( "arrayLast() does not throw when the last position is undefined", function() {
				var arr = [];
				arrayResize( arr, 3 );
				arr[ 1 ] = 1;
				arr[ 3 ] = 3;
				arrayDeleteAt( arr, 3 );
				arrayResize( arr, 3 );
				var res = arrayLast( arr );
				expect( isNull( res ) ).toBeTrue();
			});

			it( "member function first() does not throw when position 1 is undefined", function() {
				var arr = [];
				arr[ 5 ] = "x";
				var res = arr.first();
				expect( isNull( res ) ).toBeTrue();
			});

			it( "arrayFirst() still returns a defined first element", function() {
				expect( arrayFirst( [ "a", "b" ] ) ).toBe( "a" );
				expect( arrayLast( [ "a", "b" ] ) ).toBe( "b" );
			});

			it( "arrayFirst() on an empty array still throws", function() {
				expect( function() { arrayFirst( [] ); } ).toThrow();
			});
		});
	}
}
