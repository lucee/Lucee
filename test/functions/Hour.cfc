component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( "testcase for Hour()", function() {
			it(title="Checking with Hour() function", body=function( currentSpec ) {
				var dt = createDateTime(2018, 07, 30, 06, 15, 45);
				setLocale("english (us)");
				expect(hour(dt)).toBe(6);
				expect(hour(now())).toBeBetween(0, 23);
			});
		});
	}
}