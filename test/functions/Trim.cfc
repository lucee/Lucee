component extends="org.lucee.cfml.test.LuceeTestCase" {

	public function run( testResults , testBox ) {
		describe( title = "Testcase for Trim()", body = function() {
			it(title = "checking Trim()", body = function( currentSpec ) {
				expect( trim("welcome ") ).toBe( "welcome");
				expect( trim(" welcome ") ).toBe( "welcome");
				expect( trim(" welcome") ).toBe( "welcome");
			});

			it(title = "checking Trim() member function", body = function( currentSpec ) {
				expect( ("welcome ").trim() ).toBe( "welcome");
				expect( (" welcome ").trim() ).toBe( "welcome");
				expect( (" welcome").trim() ).toBe( "welcome");
			});
		});
	}
}