component extends="org.lucee.cfml.test.LuceeTestCase" labels="array" {

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2970", function() {
			it( title = "array slice function", body=function( currentSpec ) {
				myarray = ["one","two","three","TWO","five","Two"];
				res = arrayslice(myarray,2);
				expect(arraytolist(res)).toBe("two,three,TWO,five,Two");
				res = arrayslice(myarray,-2);
				expect(arraytolist(res)).toBe("five,Two");
				res = arrayslice(myarray,-1);
				expect(arraytolist(res)).toBe("Two");
			});

			it( title = "array slice member function", body=function( currentSpec ) {
				myarray = ["one","two","three","TWO","five","Two"];
				res = myarray.slice(2);
				expect(arraytolist(res)).toBe("two,three,TWO,five,Two");
				res = myarray.slice(-2);
				expect(arraytolist(res)).toBe("five,Two");
				res = myarray.slice(-1);
				expect(arraytolist(res)).toBe("Two");
			});

			it( title = "negative offset with a length", body=function( currentSpec ) {
				var myarray = [ 1, 2, 3, 4, 5, 6 ];
				expect( arrayToList( myarray.slice( -2, 1 ) ) ).toBe( "5" );
				expect( arrayToList( myarray.slice( -3, 2 ) ) ).toBe( "4,5" );
				expect( arrayToList( myarray.slice( -6 ) ) ).toBe( "1,2,3,4,5,6" );
			});

			it( title = "offset 0 and a negative offset before the first element are invalid", body=function( currentSpec ) {
				var myarray = [ 1, 2, 3 ];
				expect( function() { myarray.slice( 0 ); } ).toThrow();
				expect( function() { myarray.slice( -4 ); } ).toThrow();
			});
		});
	}

}
