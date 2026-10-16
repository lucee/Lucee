component extends="org.lucee.cfml.test.LuceeTestCase" {

	variables.adm = new Administrator( 'server', request.SERVERADMINPASSWORD ?: server.SERVERADMINPASSWORD );

	function beforeAll() {
		// store original security settings so we can restore them
		variables.originalSecurity = adm.getDefaultSecurityManager();
	}

	function afterAll() {
		// restore original security settings
		adm.updateDefaultSecurityManager( argumentCollection=variables.originalSecurity );
	}

	function run( testResults, testBox ) {
		describe( "Test case for LDEV-6177", function() {

			it( title="file_access should not grow on repeated saves", body=function( currentSpec ) {
				var customDir = expandPath( "/test/" );

				// save security settings with file access set to "local" and a single custom directory
				adm.updateDefaultSecurityManager(
					file: "local",
					file_access: [ "#customDir#" ]
				);

				// get the security manager back
				var security = adm.getDefaultSecurityManager();
				var firstSize = arrayLen( security.file_access );

				// save again with whatever was returned (simulating user clicking Update)
				adm.updateDefaultSecurityManager(
					file: "local",
					file_access: security.file_access
				);

				var securityAfter = adm.getDefaultSecurityManager();
				var secondSize = arrayLen( securityAfter.file_access );

				// do it a third time for good measure
				adm.updateDefaultSecurityManager(
					file: "local",
					file_access: securityAfter.file_access
				);

				var securityThird = adm.getDefaultSecurityManager();
				var thirdSize = arrayLen( securityThird.file_access );

				// the array should not grow on each save
				expect( secondSize ).toBe( firstSize, "file_access grew from #firstSize# to #secondSize# after second save" );
				expect( thirdSize ).toBe( firstSize, "file_access grew from #firstSize# to #thirdSize# after third save" );
			});

		});
	}

}
