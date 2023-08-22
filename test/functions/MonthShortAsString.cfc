component extends="org.lucee.cfml.test.LuceeTestCase" labels="test111"{
	function run( testResults , testBox ) {
		describe( title = "Testcase for monthShortAsString() function", body = function() {
			it( title = "Checking monthShortAsString() function", body = function( currentSpec ) {
				expect(monthShortAsString(1, "english (india)")).toBe('Jan');
				expect(monthShortAsString(2, "albanian")).toBe('shk');
				expect(monthShortAsString(monthNumber=4, locale="english (united kingdom)")).toBe('Apr');
			});
		});
	}
}

