component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for iIf() function", body=function() {
			it(title="Checking with iIf() function", body=function( currentSpec ) {
				expect(iIf(true, "true", "false")).toBeTrue();
				expect(iIf(false, "true", "false")).toBeFalse();
				expect(iIf(1>5, "true", "false")).toBeFalse();
			});
		});
	}
}