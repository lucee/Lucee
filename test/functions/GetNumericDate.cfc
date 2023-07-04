component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for getNumericDate()", function() {
			it(title="checking getNumericDate() function", body=function( currentSpec ) {
				expect(getNumericDate('11/10/1992')).toBe("33918");
				expect(getNumericDate(createDate( 1970, 1, 1 ))).toBe("25569");
				expect(getNumericDate(createDateTime(2023,01,1,0,0,0,0))).toBe("44927");
			});
		});
	}
}