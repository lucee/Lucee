component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function beforeAll() {
		variables.adminPassword = request.SERVERADMINPASSWORD ?: "webweb";
		variables.cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
		variables.ConfigAdmin = createObject( "java", "lucee.runtime.config.ConfigAdmin" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6813: detect a change of the config file made outside of Lucee", function() {

			it( "does not report a change when nothing touched the config file", function() {
				expect( variables.ConfigAdmin.wasConfigFileChangedOutside( variables.cs ) ).toBeFalse();
			});

			it( "does not report a change after Lucee wrote the config file itself", function() {
				admin action="getDebug" type="server" password="#variables.adminPassword#" returnVariable="local.debug";
				var maxLogs = val( debug.maxLogs );
				try {
					admin action="updateDebugSetting" type="server" password="#variables.adminPassword#" maxLogs="#maxLogs + 1#";
					expect( variables.ConfigAdmin.wasConfigFileChangedOutside( variables.cs ) ).toBeFalse();
				}
				finally {
					admin action="updateDebugSetting" type="server" password="#variables.adminPassword#" maxLogs="#maxLogs#";
				}
				expect( variables.ConfigAdmin.wasConfigFileChangedOutside( variables.cs ) ).toBeFalse();
			});

			it( title="reports a change when the config file is newer than what Lucee has loaded", body=function() {
				_withModifiedDate( 60000, function() {
					expect( variables.ConfigAdmin.wasConfigFileChangedOutside( variables.cs ) ).toBeTrue();
				});
			});

			it( title="reports a change when the config file is older than what Lucee has loaded", body=function() {
				_withModifiedDate( -60000, function() {
					expect( variables.ConfigAdmin.wasConfigFileChangedOutside( variables.cs ) ).toBeTrue();
				});
			});

		});
	}

	/**
	* simulates an edit from outside by moving the last modified date of the config file,
	* the original date is always restored
	*/
	private void function _withModifiedDate( required numeric offsetMs, required function callback ) {
		var file = variables.cs.getConfigFile();
		var original = file.lastModified();
		try {
			file.setLastModified( variables.cs.configLastModified() + arguments.offsetMs );
			arguments.callback();
		}
		finally {
			file.setLastModified( original );
		}
	}

}
