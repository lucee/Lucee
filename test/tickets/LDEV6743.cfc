component extends="org.lucee.cfml.test.LuceeTestCase" labels="warmup" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV6743-" & createUUID() & "/";
		variables.webroot = root & "webroot/";
		variables.resultFile = root & "onBuild.json";
		variables.logFile = root & "process.log";
		directoryCreate( webroot & "WEB-INF/lucee-server/context/context", true );
		fileWrite( webroot & "index.cfm", "" );

		// Server.cfc of the new server, onBuild() reports what is available at that point
		var cfc = 'component {
	function onBuild() {
		var installed = directoryList( "#webroot#WEB-INF/lucee-server/context/extensions/installed", false, "name", "*.lex" );
		fileWrite( "#resultFile#", serializeJSON( {
			"registered": extensionList().recordCount,
			"installed": arrayLen( installed )
		} ) );
	}
}';
		fileWrite( webroot & "WEB-INF/lucee-server/context/context/Server.cfc", cfc );
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6743: warmup, onBuild() is called after the extensions are installed and registered", function() {

			it( "onBuild() sees all installed extensions", function() {
				var exitCode = runWarmup();
				var log = fileExists( logFile ) ? fileRead( logFile ) : "";
				expect( exitCode ).toBe( 0, log );
				expect( fileExists( resultFile ) ).toBeTrue( "onBuild() was not executed: " & log );

				var result = deserializeJSON( fileRead( resultFile ) );
				expect( result.installed ).toBeGT( 0, "no extension installed at onBuild()" );
				expect( result.registered ).toBe( result.installed, "extensions registered at onBuild() [#result.registered#], installed [#result.installed#]" );
			});

		});
	}

	// starts a new Lucee server (fresh server directory) in a separate JVM with warmup enabled
	private numeric function runWarmup() {
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
		var cmd = [ java, "-Dlucee.enable.warmup=true", "-Dlucee.bundles.dir=" & bundleDir, "-cp", arrayToList( cp, File.pathSeparator ), "lucee.cli.CLI", "webroot=" & webroot, "uri=/index.cfm" ];
		var pb = createObject( "java", "java.lang.ProcessBuilder" ).init( cmd );
		var env = pb.environment();
		// the new server must not pick up settings from the server running the tests
		loop array=[ "LUCEE_ENABLE_WARMUP", "LUCEE_BUILD", "LUCEE_EXTENSIONS", "LUCEE_EXTENSIONS_INSTALL", "LUCEE_BASE_CONFIG" ] item="local.k" {
			env.remove( k );
		}
		pb.directory( File.init( root ) );
		pb.redirectErrorStream( true );
		pb.redirectOutput( File.init( logFile ) );

		var process = pb.start();
		var TimeUnit = createObject( "java", "java.util.concurrent.TimeUnit" );
		if ( !process.waitFor( 5, TimeUnit.MINUTES ) ) {
			// LDEV-6743 report where the server hangs
			var threads = threadDump( process, logFile );
			process.destroyForcibly();
			throw "warmup did not finish within 5 minutes, output: " & ( fileExists( logFile ) ? right( fileRead( logFile ), 5000 ) : "" ) & chr( 10 ) & "threads: " & threads;
		}
		return process.exitValue();
	}

	// thread dump of a process that does not finish
	private string function threadDump( required any process, required string logFile ) {
		var jcmd = createObject( "java", "java.lang.System" ).getProperty( "java.home" ) & "/bin/jcmd";
		if ( !fileExists( jcmd ) ) jcmd &= ".exe";
		if ( !fileExists( jcmd ) ) return "jcmd not available";
		var out = arguments.logFile & ".threads";
		var pb = createObject( "java", "java.lang.ProcessBuilder" ).init( [ jcmd, toString( arguments.process.pid() ), "Thread.print" ] );
		pb.redirectErrorStream( true );
		pb.redirectOutput( createObject( "java", "java.io.File" ).init( out ) );
		pb.start().waitFor( 30, createObject( "java", "java.util.concurrent.TimeUnit" ).SECONDS );
		return fileExists( out ) ? fileRead( out ) : "";
	}

	// jar (or class directory) the class of the given object is loaded from
	private string function toPath( required any obj ) {
		return createObject( "java", "java.io.File" ).init( arguments.obj.getClass().getProtectionDomain().getCodeSource().getLocation().toURI() ).getAbsolutePath();
	}
}
