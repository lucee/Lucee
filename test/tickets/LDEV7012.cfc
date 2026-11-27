component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension,osgi" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV7012-" & createUUID() & "/";
		variables.scriptDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV7012/";
		variables.msg = "install the OSGi extension (org.lucee:osgi-extension)";
		directoryCreate( root, true );
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-7012: install the OSGi extension automatically when OSGi is needed and missing", function() {

			it( "knows which extensions need OSGi", function() {
				var res = runScenario( "classify", { "ldev7012.osgilex": "true" } );
				expect( res.error ).toBe( "", serializeJSON( res ) );
				expect( res.bundle ).toBeTrue( "an extension that ships a bundle needs OSGi" );
				expect( res.cache ).toBeTrue( "an extension that defines a cache with a bundle class needs OSGi" );
				expect( res.plain ).toBeFalse( "an extension that ships a jar that is not a bundle does not need OSGi" );
				expect( res.class ).toBeFalse( "an extension that defines a datasource without a bundle does not need OSGi" );
				expect( res.osgi ).toBeFalse( "the OSGi extension itself does not need OSGi" );
				expect( res.isOsgiExtension ).toBeTrue();
				expect( res.isOsgiExtensionBundle ).toBeFalse();
				expect( res.message ).toInclude( msg );
			});

			it( "installs the OSGi extension (local copy, offline) before an extension that ships bundles", function() {
				var res = runScenario( "extension", { "ldev7012.osgilex": "true" } );
				expect( res.osgiAtStart ).toBeFalse( "the server must start without OSGi: " & serializeJSON( res ) );
				expect( res.error ).toBe( "", serializeJSON( res ) );
				var installed = res.installed.map( function( e ) { return e.artifactId; } );
				expect( installed ).toBe( [ "osgi-extension", "bundle" ], "the OSGi extension must be installed first: " & serializeJSON( res.installed ) );
				expect( res.osgiAtEnd ).toBeTrue( "OSGi is available without a restart" );
			});

			it( "does not install the OSGi extension for an extension that does not need OSGi", function() {
				var res = runScenario( "plain", { "ldev7012.osgilex": "true" } );
				expect( res.error ).toBe( "", serializeJSON( res ) );
				var installed = res.installed.map( function( e ) { return e.artifactId; } );
				expect( installed ).toBe( [ "plain" ] );
			});

			it( "installs the OSGi extension when a class definition with a bundle is loaded", function() {
				var res = runScenario( "classdefinition", { "ldev7012.osgilex": "true" } );
				// the bundle of the class definition does not exist, that is a different error
				expect( res.error ).notToBeEmpty( "the bundle does not exist" );
				expect( res.error ).notToInclude( "OSGi is needed", "the OSGi extension is installed, the error is about the missing bundle: " & serializeJSON( res ) );
				var installed = res.installed.map( function( e ) { return e.artifactId; } );
				expect( installed ).toBe( [ "osgi-extension" ] );
			});

			it( "throws an error that tells to install the OSGi extension and installs nothing when it cannot be resolved", function() {
				// no .lex and no download possible
				var res = runScenario( "unresolved", {
					"ldev7012.osgilex": "false",
					"lucee.maven.local.repository": root & "empty-repo",
					"lucee.maven.download.policy.startup": "error",
					"lucee.maven.download.policy.runtime": "error"
				} );
				// the jar can contain the OSGi extension or the build can reach it offline, then it is resolvable and installed, there is nothing to test
				if ( !len( res.error ) ) skip( "the OSGi extension can be resolved on this build: " & serializeJSON( res ) );
				expect( res.error ).toInclude( msg, serializeJSON( res ) );
				expect( res.installed ).toBeEmpty( "nothing must be installed, not even the extension that needs OSGi: " & serializeJSON( res ) );
			});

			it( "does not install the OSGi extension when the automatic installation is disabled", function() {
				var res = runScenario( "optout", { "ldev7012.osgilex": "true", "lucee.osgi.autoinstall": "false" } );
				expect( res.error ).toInclude( msg, serializeJSON( res ) );
				expect( res.error ).toInclude( "lucee.osgi.autoinstall" );
				expect( res.installed ).toBeEmpty( "nothing must be installed: " & serializeJSON( res ) );
			});

		});
	}

	// starts a new Lucee server (fresh server directory) in a separate JVM and executes the scenario in it
	private struct function runScenario( required string name, required struct props ) {
		var webroot = root & arguments.name & "/";
		var work = root & arguments.name & "-work/";
		var resultFile = root & arguments.name & ".json";
		var logFile = root & arguments.name & ".log";
		directoryCreate( webroot & "WEB-INF/lucee-server/context", true );
		fileCopy( variables.scriptDir & "index.cfm", webroot & "index.cfm" );
		fileCopy( getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6829/Lex.cfc", webroot & "Lex.cfc" );

		// the OSGi extension of this server as .lex in the extension folder (the same place the bundled .lex files are extracted to), this is the offline source
		var osgiLex = osgiLexOfThisServer();
		if ( ( arguments.props[ "ldev7012.osgilex" ] ?: "" ) == "true" ) {
			directoryCreate( webroot & "WEB-INF/lucee-server/context/extensions/available", true, true );
			fileCopy( osgiLex, webroot & "WEB-INF/lucee-server/context/extensions/available/" & getFileFromPath( osgiLex ) );
		}

		var System = createObject( "java", "java.lang.System" );
		var File = createObject( "java", "java.io.File" );

		var java = System.getProperty( "java.home" ) & "/bin/java";
		if ( !fileExists( java ) ) java &= ".exe";

		// classpath of the running JVM (loader and its dependencies), make sure the Lucee loader, servlet and jsp api are part of it
		var cp = listToArray( System.getProperty( "java.class.path" ), File.pathSeparator );
		loop array=[
			createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance().getCFMLEngineFactory(),
			createObject( "java", "jakarta.servlet.ServletException" ).init( "" ),
			createObject( "java", "jakarta.servlet.jsp.JspException" ).init( "" )
		] item="local.obj" {
			var path = toPath( obj );
			if ( !arrayContains( cp, path ) ) arrayAppend( cp, path );
		}

		// use the OSGi bundles of the running server, the new server can not necessarily download them
		var bundleDir = createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance().getCFMLEngineFactory().getBundleDirectory().getAbsolutePath();
		var cmd = [ java, "-Dlucee.bundles.dir=" & bundleDir, "-Dldev7012.scenario=" & arguments.name, "-Dldev7012.result=" & resultFile, "-Dldev7012.dir=" & work, "-Dldev7012.osgilexfile=" & osgiLex, "-Dlucee.extensions.config.only=true" ];
		loop struct=arguments.props index="local.k" item="local.v" {
			arrayAppend( cmd, "-D" & k & "=" & v );
		}
		arrayAppend( cmd, [ "-cp", arrayToList( cp, File.pathSeparator ), "lucee.cli.CLI", "webroot=" & webroot, "uri=/index.cfm" ], true );
		var pb = createObject( "java", "java.lang.ProcessBuilder" ).init( cmd );
		var penv = pb.environment();
		// the new server must not pick up settings from the server running the tests
		loop array=[ "LUCEE_ENABLE_WARMUP", "LUCEE_BUILD", "LUCEE_EXTENSIONS", "LUCEE_EXTENSIONS_INSTALL", "LUCEE_BASE_CONFIG", "LUCEE_OSGI_AUTOINSTALL", "LUCEE_MAVEN_DOWNLOAD_POLICY_STARTUP", "LUCEE_MAVEN_DOWNLOAD_POLICY_RUNTIME" ] item="local.k" {
			penv.remove( k );
		}
		pb.directory( File.init( root ) );
		pb.redirectErrorStream( true );
		pb.redirectOutput( File.init( logFile ) );

		var process = pb.start();
		var TimeUnit = createObject( "java", "java.util.concurrent.TimeUnit" );
		if ( !process.waitFor( 5, TimeUnit.MINUTES ) ) {
			process.destroyForcibly();
			throw "scenario [#arguments.name#] did not finish within 5 minutes, output: " & ( fileExists( logFile ) ? right( fileRead( logFile ), 5000 ) : "" );
		}
		var log = fileExists( logFile ) ? fileRead( logFile ) : "";
		expect( process.exitValue() ).toBe( 0, log );
		expect( fileExists( resultFile ) ).toBeTrue( "the scenario [#arguments.name#] did not write a result: " & log );
		return deserializeJSON( fileRead( resultFile ) );
	}

	// jar (or class directory) the class of the given object is loaded from
	private string function toPath( required any obj ) {
		return createObject( "java", "java.io.File" ).init( arguments.obj.getClass().getProtectionDomain().getCodeSource().getLocation().toURI() ).getAbsolutePath();
	}

	private string function osgiLexOfThisServer() {
		var cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
		var files = directoryList( cs.getExtensionInstalledDir().getAbsolutePath(), false, "path", "*osgi-extension*.lex" );
		expect( files ).notToBeEmpty( "the OSGi extension is not installed in the server that runs the test" );
		return files[ arrayLen( files ) ];
	}

}
