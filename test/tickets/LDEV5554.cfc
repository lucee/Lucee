component extends="org.lucee.cfml.test.LuceeTestCase" labels="logs" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV5554-" & createUUID() & "/";
		directoryCreate( root, true );
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5554: LUCEE_LOGGING_FORCE_APPENDER also applies to out.log and err.log", function() {

			it( "without a forced appender the entries are written to out.log and err.log", function() {
				var res = runServer( "default", "" );
				expect( res.exitCode ).toBe( 0, res.console );
				expect( res.outLog ).toInclude( res.marker & "-global-out", res.console );
				expect( res.errLog ).toInclude( res.marker & "-global-err", res.console );
				expect( res.outLog ).toInclude( res.marker & "-system-out", res.console );
				expect( res.outLog ).toInclude( res.marker & "-writer-out", res.console );
				expect( res.errLog ).toInclude( res.marker & "-system-err", res.console );
				expect( res.errLog ).toInclude( res.marker & "-writer-err", res.console );
			});

			it( "with the console appender forced the entries go to the console and not to out.log and err.log", function() {
				var res = runServer( "console", "console" );
				expect( res.exitCode ).toBe( 0, res.console );
				expect( res.outLog ).notToInclude( res.marker, "out.log: " & res.outLog );
				expect( res.errLog ).notToInclude( res.marker, "err.log: " & res.errLog );
				expect( res.console ).toInclude( res.marker & "-global-out" );
				expect( res.console ).toInclude( res.marker & "-global-err" );
				expect( res.console ).toInclude( res.marker & "-system-out" );
				expect( res.console ).toInclude( res.marker & "-writer-out" );
				expect( res.console ).toInclude( res.marker & "-system-err" );
				expect( res.console ).toInclude( res.marker & "-writer-err" );
			});

		});
	}

	// starts a new Lucee server (fresh server directory) in a separate JVM, executes index.cfm and
	// returns the content of out.log, err.log and the console output of that JVM
	private struct function runServer( required string name, required string forceAppender ) {
		var webroot = root & arguments.name & "/";
		var serverContext = webroot & "WEB-INF/lucee-server/context/";
		var consoleFile = root & arguments.name & ".console";
		var marker = "LDEV5554-" & arguments.name & "-" & createUUID();
		directoryCreate( serverContext, true );
		fileCopy( getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5554/index.cfm", webroot & "index.cfm" );

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
		// the settings are passed as system properties, same as the environment variables of the same name
		var cmd = [ java, "-Dlucee.bundles.dir=" & bundleDir, "-DLDEV5554.marker=" & marker, "-Dlucee.system.log.level=info", "-Dlucee.system.out=log", "-Dlucee.system.err=log", "-cp", arrayToList( cp, File.pathSeparator ), "lucee.cli.CLI", "webroot=" & webroot, "uri=/index.cfm" ];
		if ( len( arguments.forceAppender ) ) arrayInsertAt( cmd, 2, "-Dlucee.logging.force.appender=" & arguments.forceAppender );
		var pb = createObject( "java", "java.lang.ProcessBuilder" ).init( cmd );
		var env = pb.environment();
		// the new server must not pick up settings from the server running the tests
		loop array=[ "LUCEE_ENABLE_WARMUP", "LUCEE_BUILD", "LUCEE_EXTENSIONS", "LUCEE_EXTENSIONS_INSTALL", "LUCEE_BASE_CONFIG", "LUCEE_LOGGING_FORCE_APPENDER", "LUCEE_LOGGING_FORCE_LEVEL", "LUCEE_SYSTEM_OUT", "LUCEE_SYSTEM_ERR", "LUCEE_SYSTEM_LOG_LEVEL" ] item="local.k" {
			env.remove( k );
		}
		pb.directory( File.init( root ) );
		pb.redirectErrorStream( true );
		pb.redirectOutput( File.init( consoleFile ) );

		var process = pb.start();
		var TimeUnit = createObject( "java", "java.util.concurrent.TimeUnit" );
		if ( !process.waitFor( 5, TimeUnit.MINUTES ) ) {
			process.destroyForcibly();
			throw "server [#arguments.name#] did not finish within 5 minutes, output: " & ( fileExists( consoleFile ) ? right( fileRead( consoleFile ), 5000 ) : "" );
		}
		var logDir = serverContext & "logs/";
		return {
			"marker": marker,
			"exitCode": process.exitValue(),
			"outLog": fileExists( logDir & "out.log" ) ? fileRead( logDir & "out.log" ) : "",
			"errLog": fileExists( logDir & "err.log" ) ? fileRead( logDir & "err.log" ) : "",
			"console": fileExists( consoleFile ) ? fileRead( consoleFile ) : ""
		};
	}

	// jar (or class directory) the class of the given object is loaded from
	private string function toPath( required any obj ) {
		return createObject( "java", "java.io.File" ).init( arguments.obj.getClass().getProtectionDomain().getCodeSource().getLocation().toURI() ).getAbsolutePath();
	}
}
