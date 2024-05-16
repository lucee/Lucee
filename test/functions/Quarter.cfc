component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for quarter() function", body=function() {
			it(title="Checking the quarter() function", body=function( currentSpec ) {
  				var date = createDate(2018, 12, 30);
				expect(quarter(createDate(2018, 05, 04))).toBe("2");
				expect(quarter(createDate(2018, 02, 04))).toBe("1");
				expect(quarter(createDate(2018, 07, 04))).toBe("3");
				expect(date.quarter()).toBe("4");
			});
		});
	}
}