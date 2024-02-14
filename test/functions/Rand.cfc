component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe(title="Testcase for rand() function", body=function() {
			it(title="Checking the rand() function", body=function( currentSpec ) {
				expect(rand()).toBeBetween(0, 1);
				expect(rand("SHA1PRNG")).toBeBetween(0, 1);
				expect(rand("CFMX_COMPAT")).toBeBetween(0, 1);
			});
		});
	}
}