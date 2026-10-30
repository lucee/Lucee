component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven,extension" {

	function beforeAll() {
		// the fake org.lucee artifacts served by the local repository have no Lucee signature (LDEV-1554)
		variables.sigProp = "lucee.maven.signature.verification";
		variables.orgSigPolicy = createObject( "java", "java.lang.System" ).getProperty( variables.sigProp );
		createObject( "java", "java.lang.System" ).setProperty( variables.sigProp, "off" );
		createObject( "java", "lucee.runtime.config.ConfigServerImpl" ).instance.resetMavenSignatureVerification();
		variables.root = getTempDirectory() & "LDEV6523-" & createUUID() & "/";
		variables.repo = root & "repo/";
		variables.cache = root & "cache/";
		directoryCreate( repo, true );
		directoryCreate( cache, true );

		// an extension that (wrongly) also has a .lco next to the .lex, it must not be looked up
		addArtifact( "ldev6523-extension", "1.0.0.1", [ "lex", "pom", "lco" ] );
		// the core still gets the pom and the lco
		addArtifact( "lucee", "7.1.99.6523", [ "jar", "pom", "lco" ] );
		// a real zip as .lex, the pom has to stay the pom
		addArtifact( "ldev6523-pom-extension", "1.0.0.2", [ "lex", "pom" ] );
		addArtifact( "ldev6523-nopom-extension", "1.0.0.3", [ "lex" ] );

		var OutputLevel = createObject( "java", "com.sun.net.httpserver.SimpleFileServer$OutputLevel" );
		var SimpleFileServer = createObject( "java", "com.sun.net.httpserver.SimpleFileServer" );
		var addr = createObject( "java", "java.net.InetSocketAddress" ).init( createObject( "java", "java.net.InetAddress" ).getLoopbackAddress(), 0 );
		variables.requestLog = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
		variables.httpServer = createObject( "java", "com.sun.net.httpserver.HttpServer" ).create( addr, 0 );
		var ctx = httpServer.createContext( "/", SimpleFileServer.createFileHandler( createObject( "java", "java.io.File" ).init( root ).toPath() ) );
		ctx.getFilters().add( SimpleFileServer.createOutputFilter( requestLog, OutputLevel.INFO ) );
		httpServer.start();
		variables.baseUrl = "http://127.0.0.1:" & httpServer.getAddress().getPort() & "/";
	}

	function afterAll() {
		if ( isNull( variables.orgSigPolicy ) ) createObject( "java", "java.lang.System" ).clearProperty( variables.sigProp );
		else createObject( "java", "java.lang.System" ).setProperty( variables.sigProp, variables.orgSigPolicy );
		createObject( "java", "lucee.runtime.config.ConfigServerImpl" ).instance.resetMavenSignatureVerification();
		if ( !isNull( variables.httpServer ) ) httpServer.stop( 0 );
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6523: installing an extension from Maven does no unneeded requests and keeps the cached .lex", function() {

			it( "does not look for a .lco or a .pom of an extension", function() {
				requestLog.reset();
				var detail = provider( "ldev6523-extension" ).detail( toVersion( "1.0.0.1" ), "lex", true );
				expect( detail[ "lex" ] ).toBe( baseUrl & "repo/org/lucee/ldev6523-extension/1.0.0.1/ldev6523-extension-1.0.0.1.lex" );
				expect( detail ).notToHaveKey( "lco" );
				expect( detail ).notToHaveKey( "pom" );
				var requests = loggedRequests();
				expect( requests ).toInclude( "ldev6523-extension-1.0.0.1.lex" );
				expect( requests ).notToInclude( ".lco" );
				expect( requests ).notToInclude( ".pom" );
			});

			it( "still looks for the .pom and the .lco of the core", function() {
				var detail = provider( "lucee" ).detail( toVersion( "7.1.99.6523" ), "jar", true );
				expect( detail ).toHaveKey( "jar" );
				expect( detail[ "pom" ] ).toBe( baseUrl & "repo/org/lucee/lucee/7.1.99.6523/lucee-7.1.99.6523.pom" );
				expect( detail[ "lco" ] ).toBe( baseUrl & "repo/org/lucee/lucee/7.1.99.6523/lucee-7.1.99.6523.lco" );
			});

			it( "the cached .pom of an extension is the pom and not the .lex", function() {
				var config = getPageContext().getConfig();
				var POM = createObject( "java", "lucee.runtime.mvn.POM" );
				var artifactDir = POM.localArtifactDir( config.getMavenDir(), "org.lucee", "ldev6523-pom-extension" );
				if ( artifactDir.isDirectory() ) artifactDir.remove( true );
				try {
					var ep = extensionProvider();
					var pom = ep.getPOMResource( config, "ldev6523-pom-extension", toVersion( "1.0.0.2" ) );
					expect( pom.getName() ).toBe( "ldev6523-pom-extension-1.0.0.2.pom" );
					expect( readStart( pom, 2 ) ).notToBe( "PK" );
					expect( trim( toString( pom.getInputStream().readAllBytes(), "UTF-8" ) ) ).toBe( pomContent( "ldev6523-pom-extension", "1.0.0.2" ) );
					expect( isXML( toString( pom.getInputStream().readAllBytes(), "UTF-8" ) ) ).toBeTrue();

					var lex = ep.getLEXResource( config, "ldev6523-pom-extension", toVersion( "1.0.0.2" ) );
					expect( readStart( lex, 2 ) ).toBe( "PK" );
				}
				finally {
					if ( artifactDir.isDirectory() ) artifactDir.remove( true );
				}
			});

			it( "an extension without a .pom gets no .pom written to the local Maven repository", function() {
				var config = getPageContext().getConfig();
				var POM = createObject( "java", "lucee.runtime.mvn.POM" );
				var artifactDir = POM.localArtifactDir( config.getMavenDir(), "org.lucee", "ldev6523-nopom-extension" );
				if ( artifactDir.isDirectory() ) artifactDir.remove( true );
				try {
					var ep = extensionProvider();
					expect( isNull( ep.getPOMResource( config, "ldev6523-nopom-extension", toVersion( "1.0.0.3" ), javaCast( "null", "" ) ) ) ).toBeTrue();
					var pom = artifactDir.getRealResource( "1.0.0.3/ldev6523-nopom-extension-1.0.0.3.pom" );
					expect( pom.exists() ).toBeFalse();
				}
				finally {
					if ( artifactDir.isDirectory() ) artifactDir.remove( true );
				}
			});

			it( "an extension installed from the local Maven repository stays in the repository", function() {
				var id = createUUID();
				var artifact = "ldev6523-install-" & lCase( left( replace( id, "-", "", "all" ), 12 ) );
				var version = "1.0.0." & randRange( 1, 99999 );
				var config = getPageContext().getConfig();
				var POM = createObject( "java", "lucee.runtime.mvn.POM" );
				var lex = POM.local( config.getMavenDir(), "org.lucee", artifact, version, "lex" );
				var artifactDir = lex.getParentResource().getParentResource();
				createExtension( lex, id, version, artifact );
				fileWrite( POM.local( config.getMavenDir(), "org.lucee", artifact, version, "pom" ).getAbsolutePath(),
					"<project><modelVersion>4.0.0</modelVersion><groupId>org.lucee</groupId><artifactId>#artifact#</artifactId><version>#version#</version><packaging>lex</packaging></project>" );
				var lexSize = lex.length();

				var ed = createObject( "java", "lucee.runtime.extension.ExtensionDefintion" ).init( id, version );
				ed.setCoordinate( createObject( "java", "lucee.runtime.mvn.MavenUtil$MavenCoordinate" ).init( "org.lucee", artifact, version ) );
				var filter = createObject( "java", "lucee.runtime.config.ResetFilter" ).init();
				var rhe = "";
				try {
					rhe = createObject( "java", "lucee.runtime.config.DeployHandler" )
						.deployExtension( config, ed, filter, javaCast( "null", "" ), true, true, true, javaCast( "null", "" ) );
					filter.resetThrowPageException( config );
					expect( rhe.getId() ).toBe( id );
					expect( isInstalled( id ) ).toBeTrue();
					expect( lex.isFile() ).toBeTrue( "the cached .lex was removed from the local Maven repository by the install" );
					expect( lex.length() ).toBe( lexSize );
				}
				finally {
					if ( isInstalled( id ) ) {
						// the extension is not available from any provider, so remove the installed instance directly
						var cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( config );
						var ca = createObject( "java", "lucee.runtime.config.ConfigAdmin" ).newInstance( cs, javaCast( "null", "" ), true );
						var installedEd = createObject( "java", "lucee.runtime.extension.ExtensionDefintion" ).init( id, version );
						if ( !isSimpleValue( rhe ) ) installedEd.setSource( rhe );
						ca.removeRHExtension( installedEd, cs.getLog( "deploy" ) );
						ca.store();
						cs.resetExtensionDefinitions().resetRHExtensions();
					}
					if ( artifactDir.isDirectory() ) artifactDir.remove( true );
				}
				expect( isInstalled( id ) ).toBeFalse();
			});
		});
	}

	/**
	 * the output filter logs a request only after the response was sent, so the client can be faster than the log.
	 * the server handles one exchange after the other (no executor), so once a last request is answered,
	 * all the requests before it are logged.
	 */
	private string function loggedRequests() {
		var conn = createObject( "java", "java.net.URL" ).init( baseUrl & "ldev6523-sync" ).openConnection();
		try {
			conn.setRequestMethod( "HEAD" );
			conn.getResponseCode();
		}
		finally {
			conn.disconnect();
		}
		return requestLog.toString( "UTF-8" );
	}

	private function addArtifact( required string artifact, required string version, required array types ) {
		var dir = repo & "org/lucee/" & artifact & "/" & version & "/";
		directoryCreate( dir, true );
		for ( var type in types ) {
			var file = dir & artifact & "-" & version & "." & type;
			if ( type == "pom" ) fileWrite( file, pomContent( artifact, version ) );
			else if ( type == "lex" ) {
				var src = root & "lexsrc-" & createUUID() & "/";
				directoryCreate( src & "META-INF", true );
				fileWrite( src & "META-INF/MANIFEST.MF", "Manifest-Version: 1.0#chr(10)#name: #artifact##chr(10)#" );
				compress( "zip", src, file, false );
				directoryDelete( src, true );
			}
			else fileWrite( file, artifact & ":" & type );
		}
	}

	private string function pomContent( required string artifact, required string version ) {
		return "<project><groupId>org.lucee</groupId><artifactId>#arguments.artifact#</artifactId><version>#arguments.version#</version><packaging>lex</packaging></project>";
	}

	private function createExtension( required lex, required string id, required string version, required string name ) {
		var manifest = createObject( "java", "java.util.jar.Manifest" ).init();
		var attrs = manifest.getMainAttributes();
		attrs.putValue( "Manifest-Version", "1.0" );
		attrs.putValue( "id", '"' & id & '"' );
		attrs.putValue( "version", '"' & version & '"' );
		attrs.putValue( "name", '"' & name & '"' );
		attrs.putValue( "description", '"LDEV-6523 test extension"' );
		var out = createObject( "java", "java.util.jar.JarOutputStream" ).init( createObject( "java", "java.io.FileOutputStream" ).init( lex.getAbsolutePath() ), manifest );
		out.close();
	}

	private boolean function isInstalled( required string id ) {
		var target = arguments.id;
		admin action="getRHExtensions" type="server" password=request.SERVERADMINPASSWORD returnVariable="local.exts";
		return queryFilter( local.exts, function( row ) { return row.id == target; } ).recordCount > 0;
	}

	private function repositories() {
		var Repository = createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider$Repository" );
		// no caching, every call asks the repository
		var r = Repository.init( "LDEV-6523", baseUrl & "repo/", Repository.TYPE_ALL, Repository.TIMEOUT_ZERO, Repository.TIMEOUT_ZERO, getPageContext().getConfig().getResource( cache ) );
		var repos = createObject( "java", "java.lang.reflect.Array" ).newInstance( r.getClass(), 1 );
		repos[ 1 ] = r;
		return repos;
	}

	private function provider( required string artifact ) {
		return createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider" ).init( repositories(), "org.lucee", arguments.artifact );
	}

	// the constructor of the ExtensionProvider that takes the repositories is private
	private function extensionProvider() {
		var repos = repositories();
		var ConfigClass = createObject( "java", "java.lang.Class" ).forName( "lucee.runtime.config.Config", false, repos.getClass().getComponentType().getClassLoader() );
		var types = createObject( "java", "java.lang.reflect.Array" ).newInstance( createObject( "java", "java.lang.Class" ).getClass(), 3 );
		types[ 1 ] = ConfigClass;
		types[ 2 ] = createObject( "java", "java.lang.String" ).getClass();
		types[ 3 ] = repos.getClass();
		var c = repos.getClass().getComponentType().getClassLoader().loadClass( "lucee.runtime.config.maven.ExtensionProvider" ).getDeclaredConstructor( types );
		c.setAccessible( true );
		return c.newInstance( [ getPageContext().getConfig(), "org.lucee", repos ] );
	}

	private string function readStart( required file, required numeric len ) {
		var is = arguments.file.getInputStream();
		try {
			var bytes = is.readNBytes( arguments.len );
			return toString( bytes, "ISO-8859-1" );
		}
		finally {
			is.close();
		}
	}

	private function toVersion( required string version ) {
		return createObject( "java", "lucee.runtime.config.maven.Version" ).parseVersion( version );
	}
}
