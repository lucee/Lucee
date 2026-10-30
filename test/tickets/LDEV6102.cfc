component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi,extension" {

	variables.esapiExtId = "37C61C0A-5D7E-4256-8572639BE0CF5838";
	// far older than the ESAPI version the core requires (Require-Extension in the manifest)
	variables.oldEsapiVersion = "2.6.0.1";

	function beforeAll() {
		variables.esapiVersionBefore = getEsapiVersion();

		admin action="getRHExtensionProviders"
			type="web"
			password="#request.WEBADMINPASSWORD#"
			returnVariable="local.providers";
		variables.providerURL = providers.url[ 1 ];
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6102: block installing an extension older than the version required by the core", function() {

			it( title="installing an ESAPI version older than the required one is rejected", skip=isConfigOnly(), body=function( currentSpec ) {
				expect( variables.esapiVersionBefore ).notToBeEmpty( "ESAPI extension should be installed" );

				var downloadURL = variables.providerURL & "/rest/extension/provider/full/" & variables.esapiExtId &
								  "?type=all&coreVersion=" & server.lucee.version & "&version=" & variables.oldEsapiVersion;

				cfhttp( url=downloadURL, result="local.httpResult" ) {
					cfhttpparam( type="header", name="accept", value="application/cfml" );
				}
				expect( httpResult.status_code ).toBe( 200, "Extension download should succeed" );

				expect( function() {
					admin action="updateRHExtension"
						type="web"
						password="#request.WEBADMINPASSWORD#"
						source="#httpResult.fileContent#";
				}).toThrow( regex="version \[" & replace( variables.oldEsapiVersion, ".", "\.", "all" ) & "\] is older than the required minimum version \[[0-9.]+\]" );

				// the installed version did not change and the extension still works
				expect( getEsapiVersion() ).toBe( variables.esapiVersionBefore );
				expect( encodeForHTML( "<b>" ) ).notToInclude( "<b>" );
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

	// with lucee.extensions.config.only the Require-Extension entries, and so the block, are ignored
	private boolean function isConfigOnly() {
		var prop = server.system.properties[ "lucee.extensions.config.only" ] ?: ( server.system.environment[ "LUCEE_EXTENSIONS_CONFIG_ONLY" ] ?: false );
		return isBoolean( prop ) && prop;
	}

}
