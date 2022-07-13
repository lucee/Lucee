component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4070", function() {
			it( title="checking large number value with more than 19 digits", body=function( currentSpec ) {
				var largeNumberValue = 12345678901234567890;
				expect(mid(largeNumberValue, 1, 13)).toBe("1234567890123");
				expect(find("123456789.012", largeNumberValue/100000000000)).toBeGT(0);
			});
		});
	}
}