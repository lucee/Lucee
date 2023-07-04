component extends="org.lucee.cfml.test.LuceeTestCase" labels="struct" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for isStruct() function", body=function() {
			it(title="Checking the isStruct() function", body=function( currentSpec ) {
				expect(isStruct(structNew())).toBeTrue();
				expect(isStruct({})).toBeTrue();
				expect(isStruct(arrayNew(1))).toBeFalse();
			});
		});
	}
}