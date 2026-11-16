component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV6931" {

	function beforeAll() {
		variables.admin = new org.lucee.cfml.Administrator( "server", request.ServerAdminPassword );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6931 updateLoginSettings with missing attributes", function() {

			beforeEach( function( currentSpec ) {
				variables.originalSettings = admin.getLoginSettings();
				// use non default values, so a reset to the defaults can be detected
				admin.updateLoginSettings( loginCaptcha=true, loginDelay=5, loginRememberme=false );
			});

			afterEach( function( currentSpec ) {
				admin.updateLoginSettings( argumentCollection=variables.originalSettings );
			});

			it( "throws when an attribute is missing and keeps the previous values", function() {
				expect( function() {
					cfadmin( action="updateLoginSettings", type="server", password=request.ServerAdminPassword,
						loginCaptcha=false, loginRememberme=true );
				}).toThrow();
				assertUnchanged();
			});

			it( "throws for the old attribute names and keeps the previous values", function() {
				expect( function() {
					cfadmin( action="updateLoginSettings", type="server", password=request.ServerAdminPassword,
						captcha=false, delay=1, rememberme=true );
				}).toThrow();
				assertUnchanged();
			});

		});
	}

	private function assertUnchanged() {
		var settings = admin.getLoginSettings();
		expect( settings.loginCaptcha ).toBeTrue();
		expect( settings.loginDelay ).toBe( 5 );
		expect( settings.loginRememberme ).toBeFalse();
	}
}
