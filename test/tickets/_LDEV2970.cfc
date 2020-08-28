component extends="org.lucee.cfml.test.LuceeTestCase"{

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
		});
	}

}