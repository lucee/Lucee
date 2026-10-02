component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function run( testResults, testBox ) {
		describe( "Test suite for LDEV-6486 (Require-Extension since for mail/ftp)", function() {

			it( title="RHExtension parses since= on a GAV Require-Extension entry", body=function( currentSpec ) {
				var RHExtension = createObject( "java", "lucee.runtime.extension.RHExtension" );
				var ed = RHExtension.toExtensionDefinition( "org.lucee:mail-extension:1.1.0.8-RC;since=7.1.0.0" );
				expect( ed.getSince() ).notToBeNull();
				expect( ed.getSince().toString() ).toBe( "7.1.0.0" );
			});

			it( title="mail-extension Require-Extension entry declares since=7.1.0.0", body=function( currentSpec ) {
				var ed = findRequired( "mail-extension" );
				expect( ed ).notToBeNull( "mail-extension missing from Require-Extension" );
				expect( ed.getSince() ).notToBeNull( "mail-extension has no since= (7.0->7.1 minor update will not install it)" );
				expect( ed.getSince().toString() ).toBe( "7.1.0.0" );
			});

			it( title="ftp-extension Require-Extension entry declares since=7.1.0.0", body=function( currentSpec ) {
				var ed = findRequired( "ftp-extension" );
				expect( ed ).notToBeNull( "ftp-extension missing from Require-Extension" );
				expect( ed.getSince() ).notToBeNull( "ftp-extension has no since=" );
				expect( ed.getSince().toString() ).toBe( "7.1.0.0" );
			});

		});
	}

	private any function findRequired( required string artifactId ) {
		var info = getPageContext().getConfig().getFactory().getEngine().getInfo();
		var it = info.getRequiredExtension().iterator();
		while ( it.hasNext() ) {
			var ed = it.next();
			if ( ( ed.getArtifactId() ?: "" ) == arguments.artifactId ) return ed;
		}
		return javaCast( "null", "" );
	}
}
