component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for firstDayOfMonth() function", body=function() {
			it(title="Checking the firstDayOfMonth() function", body=function( currentSpec ) {
				expect(firstDayOfMonth('03/05/2018')).toBe("60");
			});
			it(title="Checking the datetime.firstDayOfMonth() member function", body=function( currentSpec ) {
				expect(createDate(1999,11,25).firstDayOfMonth()).toBe("305");
			});
		});
	}
}