component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title = "Testcase for getUserRoles() function", body = function() {
			it( title = "Checking getUserRoles() function", body = function( currentSpec ) {
				```
				<cflogin>
					<cfloginuser name = "test" password = "password" roles = "user,admin,editor">
				</cflogin>
				```
				expect(getUserRoles()).toBe("user,admin,editor");
			});
		});
	}
}