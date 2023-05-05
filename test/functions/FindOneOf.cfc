component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Testcase for findOneOf",  body=function() {
			it(title="Checking with findOneOf function", body=function( currentSpec ) {
				expect( findOneOf("a", "a a a a b b b b" ) ).toBe(1);
				expect( findOneOf("b", "a a a a b b b b" ) ).toBe(9);
				expect( findOneOf("c", "a a a a b b b b" ) ).toBe(0);
				expect( findOneOf("A", "a a a a b b b b" ) ).toBe(0);
			});
			it(title="Checking with string.findOneOf() member function", body=function( currentSpec ) {
				expect( "a a a a b b b b".findOneOf("a") ).toBe(1);
				expect( "a a a a b b b b".findOneOf("b") ).toBe(9);
				expect( "a a a a b b b b".findOneOf("c") ).toBe(0);
				expect( "a a a a b b b b".findOneOf("A") ).toBe(0);
			});
		});
	}
}