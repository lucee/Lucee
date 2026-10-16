component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension" {

	// An extension defined only by its id (e.g. lucee.extensions / LUCEE_EXTENSIONS) gets its version later
	// (latest from Maven). The storage name must use that version, otherwise the start-bundles cleanup does not
	// find the extension in the config ("..._null.lex") and uninstalls it again.
	function run( testResults, testBox ) {
		describe( "ExtensionDefintion created from an id without a version", function() {

			it( "uses the version set after the coordinate was resolved for the storage name", function() {
				var cfg = getPageContext().getConfig();
				var ed = createObject( "java", "lucee.runtime.extension.ExtensionDefintion" ).init( "B737ABC4-D43F-4D91-8E8E973E37C40D1B" );

				// resolve the coordinate while the version is still unknown
				var coordinate = ed.getCoordinate( cfg, false );
				expect( isNull( coordinate ) ).toBeFalse();
				expect( coordinate.artifactId ).toBe( "image-extension" );

				// the version gets known later, like DeployHandler.getLatestVersionFor does
				ed.setParam( "version", "3.1.0.11" );

				expect( ed.getStorageName( false ) ).toBe( "mvn_org.lucee_image-extension_3.1.0.11.lex" );
				expect( ed.getCoordinate( cfg, false ).version ).toBe( "3.1.0.11" );
			});

			it( "does not change the shared id to coordinate mapping", function() {
				var cfg = getPageContext().getConfig();
				var ed = createObject( "java", "lucee.runtime.extension.ExtensionDefintion" ).init( "B737ABC4-D43F-4D91-8E8E973E37C40D1B" );
				ed.getCoordinate( cfg, false );
				ed.setParam( "version", "3.1.0.11" );
				ed.getStorageName( false );

				var fresh = createObject( "java", "lucee.runtime.extension.ExtensionDefintion" ).init( "B737ABC4-D43F-4D91-8E8E973E37C40D1B" );
				var c = fresh.getCoordinate( cfg, false );
				expect( isNull( c.version ) || !len( c.version ) ).toBeTrue();
				expect( fresh.getStorageName( false ) ).notToBe( "mvn_org.lucee_image-extension_3.1.0.11.lex" );
			});
		});
	}
}
