component extends="org.lucee.cfml.test.LuceeTestCase" skip=false {
	function run( testResults, textbox ) {
		describe("Testcase for LDEV-4545", function() {
			it(title="checking precisionEvaluate(String) function", body=function( currentSpec ) {
				var str = "1827121492050112345678181400";
				expect(precisionEvaluate(str mod 97)).toBe("34");
			});
			it(title="checking precisionEvaluate(Number) function", body=function( currentSpec ) {
				var nbr = 1827121492050112345678181400;
				expect(precisionEvaluate(nbr mod 97)).toBe("34");
			});
			it(title="checking precisionEvaluate(BigInteger) function", body=function( currentSpec ) {
				var bi = JavaCast("BigInteger", "1827121492050112345678181400");
				expect(precisionEvaluate(bi mod 97)).toBe("34");
			});

			it(title="checking mod with string", body=function( currentSpec ) {
				var str = "1827121492050112345678181400";
				expect((str mod 97)).toBe("34");
			});
			it(title="checking  mod with number", body=function( currentSpec ) {
				var nbr = 1827121492050112345678181400;
				expect((nbr mod 97)).toBe("34");
			});
			it(title="checking mod with BigInteger", body=function( currentSpec ) {
				var bi = JavaCast("BigInteger", "1827121492050112345678181400");
				expect((bi mod 97)).toBe("34");
			});


		});
	}
}