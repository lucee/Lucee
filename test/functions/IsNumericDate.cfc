component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe( title = "Testcase for isNumericDate() function", body = function() {
			it( title = "Checking isNumericDate() function", body = function( currentSpec ) {
				expect(isNumericDate('11/10/1992')).toBeTrue();
				expect(isNumericDate(createDate( 1970,1,1 ))).toBeTrue();
				expect(isNumericDate(createDateTime(2023,01,1,0,0,0,0))).toBeTrue();
			});
		});
	}
}