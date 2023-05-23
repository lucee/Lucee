
component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for isLeapYear()", function() {
			it(title="checking isLeapYear() function", body=function( currentSpec ) {
				expect(isLeapYear(2020)).toBeTrue();
				expect(isLeapYear(2012)).toBeTrue();
				expect(isLeapYear(2020)).toBeTrue();
				expect(isLeapYear(1000)).toBeFalse();
				expect(isLeapYear(2018)).toBefalse();
				expect(isLeapYear(2023)).toBeFalse();
			});
		});
	}
}