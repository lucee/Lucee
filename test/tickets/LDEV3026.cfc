component extends = "org.lucee.cfml.test.luceetestcase" {

	function run( testResults , testbox ) {
		describe( "test case for the LDEV-3026", function() {
			it( title = "Checking containsAll() function", body = function( currentSpec ){
				myArray = ["a","b","c","d","e"];
				expect(myArray.containsAll([1,2,3])).toBe(false);
				expect(myArray.containsAll(["a","c","e","h"])).toBe(false);
				expect(myArray.containsAll(["A","C","E"])).toBe(false);
				expect(myArray.containsAll(["a","c","e"])).toBe(true);
				expect(myArray.containsAll(["e","c","a"])).toBe(true);
				expect(myArray.containsAll(["a","a","a","b","b","c","c"])).toBe(true);
			});
		});
	}
}