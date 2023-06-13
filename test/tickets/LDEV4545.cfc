component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("Testcase for LDEV-4545", function() {
			it(title="checking precisionEvaluate() function", body=function( currentSpec ) {
				var bi = JavaCast("BigInteger", "1827121492050112345678181400");
				expect(precisionEvaluate(bi mod 97)).toBe("34");
			});
		});
	}
}