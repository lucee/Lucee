component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-4366", function() {
			it(title = "checking cfparam with random Attribute", body = function( currentSpec ) {
				expect( function() {
					param name="test" hint="hint";
				} ).toThrow();
			});
		});
	}
}