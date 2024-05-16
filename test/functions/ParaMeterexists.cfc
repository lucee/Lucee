component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title = "Testcase for parameterExists function", body = function() {
			it( title = "checking parameterExists() function", body = function( currentSpec ) {
				boolean = true;
				string = "I Love Lucee";
				numeric = 100;
				expect(ParameterExists("boolean")).toBe(true);
				expect(ParameterExists("string")).toBe(true);
				expect(ParameterExists("number")).toBe(false);
			});
		});
	}
}