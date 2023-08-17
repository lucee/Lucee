component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title = "Testcase for LDEV-4668", body = function() {
			it( title = "Checking forwardslash/backslash for LDEV-4668", body = function( currentSpec ) {
				expect(18010737 / 1).toBe("18010737");
				expect(18010737 \ 1).toBe("18010737");
				expect(floor(18010737 / 1)).toBe("18010737");
				expect(floor(18010737 \ 1)).toBe("18010737");
			});
		});
	}
}