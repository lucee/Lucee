component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension,osgi" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV6829-" & createUUID() & "/";
		variables.scriptDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6829/";
		variables.msg = "install the OSGi extension (org.lucee:osgi-extension)";
		directoryCreate( root, true );
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6829: install the OSGi extension automatically when OSGi is needed", function() {

			it( "knows which extensions need OSGi", function() {
				var res = runScenario( "classify", { "ldev6829.osgilex": "true" } );
				expect( res.error ).toBe( "", serializeJSON( res ) );
				expect( res.bundle ).toBeTrue( "an extension that ships a bundle needs OSGi" );
				expect( res.cache ).toBeTrue( "an extension that defines a cache with a bundle class needs OSGi" );
				expect( res.plain ).toBeFalse( "an extension that ships a jar that is not a bundle does not need OSGi" );
				expect( res.class ).toBeFalse( "an extension that defines a datasource without a bundle does not need OSGi" );
				expect( res.osgi ).toBeFalse( "the OSGi extension itself does not need OSGi" );
				expect( res.isOsgiExtension ).toBeTrue();
				expect( res.isOsgiExtensionBundle ).toBeFalse();
				expect( res.message ).toInclude( msg );
				expect( res.installedByEnsure ).toBeFalse( "nothing to install when OSGi is available" );
				expect( res.installed ).toBeEmpty( "nothing must be installed when OSGi is available" );
			});

			it( "installs the OSGi extension (bundled .lex, offline) before an extension that ships bundles", function() {
				var res = runScenario( "extension", { "ldev6829.osgilex": "true" } );
				expect( res.error ).toBe( "", serializeJSON( res ) );
				var installed = res.installed.map( function( e ) { return e.artifactId; } );
				expect( installed ).toBe( [ "osgi-extension", "bundle" ], "the OSGi extension must be installed first: " & serializeJSON( res.installed ) );
				expect( res.installed[ 1 ].modified ).toBeLTE( res.installed[ 2 ].modified );
			});

			it( "does not install the OSGi extension for an extension that does not need OSGi", function() {
				var res = runScenario( "plain", { "ldev6829.osgilex": "true" } );
				expect( res.error ).toBe( "", serializeJSON( res ) );
				var installed = res.installed.map( function( e ) { return e.artifactId; } );
				expect( installed ).toBe( [ "plain" ] );
			});

			it( "installs the OSGi extension when a class definition with a bundle is loaded", function() {
				var res = runScenario( "classdefinition", { "ldev6829.osgilex": "true" } );
				// the bundle of the class definition does not exist, that is a different error
				expect( res.error ).notToBeEmpty( "the bundle does not exist" );
				expect( res.error ).notToInclude( msg, "the OSGi extension is installed, the error is about the missing bundle: " & serializeJSON( res ) );
				var installed = res.installed.map( function( e ) { return e.artifactId; } );
				expect( installed ).toBe( [ "osgi-extension" ] );
			});

			it( "throws an error that tells to install the OSGi extension and installs nothing when it cannot be resolved", function() {
				// no .lex and no download possible
				var res = runScenario( "unresolved", {
					"ldev6829.osgilex": "false",
					"lucee.maven.download.policy.startup": "error",
					"lucee.maven.download.policy.runtime": "error"
				} );
				expect( res.error ).toInclude( msg, serializeJSON( res ) );
				expect( res.installed ).toBeEmpty( "nothing must be installed, not even the extension that needs OSGi: " & serializeJSON( res ) );
			});

			it( "does not install the OSGi extension when the automatic installation is disabled", function() {
				var res = runScenario( "optout", { "ldev6829.osgilex": "true", "lucee.osgi.autoinstall": "false" } );
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
		fileCopy( variables.scriptDir & "Lex.cfc", webroot & "Lex.cfc" );

		// the OSGi extension as .lex in the extension folder (the same place the bundled .lex files are extracted to), this is the offline source
		if ( ( arguments.props[ "ldev6829.osgilex" ] ?: "" ) == "true" ) {
			// it ships a plain jar (no OSGi bundle), it has to load without OSGi
			new LDEV6829.Lex().createLex( webroot & "WEB-INF/lucee-server/context/extensions/available/mvn_org.lucee_osgi-extension_1.0.0.1.lex", "org.lucee", "osgi-extension", "LDEV6829 OSGi", createUUID(), "1.0.0.1", {}, { "jars/osgi-impl.jar": new LDEV6829.Lex().createJar( false ) } );
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
		var cmd = [ java, "-Dlucee.bundles.dir=" & bundleDir, "-Dldev6829.scenario=" & arguments.name, "-Dldev6829.result=" & resultFile, "-Dldev6829.dir=" & work ];
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
}
