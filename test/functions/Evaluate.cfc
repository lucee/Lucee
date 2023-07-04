component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe(title="Testcase for evaluate()", body=function() {
			it(title="Checking the evaluate() function", body=function( currentSpec ) {
				var str1 = "Lucee";
				var str2 = "Lucee";
				expect(evaluate("str1 eq str2")).toBeTrue();
				expect(evaluate("10 eq 12")).toBeFalse();
				expect(evaluate("10 neq 12")).toBeTrue();
				expect(evaluate("1 + 2 * 3")).toBe(7);
				expect(evaluate("10 gt 12")).toBeFalse();
				expect(evaluate("13 gt 12")).toBeTrue();
			});
		});
	}
}