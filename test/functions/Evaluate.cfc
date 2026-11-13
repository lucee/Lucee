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

			it(title="Checking number literal type follows preciseMath", body=function( currentSpec ) {
				var expected = getApplicationSettings().preciseMath ? "java.math.BigDecimal" : "java.lang.Double";
				expect( evaluate("1.5").getClass().getName() ).toBe( expected );
				expect( evaluate("-1.5").getClass().getName() ).toBe( expected );
			});

			it(title="Checking ++ -- and unary minus on a variable", body=function( currentSpec ) {
				var i = 5;
				var r = evaluate("++i");
				expect( r ).toBe( 6 );
				expect( toString( r ) ).toBe( "6" );
				i = 5;
				r = evaluate("--i");
				expect( r ).toBe( 4 );
				expect( toString( r ) ).toBe( "4" );
				i = 5;
				expect( evaluate("i--") ).toBe( 5 );
				i = 5;
				expect( evaluate("-i") ).toBe( -5 );
			});
		});
	}
}