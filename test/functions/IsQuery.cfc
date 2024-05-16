component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe(title="Testcase for isQuery() function", body=function() {
			it(title="Checking the isQuery() function", body=function( currentSpec ) {
				var qry = queryNew('col1')
				expect(isQuery(qry)).toBeTrue();
				expect(isQuery("I love lucee")).toBeFalse();
			});
		});
	}
}