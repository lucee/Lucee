component extends="org.lucee.cfml.test.LuceeTestCase" labels="warmup,extensions" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV5272-" & createUUID() & "/";
		directoryCreate( root, true );
		variables.malformed = "path=" & root & "does-not-exist-LDEV5272.lex";
		variables.unknown = "org.lucee:ldev5272-does-not-exist:1.0.0.0";
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5272: invalid LUCEE_EXTENSIONS", function() {

			describe( "malformed entries are reported", function() {

				it( "does not silently drop a malformed entry", function() {
					var list = createObject( "java", "java.util.ArrayList" ).init();
					var defs = createObject( "java", "lucee.runtime.extension.RHExtension" ).toExtensionDefinitions( "99A69D1C-0DDD-4C30-BE5B-A3DA3BEFC1D8, #malformed#, , 8AF4A5B1-AF51-4D02-8AA2-4B0F0DC0B8C1", list );
					expect( arrayLen( defs ) ).toBe( 2 );
					expect( list.size() ).toBe( 1 );
					expect( list.get( 0 ) ).toInclude( "does-not-exist-LDEV5272.lex" );
				});

				it( "does not report valid entries", function() {
					var list = createObject( "java", "java.util.ArrayList" ).init();
					var defs = createObject( "java", "lucee.runtime.extension.RHExtension" ).toExtensionDefinitions( "99A69D1C-0DDD-4C30-BE5B-A3DA3BEFC1D8;version=1.0.0.0,8AF4A5B1-AF51-4D02-8AA2-4B0F0DC0B8C1", list );
					expect( arrayLen( defs ) ).toBe( 2 );
					expect( list.size() ).toBe( 0 );
				});

			});

			describe( "warmup", function() {

				it( "exits with 0 when LUCEE_EXTENSIONS is not set", function() {
					var res = runServer( "warmup-none", true, {}, {} );
					expect( res.exitCode ).toBe( 0, res.log );
				});

				it( "exits with a non zero exit code when an entry of LUCEE_EXTENSIONS is malformed", function() {
					var res = runServer( "warmup-malformed", true, { "LUCEE_EXTENSIONS": malformed }, {} );
					expect( res.exitCode ).notToBe( 0, res.log );
					expect( res.log ).toInclude( "does-not-exist-LDEV5272.lex", res.log );
					expect( res.log ).toInclude( "LUCEE_EXTENSIONS", res.log );
				});

				it( "exits with a non zero exit code when an extension of LUCEE_EXTENSIONS can not be installed", function() {
					var res = runServer( "warmup-unknown", true, { "LUCEE_EXTENSIONS": unknown }, {} );
					expect( res.exitCode ).notToBe( 0, res.log );
					expect( res.log ).toInclude( "ldev5272-does-not-exist", res.log );
				});

				it( "exits with a non zero exit code when the extensions are defined with the lucee.extensions system property", function() {
					var res = runServer( "warmup-sysprop", true, {}, { "lucee.extensions": malformed } );
					expect( res.exitCode ).notToBe( 0, res.log );
				});

			});

			describe( "normal startup", function() {

				it( "continues by default and writes the error to stderr", function() {
					var res = runServer( "start-default", false, { "LUCEE_EXTENSIONS": malformed }, {} );
					expect( fileExists( root & "start-default/started.txt" ) ).toBeTrue( "the request was not executed: " & res.log );
					expect( res.log ).toInclude( "does-not-exist-LDEV5272.lex", res.log );
				});

				it( "continues when lucee.extensions.fail.on.error is false", function() {
					var res = runServer( "start-false", false, { "LUCEE_EXTENSIONS": malformed, "LUCEE_EXTENSIONS_FAIL_ON_ERROR": "false" }, {} );
					expect( fileExists( root & "start-false/started.txt" ) ).toBeTrue( "the request was not executed: " & res.log );
				});

				it( "is aborted when lucee.extensions.fail.on.error is true and an entry is malformed", function() {
					var res = runServer( "start-fatal-malformed", false, { "LUCEE_EXTENSIONS": malformed, "LUCEE_EXTENSIONS_FAIL_ON_ERROR": "true" }, {}, true );
					expect( res.exitCode ).notToBe( 0, res.log );
					expect( fileExists( root & "start-fatal-malformed/started.txt" ) ).toBeFalse( "the request was executed: " & res.log );
					expect( res.log ).toInclude( "does-not-exist-LDEV5272.lex", res.log );
				});

				it( "is aborted when lucee.extensions.fail.on.error is true and an extension can not be installed", function() {
					var res = runServer( "start-fatal-unknown", false, { "LUCEE_EXTENSIONS": unknown }, { "lucee.extensions.fail.on.error": "true" }, true );
					expect( res.exitCode ).notToBe( 0, res.log );
					expect( fileExists( root & "start-fatal-unknown/started.txt" ) ).toBeFalse( "the request was executed: " & res.log );
					expect( res.log ).toInclude( "ldev5272-does-not-exist", res.log );
				});

				it( "is not aborted when lucee.extensions.fail.on.error is true and LUCEE_EXTENSIONS is not set", function() {
					var res = runServer( "start-fatal-none", false, { "LUCEE_EXTENSIONS_FAIL_ON_ERROR": "true" }, {} );
					expect( fileExists( root & "start-fatal-none/started.txt" ) ).toBeTrue( "the request was not executed: " & res.log );
				});

			});

		});
	}

	// starts a new Lucee server (fresh server directory) in a separate JVM, with or without warmup
	private struct function runServer( required string name, required boolean warmup, required struct env, required struct sysprops, boolean mustExit = false ) {
		var webroot = root & arguments.name & "/";
		var serverContext = webroot & "WEB-INF/lucee-server/context/";
		var logFile = root & arguments.name & ".log";
		directoryCreate( serverContext & "context", true );
		fileWrite( webroot & "index.cfm", '<cfscript>fileWrite( "#webroot#started.txt", "started" );</cfscript>' );
		fileWrite( serverContext & "context/Server.cfc", "component { function onBuild() {} }" );

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
		var cmd = [ java ];
		if ( arguments.warmup ) arrayAppend( cmd, "-Dlucee.enable.warmup=true" );
		loop struct=arguments.sysprops index="local.k" item="local.v" {
			arrayAppend( cmd, "-D#k#=#v#" );
		}
		arrayAppend( cmd, "-Dlucee.bundles.dir=" & bundleDir );
		arrayAppend( cmd, "-cp" );
		arrayAppend( cmd, arrayToList( cp, File.pathSeparator ) );
		arrayAppend( cmd, "lucee.cli.CLI" );
		arrayAppend( cmd, "webroot=" & webroot );
		arrayAppend( cmd, "uri=/index.cfm" );
		var pb = createObject( "java", "java.lang.ProcessBuilder" ).init( cmd );
		var penv = pb.environment();
		// the new server must not pick up settings from the server running the tests
		loop array=[ "LUCEE_ENABLE_WARMUP", "LUCEE_BUILD", "LUCEE_EXTENSIONS", "LUCEE_EXTENSIONS_INSTALL", "LUCEE_EXTENSIONS_FAIL_ON_ERROR", "LUCEE_BASE_CONFIG", "LUCEE_REQUEST_TIMEOUT" ] item="local.k" {
			penv.remove( k );
		}
		// the environment map is an inaccessible JDK class, call put via the public Map interface
		var mapClass = createObject( "java", "java.lang.Class" ).forName( "java.util.Map" );
		var put = mapClass.getMethod( "put", [ createObject( "java", "java.lang.Object" ).getClass(), createObject( "java", "java.lang.Object" ).getClass() ] );
		loop struct=arguments.env index="local.k" item="local.v" {
			put.invoke( penv, [ toString( k ), toString( v ) ] );
		}
		pb.directory( File.init( root ) );
		pb.redirectErrorStream( true );
		pb.redirectOutput( File.init( logFile ) );

		var process = pb.start();
		var TimeUnit = createObject( "java", "java.util.concurrent.TimeUnit" );
		var started = webroot & "started.txt";
		var begin = getTickCount();
		while ( !process.waitFor( 250, TimeUnit.MILLISECONDS ) ) {
			// the CLI does not end the JVM on its own after a successful request, so once the request was executed we are done
			if ( !arguments.mustExit && !arguments.warmup && fileExists( started ) ) {
				process.destroyForcibly();
				process.waitFor( 30, TimeUnit.SECONDS );
				break;
			}
			if ( getTickCount() - begin > 120000 ) {
				// report where the server hangs
				var threads = threadDump( process, logFile );
				process.destroyForcibly();
				throw "server [#arguments.name#] did not finish within 2 minutes, output: " & ( fileExists( logFile ) ? right( fileRead( logFile ), 5000 ) : "" ) & chr( 10 ) & "threads: " & threads;
			}
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
