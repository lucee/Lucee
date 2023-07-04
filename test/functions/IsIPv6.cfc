component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title = "Testcase for isIPv6() function", body = function() {
			it( title = "Checking isIPv6() function", body = function( currentSpec ) {
				expect(isIPv6("FE80:CD00:0000:0CDE:1257:0000:211E:729C")).toBeTrue();
				expect(isIPv6("127.0.0.1")).toBeFalse();
			});
		});
	}
}