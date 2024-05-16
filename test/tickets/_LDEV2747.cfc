component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2747", function() {
			it(title = "isnumeric function", body = function( currentSpec ) {
				expect(isNumeric(6.62607004e-34)).toBe(true);
				expect(isNumeric(-77)).toBe(true);
				expect(isNumeric(0)).toBe(true);
				expect(isNumeric(-0)).toBe(true);
				expect(isNumeric(0.34e-22)).toBe(true);
				expect(isNumeric(5e2)).toBe(true);
				expect(isNumeric("lucee")).toBe(false);
				expect(isNumeric("6.62607004e-34")).toBe(true);
			});
		});
	}

}