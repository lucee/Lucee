component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe("Testcase for IsUserInRole() function", function() {
			it( title="Checking IsUserInRole() function", body=function( currentSpec ) {
				```
				<cflogin>
					<cfloginuser name = "test" password = "password" roles = "user,admin,editor">
				</cflogin>
				```
				expect( isUserInRole ( role_name="user" ) ).toBeTrue();
				expect( isUserInRole ( role_name="user,admin" ) ).toBeTrue();
				expect( isUserInRole ( role_name="customer" ) ).toBeFalse();
			});
		});
	}
}