component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi,extension" {

	function beforeAll() {
		variables.adminPw = request.SERVERADMINPASSWORD;
		variables.service = getPageContext().getConfig().getFactory().getEngine().getOSGiService();
		variables.lex = new LDEV6829.Lex();
		variables.dir = getTempDirectory() & "LDEV6627" & createUniqueId() & "/";
		directoryCreate( variables.dir );
		variables.installed = [];
	}

	function afterAll() {
		for ( var id in variables.installed ) {
			try {
				admin action="removeRHExtension" type="server" password=variables.adminPw id=id;
			}
			catch ( any e ) {
			}
		}
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6627: removing an extension only removes the bundles nobody else needs", function() {

			// TODO LDEV-6627: needs the OSGi extension, skipped as long as OSGi is not installed.
			it( title="an upgrade keeps the bundles the new version contains as well", skip=!server._hasOSGi(), body=function() {
				var bundle = "ldev6627.upgrade" & createUniqueId();
				var other = "ldev6627.upgradeold" & createUniqueId();
				var id = createUUID();
				var v1 = installLex( id, "1.0.0.1", { "bundles/#bundle#-1.0.0.jar": bundleJar( bundle, "1.0.0" ), "bundles/#other#-1.0.0.jar": bundleJar( other, "1.0.0" ) } );
				expect( isLoadable( bundle, "1.0.0" ) ).toBeTrue();
				expect( isLoadable( other, "1.0.0" ) ).toBeTrue();
				var loader = service.getClassLoader( bundle, "1.0.0" );

				installLex( id, "1.0.0.2", { "bundles/#bundle#-1.0.0.jar": bundleJar( bundle, "1.0.0" ) } );

				// the bundle in both versions stays, the one only the old version had is gone
				expect( isLoadable( bundle, "1.0.0" ) ).toBeTrue( "the bundle of the new version was removed with the old version" );
				expect( service.getClassLoader( bundle, "1.0.0" ) ).toBe( loader );
				expect( isLoadable( other, "1.0.0" ) ).toBeFalse( "the bundle only the old version had is still there" );
			});

			it( title="a bundle two extensions ship under different file names stays until both are removed", skip=!server._hasOSGi(), body=function() {
				var bundle = "ldev6627.shared" & createUniqueId();
				var id1 = createUUID();
				var id2 = createUUID();
				installLex( id1, "1.0.0.1", { "bundles/first-#bundle#.jar": bundleJar( bundle, "1.0.0" ) } );
				installLex( id2, "1.0.0.1", { "bundles/second.jar": bundleJar( bundle, "1.0.0", "other bytes" ) } );
				expect( isLoadable( bundle, "1.0.0" ) ).toBeTrue();

				admin action="removeRHExtension" type="server" password=variables.adminPw id=id1;
				expect( isLoadable( bundle, "1.0.0" ) ).toBeTrue( "the bundle the second extension needs was removed" );

				admin action="removeRHExtension" type="server" password=variables.adminPw id=id2;
				expect( isLoadable( bundle, "1.0.0" ) ).toBeFalse( "the bundle nobody needs is still there" );
			});

		});
	}

	private function installLex( required string id, required string version, required struct files ) {
		var path = variables.dir & "ext-" & arguments.id & "-" & arguments.version & ".lex";
		variables.lex.createLex( path, "org.lucee", "ldev6627-" & arguments.id, "LDEV6627 " & arguments.id, arguments.id, arguments.version, {}, arguments.files );
		admin action="updateRHExtension" type="server" password=variables.adminPw source=path;
		if ( !arrayFind( variables.installed, arguments.id ) ) arrayAppend( variables.installed, arguments.id );
	}

	private boolean function isLoadable( required string name, required string version ) {
		try {
			return !isNull( service.getResource( arguments.name, arguments.version, "data.txt" ) );
		}
		catch ( any e ) {
			return false;
		}
	}

	private any function bundleJar( required string name, required string version, string data="data" ) {
		var bos = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
		var zos = createObject( "java", "java.util.zip.ZipOutputStream" ).init( bos );
		var mf = "Manifest-Version: 1.0" & chr( 10 ) & "Bundle-ManifestVersion: 2" & chr( 10 ) & "Bundle-SymbolicName: " & arguments.name & chr( 10 ) & "Bundle-Version: " & arguments.version & chr( 10 ) & chr( 10 );
		variables.lex.addEntry( zos, "META-INF/MANIFEST.MF", mf.getBytes( "UTF-8" ) );
		variables.lex.addEntry( zos, "data.txt", arguments.data.getBytes( "UTF-8" ) );
		zos.close();
		return bos.toByteArray();
	}

}
