component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for tan() function", body=function() {
			it( title="Checking the tan() function", body=function( currentSpec ) {
				var res = "-1.995200412208";
				expect(res).toBe(left(tan(90),len(res)));
				expect(isnumeric(tan(90))).toBeTrue();
			});
		});
	}
}