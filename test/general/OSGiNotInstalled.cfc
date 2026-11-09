component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	function beforeAll() {
		variables.uri = createURI( "OSGiNotInstalled" );
		variables.root = getTempDirectory() & "OSGiNotInstalled-" & createUUID() & "/";
		directoryCreate( root, true );
		variables.adminPw = request.SERVERADMINPASSWORD;
	}

	function afterAll() {
		if ( directoryExists( root ) ) directoryDelete( root, true );
	}

	function run( testResults, testBox ) {
		describe( "OSGi bundles without the OSGi extension", function() {

			it( "OSGi is not available", function() {
				expect( createObject( "java", "lucee.runtime.osgi.OSGi" ).isAvailable() ).toBeFalse();
			});

			it( "createObject java with a bundle throws a clear exception", function() {
				var err = "";
				try {
					createObject( "java", "org.lucee.osgiguard.Missing", "lucee.osgiguard.missing", "1.0.0" );
				}
				catch ( any e ) {
					err = e.message;
				}
				expect( err ).toInclude( "the class [org.lucee.osgiguard.Missing] from the OSGi bundle [lucee.osgiguard.missing:1.0.0]" );
				expect( err ).toInclude( "OSGi is not installed" );
				expect( err ).toInclude( "org.lucee:osgi-extension" );
			});

			it( "a cache defined with a bundle throws a clear exception", function() {
				var res = _InternalRequest( template: uri & "/index.cfm" );
				var err = trim( res.filecontent );
				expect( err ).toInclude( "the class [lucee.osgiguard.Cache] from the OSGi bundle [lucee.osgiguard.cache:1.0.0]" );
				expect( err ).toInclude( "OSGi is not installed" );
			});

			it( "an extension that defines a cache with a bundle is not installed", function() {
				var lex = root & "osgiguard-cache.lex";
				var id = createUUID();
				createLex( lex, id, { "cache": "[{'class':'lucee.osgiguard.Cache','bundleName':'lucee.osgiguard.cache','bundleVersion':'1.0.0'}]" } );
				var err = "";
				try {
					admin action="updateRHExtension" type="server" password=adminPw source=lex;
				}
				catch ( any e ) {
					err = e.message;
				}
				expect( err ).toInclude( "Cannot install the extension [OSGiNotInstalled cache:1.0.0.1]" );
				expect( err ).toInclude( "the class [lucee.osgiguard.Cache] from the OSGi bundle [lucee.osgiguard.cache:1.0.0]" );
				expect( err ).toInclude( "OSGi is not installed" );
				admin action="getRHExtensions" type="server" password=adminPw returnVariable="local.exts";
				expect( queryColumnData( exts, "id" ) ).notToInclude( id );
			});

			it( "an extension with a Maven definition does not need OSGi", function() {
				var lex = root & "osgiguard-maven.lex";
				var id = createUUID();
				createLex( lex, id, { "cache": "[{'class':'lucee.osgiguard.Cache','bundleName':'lucee.osgiguard.cache','maven':'org.lucee:osgiguard:1.0.0'}]" } );
				var err = "";
				try {
					admin action="updateRHExtension" type="server" password=adminPw source=lex;
				}
				catch ( any e ) {
					// the Maven artifact does not exist, that is a different error
					err = e.message;
				}
				finally {
					try { admin action="removeRHExtension" type="server" password=adminPw id=id; } catch ( any e ) {}
				}
				expect( err ).notToInclude( "OSGi is not installed" );
			});

		});
	}

	private function createLex( required string path, required string id, required struct attrs ) {
		var mf = 'Manifest-Version: 1.0#chr( 10 )#id: "#arguments.id#"#chr( 10 )#version: "1.0.0.1"#chr( 10 )#name: "OSGiNotInstalled cache"#chr( 10 )#release-type: server#chr( 10 )#';
		loop struct=arguments.attrs index="local.k" item="local.v" {
			mf &= '#k#: "#v#"#chr( 10 )#';
		}
		mf &= chr( 10 );
		var zos = createObject( "java", "java.util.zip.ZipOutputStream" ).init( createObject( "java", "java.io.FileOutputStream" ).init( arguments.path ) );
		try {
			zos.putNextEntry( createObject( "java", "java.util.zip.ZipEntry" ).init( "META-INF/MANIFEST.MF" ) );
			zos.write( mf.getBytes( "UTF-8" ) );
			zos.closeEntry();
		}
		finally {
			zos.close();
		}
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
