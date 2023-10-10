component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for isArray() function", body=function() {
			it(title="Checking the isArray() function", body=function( currentSpec ) {
				expect(isArray(arrayNew(1))).toBeTrue();
				expect(isArray([])).toBeTrue();
				expect(isArray(true)).toBeFalse();
				expect(isArray("array")).toBeFalse();
			});
		});
	}
}