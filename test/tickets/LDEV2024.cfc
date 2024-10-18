component extends="org.lucee.cfml.test.LuceeTestCase" labels="zip"{
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2024", function() {
			it(title = "Checking multiple characters as delimiter", body = function( currentSpec ) {
				expect(listAppend("1, 2", 3, ", ")).toBe("1, 2, 3");
				expect(listAppend("1,2", 3, ", ")).toBe("1,2, 3");
				expect(listAppend("1 | 2", 3, " | ")).toBe("1 | 2 | 3");
				expect(listAppend("1delim2", 3, "delim")).toBe("1delim2delim3");
			});
		});
	}
}
