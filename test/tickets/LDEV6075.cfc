component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	variables.esapiExtId = "37C61C0A-5D7E-4256-8572639BE0CF5838";

	// LDEV-6075: a StackOverflowError in the OSGi resolver after the ESAPI extension was changed (downgraded v3 -> v2) and its
	// bundles had to be loaded again.
	// LDEV-6102: installing an extension older than the version the core requires is blocked now, so the downgrade to v2 can
	// not be done anymore (see LDEV6102.cfc for the block). What can still be done, and what this test does, is to install the
	// required ESAPI version again, which deploys and loads its bundles anew, and to call the encoding functions.

	function beforeAll() {
		variables.esapiVersionBefore = getEsapiVersion();
		systemOutput( "Installed ESAPI version: " & variables.esapiVersionBefore, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6075: StackOverflowError after changing the ESAPI extension", function() {

			it( title="Re-install ESAPI and use encoding functions", body=function( currentSpec ) {
				var adminPassword = request.WEBADMINPASSWORD;

				expect( variables.esapiVersionBefore ).notToBeEmpty( "ESAPI extension should be installed" );

				// get the download URL of the installed version
				var detail = LuceeExtension( "org.lucee", "esapi-extension", variables.esapiVersionBefore );
				expect( detail ).toBeStruct();
				expect( detail ).toHaveKey( "lex", "no download URL for ESAPI " & variables.esapiVersionBefore );

				cfhttp( url=detail.lex, result="local.httpResult" );
				expect( httpResult.status_code ).toBe( 200, "Extension download should succeed" );
				systemOutput( "Downloaded " & len( httpResult.fileContent ) & " bytes", true );

				// Install it again, this deploys and loads the bundles of the extension anew
				systemOutput( "Installing ESAPI " & variables.esapiVersionBefore & " again...", true );

				admin action="updateRHExtension"
					type="web"
					password="#adminPassword#"
					source="#httpResult.fileContent#";

				expect( getEsapiVersion() ).toBe( variables.esapiVersionBefore );

				// This is where the StackOverflowError occurred in the buggy version
				// When calling encodeForHTML(), OSGi tries to load bundles and hits the infinite loop
				systemOutput( "Testing ESAPI encoding function (this triggers bundle loading)...", true );

				var testString = "<script>alert('xss')</script>";
				var encoded = encodeForHTML( testString );

				systemOutput( "SUCCESS: encodeForHTML() returned: " & encoded, true );

				expect( encoded ).notToBeEmpty( "Encoding should produce output" );
				expect( encoded ).notToInclude( "<script>", "HTML should be encoded" );
			});

		});
	}

	private string function getEsapiVersion() {
		admin action="getExtensions"
			type="web"
			password="#request.WEBADMINPASSWORD#"
			returnVariable="local.extensions";

		for ( var ext in extensions ) {
			if ( ext.id == variables.esapiExtId ) {
				return ext.version;
			}
		}
		return "";
	}

}
