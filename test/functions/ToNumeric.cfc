component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for toNumeric() function", body=function() {
			it(title="Checking the toNumeric() function", body=function( currentSpec ) {
				expect(toNumeric("123.45")).toBe("123.45");
				expect(toNumeric("0110","bin")).toBe("6");
				expect(toNumeric("000C","hex")).toBe("12");
				expect(toNumeric("24","oct")).toBe("20");
			});
		});
	}
}