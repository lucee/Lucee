component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for isInstanceOf()", function() {
			it(title="checking isInstanceOf() function", body=function( currentSpec ) {
				expect(isInstanceOf({},"java.util.Map")).toBeTrue();
				expect(isInstanceOf("Lucee","java.lang.String")).toBeTrue();
				expect(isInstanceOf("String","java.lang.String")).toBeTrue();
				expect(isInstanceOf("java","java.system.lang")).toBeFalse();
				expect(isInstanceOf("Lucee","java.util.Map")).toBeFalse();
			});
		});
	}
}