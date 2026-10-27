component extends="org.lucee.cfml.test.LuceeTestCase" labels="warmup" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV6744-" & createUUID() & "/";
		directoryCreate( root, true );
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6744: warmup exit code", function() {

			it( "exits with 0 when onBuild() succeeds", function() {
				var res = runWarmup( "success", 'function onBuild() { fileWrite( "#root#success.txt", "done" ); }' );
				expect( res.exitCode ).toBe( 0, res.log );
				expect( fileExists( root & "success.txt" ) ).toBeTrue( "onBuild() was not executed: " & res.log );
			});

			it( "exits with a non zero exit code when onBuild() throws", function() {
				var res = runWarmup( "throws", 'function onBuild() { fileWrite( "#root#throws.txt", "called" ); throw "LDEV-6744 onBuild failed"; }' );
				expect( fileExists( root & "throws.txt" ) ).toBeTrue( "onBuild() was not executed: " & res.log );
				expect( res.exitCode ).notToBe( 0, res.log );
			});

			it( "exits with a non zero exit code when onBuild() does not finish within the request timeout", function() {
				var res = runWarmup( "timeout", 'function onBuild() { fileWrite( "#root#timeout.txt", "called" ); sleep( 30000 ); }', { "requestTimeout": "0,0,0,2" } );
				expect( fileExists( root & "timeout.txt" ) ).toBeTrue( "onBuild() was not executed: " & res.log );
				expect( res.exitCode ).notToBe( 0, res.log );
			});

		});
	}

	// starts a new Lucee server (fresh server directory) in a separate JVM with warmup enabled
	private struct function runWarmup( required string name, required string onBuild, struct config = {} ) {
		var webroot = root & arguments.name & "/";
		var serverContext = webroot & "WEB-INF/lucee-server/context/";
		var logFile = root & arguments.name & ".log";
		directoryCreate( serverContext & "context", true );
		fileWrite( webroot & "index.cfm", "" );
		fileWrite( serverContext & "context/Server.cfc", "component { " & arguments.onBuild & " }" );
		if ( !structIsEmpty( arguments.config ) ) fileWrite( serverContext & ".CFConfig.json", serializeJSON( arguments.config ) );

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

		var cmd = [ java, "-Dlucee.enable.warmup=true", "-cp", arrayToList( cp, File.pathSeparator ), "lucee.cli.CLI", "webroot=" & webroot, "uri=/index.cfm" ];
		var pb = createObject( "java", "java.lang.ProcessBuilder" ).init( cmd );
		var env = pb.environment();
		// the new server must not pick up settings from the server running the tests
		loop array=[ "LUCEE_ENABLE_WARMUP", "LUCEE_BUILD", "LUCEE_EXTENSIONS", "LUCEE_EXTENSIONS_INSTALL", "LUCEE_BASE_CONFIG", "LUCEE_REQUEST_TIMEOUT" ] item="local.k" {
			env.remove( k );
		}
		pb.directory( File.init( root ) );
		pb.redirectErrorStream( true );
		pb.redirectOutput( File.init( logFile ) );

		var process = pb.start();
		var TimeUnit = createObject( "java", "java.util.concurrent.TimeUnit" );
		if ( !process.waitFor( 5, TimeUnit.MINUTES ) ) {
			// LDEV-6744 report where the server hangs
			var threads = threadDump( process, logFile );
			process.destroyForcibly();
			throw "warmup [#arguments.name#] did not finish within 5 minutes, output: " & ( fileExists( logFile ) ? right( fileRead( logFile ), 5000 ) : "" ) & chr( 10 ) & "threads: " & threads;
		}
		return {
			"exitCode": process.exitValue(),
			"log": fileExists( logFile ) ? fileRead( logFile ) : ""
		};
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
