component extends="org.lucee.cfml.test.LuceeTestCase" labels="warmup,javasettings" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV5281-" & createUUID() & "/";
		directoryCreate( root, true );
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5281: warmup resolves the maven dependencies of the javasettings of compiled components", function() {

			it( "downloads the maven dependencies of a component compiled in the warmup", function() {
				var res = runWarmup( "valid", true, javasettings( '["javax.inject:javax.inject:1"]' ) );
				expect( res.exitCode ).toBe( 0, res.log );
				expect( mavenJars( "valid" ) ).toHaveLength( 1, "the jar was not downloaded: " & res.log );
				expect( mavenJars( "valid" )[ 1 ] ).toInclude( "javax.inject-1.jar" );
			});

			it( "accepts the maven dependencies as a struct", function() {
				var res = runWarmup( "struct", true, javasettings( '[{"groupId":"javax.inject","artifactId":"javax.inject","version":"1"}]' ) );
				expect( res.exitCode ).toBe( 0, res.log );
				expect( mavenJars( "struct" ) ).toHaveLength( 1, "the jar was not downloaded: " & res.log );
			});

			it( "does not download anything when no component is compiled in the warmup", function() {
				var res = runWarmup( "nocompile", false, javasettings( '["javax.inject:javax.inject:1"]' ) );
				expect( res.exitCode ).toBe( 0, res.log );
				expect( mavenJars( "nocompile" ) ).toBeEmpty( "the jar was downloaded without compiling: " & res.log );
			});

			it( "fails the warmup if an artifact can not be downloaded", function() {
				var res = runWarmup( "missing", true, javasettings( '["org.lucee.ldev5281:does-not-exist:1.0.0"]' ) );
				expect( res.exitCode ).toBe( 1, res.log );
				expect( res.log ).toInclude( "Lucee warmup failed, the maven dependencies of a javasettings could not be resolved", res.log );
				expect( res.log ).toInclude( "org.lucee.ldev5281:does-not-exist", res.log );
				expect( res.log ).toInclude( "Comp.cfc", res.log );
			});

			it( "fails the warmup if a coordinate is invalid", function() {
				var res = runWarmup( "invalid", true, javasettings( '["notacoordinate"]' ) );
				expect( res.exitCode ).toBe( 1, res.log );
				expect( res.log ).toInclude( "Lucee warmup failed, the maven dependencies of a javasettings could not be resolved", res.log );
				expect( res.log ).toInclude( "Comp.cfc", res.log );
			});

		});

		describe( "LDEV-5281: normal compile does not resolve the maven dependencies", function() {

			it( "compiles and loads a component with an artifact that does not exist", function() {
				var mvnDir = getPageContext().getConfig().getMavenDir();
				var cmp = new LDEV5281.notDownloaded();
				expect( cmp.test() ).toBe( "ok" );
				expect( directoryExists( mvnDir & "/org/lucee/ldev5281" ) ).toBeFalse( "the maven dependency was resolved when compiling" );
			});

		});
	}

	private string function javasettings( required string maven ) {
		return "component javasettings='{""maven"":" & arguments.maven & "}' { function test() { return ""ok""; } }";
	}

	private string function webroot( required string name ) {
		return root & arguments.name & "/";
	}

	// jars of the test artifact in the maven directory of the warmup server
	private array function mavenJars( required string name ) {
		var dir = webroot( arguments.name ) & "mvn";
		if ( !directoryExists( dir ) ) return [];
		return directoryList( dir, true, "path", "*javax.inject-1.jar" );
	}

	// starts a new Lucee server (fresh server directory) in a separate JVM with warmup enabled
	private struct function runWarmup( required string name, required boolean compile, required string component ) {
		var webroot = webroot( arguments.name );
		var serverContext = webroot & "WEB-INF/lucee-server/context/";
		var logFile = root & arguments.name & ".log";
		directoryCreate( serverContext & "context", true );
		fileWrite( webroot & "index.cfm", "" );
		fileWrite( webroot & "Comp.cfc", arguments.component );
		// a separate maven directory and user home, so artifacts of the machine are not used
		fileWrite( serverContext & ".CFConfig.json", serializeJSON( { "mavenDirectory": webroot & "mvn" } ) );
		directoryCreate( root & arguments.name & "-home", true );
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
		var cmd = [ java, "-Dlucee.enable.warmup=true", "-Dlucee.enable.warmup.compile=" & arguments.compile, "-Dlucee.bundles.dir=" & bundleDir,
			"-Duser.home=" & root & arguments.name & "-home", "-cp", arrayToList( cp, File.pathSeparator ), "lucee.cli.CLI", "webroot=" & webroot, "uri=/index.cfm" ];
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
