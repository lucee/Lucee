component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for getHttpTimeString() function", body=function() {
			it(title="Checking the getHttpTimeString() function", body=function( currentSpec ) {
				expect(getHttpTimeString(createDateTime(2023,04,11,08,10,00))).toBe("Tue, 11 Apr 2023 02:40:00 GMT");
				expect(getHttpTimeString(createDate(2023,04,11))).toBe("Mon, 10 Apr 2023 18:30:00 GMT");
			});
		});
	}
}