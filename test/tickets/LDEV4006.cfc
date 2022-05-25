component extends="org.lucee.cfml.test.LuceeTestCase" labels="json" skip=true{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-4006", function() {
			it( title = "Using exponent with large value in IsJson()", body=function( currentSpec ) {
				expect(isJson("blah-0E361118307")).toBeFalse();
				try {
					var res = isJson("blah-0E3611183072"); // using 0E3611183072 also throws the same error
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toBe(false);
			});
		});
	}
}