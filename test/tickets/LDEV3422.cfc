component extends="org.lucee.cfml.test.LuceeTestCase" labels="warmup" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV3422-" & createUUID() & "/";
		directoryCreate( root, true );
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3422: warmup compiles all templates of the mappings (opt-in)", function() {

			it( "does not compile the templates by default", function() {
				var res = runWarmup( "default", false );
				expect( res.exitCode ).toBe( 0, res.log );
				expect( res.log ).notToInclude( "warmup compile", res.log );
				expect( directoryExists( webroot( "default" ) & "WEB-INF/lucee-server/context/logs" ) ).toBeTrue( "the server did not start: " & res.log );
				expect( classFiles( "default", "good.cfm" ) ).toBeEmpty( "template was compiled without LUCEE_ENABLE_WARMUP_COMPILE: " & res.log );
			});

			it( "compiles the templates of the web root and of additional mappings", function() {
				var res = runWarmup( "compile", true );
				expect( res.exitCode ).toBe( 0, res.log );
				expect( res.log ).toInclude( "warmup compile", res.log );
				expect( classFiles( "compile", "good.cfm" ) ).notToBeEmpty( "web root template was not compiled: " & res.log );
				expect( classFiles( "compile", "nested.cfm" ) ).notToBeEmpty( "template in a sub directory was not compiled: " & res.log );
				expect( classFiles( "compile", "Comp.cfc" ) ).notToBeEmpty( "component was not compiled: " & res.log );
				expect( classFiles( "compile", "mapped.cfm" ) ).notToBeEmpty( "template of an additional mapping was not compiled: " & res.log );
				expect( res.log ).toMatch( "compiled \d+ templates", res.log );
			});

			it( "skips mappings that only have an archive", function() {
				var res = runWarmup( "archive", true, { "mappings": { "/archiveonly": { "archive": "{lucee-config}/archiveonly.lar" } } } );
				expect( res.exitCode ).toBe( 0, res.log );
				expect( res.log ).toMatch( "compiled \d+ templates, \d+ failed, skipped [1-9]\d* archive only mappings", res.log );
			});

			it( "logs templates that fail to compile and still completes the warmup", function() {
				var res = runWarmup( "broken", true, {}, { "broken.cfm": "<cfif>" } );
				expect( res.exitCode ).toBe( 0, res.log );
				expect( res.log ).toMatch( "compiled \d+ templates, [1-9]\d* failed", res.log );
				expect( classFiles( "broken", "good.cfm" ) ).notToBeEmpty( "valid template was not compiled after a failing one: " & res.log );

				// the failure is logged with the exception and the template
				expect( res.log ).toInclude( "warmup-compile: failed to compile [", res.log );
				expect( res.log ).toInclude( "broken.cfm", res.log );
				expect( res.log ).toInclude( "No matching end tag found for tag [cfif]", res.log );
			});

		});
	}

	private string function webroot( required string name ) {
		return root & arguments.name & "/";
	}

	// class files that were generated for the given template, the class files of a mapping are stored
	// in the class directory of the server context, in a directory per mapping
	private array function classFiles( required string name, required string template ) {
		var dir = webroot( arguments.name ) & "WEB-INF/lucee-server/context/cfclasses/";
		if ( !directoryExists( dir ) ) return [];
		var pattern = "[/\\]" & replace( arguments.template, ".", "_" ) & "\$cf\.class$";
		return directoryList( dir, true, "path", function( path ) {
			return reFindNoCase( pattern, path ) > 0;
		} );
	}

	// starts a new Lucee server (fresh server directory) in a separate JVM with warmup enabled
	private struct function runWarmup( required string name, required boolean compile, struct config = {}, struct extraTemplates = {} ) {
		var webroot = webroot( arguments.name );
		var serverContext = webroot & "WEB-INF/lucee-server/context/";
		var logFile = root & arguments.name & ".log";
		directoryCreate( serverContext & "context", true );
		directoryCreate( webroot & "sub", true );
		directoryCreate( root & arguments.name & "-mapped", true );
		fileWrite( webroot & "index.cfm", "" );
		fileWrite( webroot & "good.cfm", "<cfset x = 1>" );
		fileWrite( webroot & "sub/nested.cfm", "<cfset x = 1>" );
		fileWrite( webroot & "Comp.cfc", "component { function test() { return 1; } }" );
		fileWrite( root & arguments.name & "-mapped/mapped.cfm", "<cfset x = 1>" );
		loop struct=arguments.extraTemplates index="local.file" item="local.code" {
			fileWrite( webroot & file, code );
		}
		var cfg = duplicate( arguments.config );
		if ( !structKeyExists( cfg, "mappings" ) ) cfg.mappings = {};
		cfg.mappings[ "/mappedtemplates" ] = { "physical": root & arguments.name & "-mapped" };
		// the mappings are configured in the server context
		fileWrite( serverContext & ".CFConfig.json", serializeJSON( cfg ) );
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
		var cmd = [ java, "-Dlucee.enable.warmup=true", "-Dlucee.enable.warmup.compile=" & arguments.compile, "-Dlucee.bundles.dir=" & bundleDir, "-cp", arrayToList( cp, File.pathSeparator ), "lucee.cli.CLI", "webroot=" & webroot, "uri=/index.cfm" ];
		var pb = createObject( "java", "java.lang.ProcessBuilder" ).init( cmd );
		var env = pb.environment();
		// the new server must not pick up settings from the server running the tests
		loop array=[ "LUCEE_ENABLE_WARMUP", "LUCEE_ENABLE_WARMUP_COMPILE", "LUCEE_BUILD", "LUCEE_EXTENSIONS", "LUCEE_EXTENSIONS_INSTALL", "LUCEE_BASE_CONFIG", "LUCEE_REQUEST_TIMEOUT" ] item="local.k" {
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
