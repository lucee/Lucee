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
		describe( "Test case for LDEV-6242", function() {

			it( title="custom file_access directories should survive a round-trip through config", body=function( currentSpec ) {
				var customDir = expandPath( "/test/" );

				// save security settings with file access set to "local" and a custom directory
				adm.updateDefaultSecurityManager(
					file: "local",
					file_access: [ "#customDir#" ]
				);

				// reload from config (updateDefaultSecurityManager triggers resetDefaultSecurityManager
				// which clears the cached instance, so getDefaultSecurityManager re-reads from config)
				var security = adm.getDefaultSecurityManager();

				// the custom directory should be present after reloading from config
				expect( security.file_access ).toBeArray();

				var found = false;
				var normalizedCustomDir = replace( customDir, "\", "/", "all" );
				loop array=security.file_access item="local.path" {
					if ( replace( path, "\", "/", "all" ) == normalizedCustomDir
						|| replace( path & "/", "\", "/", "all" ) == normalizedCustomDir ) {
						found = true;
						break;
					}
				}
				expect( found ).toBeTrue( "custom directory [#customDir#] was not found in file_access after reload from config: #security.file_access.toList()#" );
			});

		});
	}

}
