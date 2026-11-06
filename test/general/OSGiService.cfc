component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	function beforeAll() {
		variables.service = getPageContext().getConfig().getFactory().getEngine().getOSGiService();
		variables.dir = getTempDirectory() & "OSGiService" & createUniqueId() & "/";
		directoryCreate( variables.dir );
	}

	function afterAll() {
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( "the OSGiService, OSGi without OSGi types (LDEV-6627)", function() {

			it( "install hands a bundle jar over, a plain jar is not a bundle", function() {
				var name = "lucee.test.install" & createUniqueId();
				var bundle = createJar( variables.dir, name, "1.0.0.1", true );
				var plain = createJar( variables.dir, "plain" & createUniqueId(), "1.0.0.1", false );
				try {
					expect( service.install( createObject( "java", "java.io.File" ).init( bundle ) ) ).toBeTrue();
					expect( service.install( createObject( "java", "java.io.File" ).init( plain ) ) ).toBeFalse();
				}
				finally {
					service.uninstall( createObject( "java", "java.io.File" ).init( bundle ) );
				}
			});

			it( "a bundle that was installed gives its classes loader and resources", function() {
				var name = "lucee.test.resource" & createUniqueId();
				var jar = createJar( variables.dir, name, "1.0.0.1", true, "hello from the bundle" );
				var file = createObject( "java", "java.io.File" ).init( jar );
				try {
					service.install( file );
					expect( isNull( service.getClassLoader( name, "1.0.0.1" ) ) ).toBeFalse();
					var url = service.getResource( name, "1.0.0.1", "/data.txt" );
					expect( isNull( url ) ).toBeFalse();
					var is = service.getResourceAsStream( name, "1.0.0.1", "/data.txt" );
					expect( isNull( is ) ).toBeFalse();
					is.close();
					expect( isNull( service.getResource( name, "1.0.0.1", "/does-not-exist.txt" ) ) ).toBeTrue();
				}
				finally {
					service.uninstall( file );
				}
			});

			it( "uninstall removes what install made available and does nothing for a plain jar", function() {
				var name = "lucee.test.uninstall" & createUniqueId();
				var jar = createJar( variables.dir, name, "1.0.0.1", true );
				var file = createObject( "java", "java.io.File" ).init( jar );
				service.install( file );
				service.uninstall( file );
				expect( isNull( service.getResource( name, "1.0.0.1", "/META-INF/MANIFEST.MF" ) ) ).toBeTrue();
				// nothing to remove
				service.uninstall( file );
				service.uninstall( createObject( "java", "java.io.File" ).init( createJar( variables.dir, "plain" & createUniqueId(), "1.0.0.1", false ) ) );
			});

			it( "has no id and no info for a class that is not from a bundle", function() {
				var clazz = createObject( "java", "java.lang.String" ).getClass();
				expect( isNull( service.getId( clazz ) ) ).toBeTrue();
				expect( isNull( service.getInfo( clazz ) ) ).toBeTrue();
				expect( isNull( service.getId( clazz.getClassLoader() ) ) ).toBeTrue();
			});

			it( "has no id for the classes of Lucee itself", function() {
				expect( isNull( service.getId( createObject( "java", "lucee.runtime.PageContextImpl" ).getClass() ) ) ).toBeTrue();
			});

			it( "findClass finds a class of the core and returns null for an unknown class", function() {
				expect( isNull( service.findClass( "lucee.runtime.PageContext" ) ) ).toBeFalse();
				expect( isNull( service.findClass( "does.not.Exist" ) ) ).toBeTrue();
				expect( isNull( service.findClass( "does.not.Exist", [ "does.not.exist.either" ] ) ) ).toBeTrue();
			});

			it( "throws for a bundle that does not exist", function() {
				expect( function() {
					service.loadClass( "does.not.exist" & createUniqueId(), "1.0.0", "does.not.Exist" );
				} ).toThrow();
			});
		});
	}

	private string function createJar( required string dir, required string name, required string version, required boolean bundle, string data="" ) {
		var src = arguments.dir & arguments.name & "-" & arguments.version & "/";
		directoryCreate( src & "META-INF", true );
		var manifest = "Manifest-Version: 1.0" & chr( 10 );
		if ( arguments.bundle ) manifest &= "Bundle-ManifestVersion: 2" & chr( 10 ) & "Bundle-SymbolicName: " & arguments.name & chr( 10 ) & "Bundle-Version: " & arguments.version & chr( 10 );
		fileWrite( src & "META-INF/MANIFEST.MF", manifest );
		if ( len( arguments.data ) ) fileWrite( src & "data.txt", arguments.data );
		var jar = arguments.dir & arguments.name & "-" & arguments.version & ".jar";
		zip action="zip" file=jar source=src recurse=true;
		return jar;
	}

}
