component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV5750-" & createUUID() & "/";
		directoryCreate( root, true );
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5750: unreadable .CFConfig.json at startup", function() {

			it( "fails the startup and leaves the file untouched when the file contains invalid JSON", function() {
				var invalid = '{#chr( 10 )#	"requestTimeout": "0,0,0,50",#chr( 10 )#	"scopeCascading": #chr( 10 )#	"sessionType" "jee"#chr( 10 )#}';
				var res = runServer( "invalid", invalid );
				expect( res.exitCode ).notToBe( 0, res.log );
				expect( res.log ).toInclude( ".CFConfig.json", res.log );
				expect( res.log ).toInclude( "line", res.log );
				expect( res.log ).toInclude( "column", res.log );
				expect( fileRead( res.configFile ) ).toBe( invalid, "the config file was changed" );
				expect( res.files ).toBe( ".CFConfig.json", "unexpected files next to the config file: " & res.files );
			});

			it( "fails the startup and leaves the file untouched when the file is not a JSON struct", function() {
				var invalid = '[ "a", "b" ]';
				var res = runServer( "array", invalid );
				expect( res.exitCode ).notToBe( 0, res.log );
				expect( res.log ).toInclude( ".CFConfig.json", res.log );
				expect( fileRead( res.configFile ) ).toBe( invalid, "the config file was changed" );
				expect( res.files ).toBe( ".CFConfig.json", "unexpected files next to the config file: " & res.files );
			});

			it( "starts with a valid file and keeps its settings", function() {
				var valid = '{ "requestTimeout": "0,0,0,50" }';
				var res = runServer( "valid", valid );
				expect( res.exitCode ).toBe( 0, res.log );
				// the server adds its own settings (for example the salt) to a valid file
				var data = deserializeJSON( fileRead( res.configFile ) );
				expect( data.requestTimeout ).toBe( "0,0,0,50" );
				expect( res.files ).toBe( ".CFConfig.json", "unexpected files next to the config file: " & res.files );
			});

			it( "creates the default config file when the file does not exist", function() {
				var res = runServer( "missing" );
				expect( res.exitCode ).toBe( 0, res.log );
				expect( fileExists( res.configFile ) ).toBeTrue( "the default config file was not created: " & res.log );
				expect( isJson( fileRead( res.configFile ) ) ).toBeTrue( "the default config file is not valid JSON" );
				expect( res.files ).toBe( ".CFConfig.json", "unexpected files next to the config file: " & res.files );
			});

		});
	}

	// starts a new Lucee server (fresh server directory) in a separate JVM with warmup enabled (the JVM exits when the startup is done or failed),
	// the config file is only written when a content is provided
	private struct function runServer( required string name, string content ) {
		var webroot = root & arguments.name & "/";
		var serverContext = webroot & "WEB-INF/lucee-server/context/";
		var configFile = serverContext & ".CFConfig.json";
		var logFile = root & arguments.name & ".log";
		directoryCreate( serverContext, true );
		fileWrite( webroot & "index.cfm", "" );
		if ( !isNull( arguments.content ) ) fileWrite( configFile, arguments.content );

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
			process.destroyForcibly();
			throw "server [#arguments.name#] did not finish within 5 minutes, output: " & ( fileExists( logFile ) ? right( fileRead( logFile ), 5000 ) : "" );
		}

		// everything in the server context directory that is a file and looks like a config file
		var files = [];
		loop array=directoryList( serverContext, false, "name" ) item="local.n" {
			if ( findNoCase( "cfconfig", n ) || findNoCase( "buggy", n ) || findNoCase( "corrupt", n ) ) arrayAppend( files, n );
		}
		arraySort( files, "textnocase" );
		return {
			"exitCode": process.exitValue(),
			"webroot": webroot,
			"configFile": configFile,
			"files": arrayToList( files ),
			"log": fileExists( logFile ) ? fileRead( logFile ) : ""
		};
	}

	// jar (or class directory) the class of the given object is loaded from
	private string function toPath( required any obj ) {
		return createObject( "java", "java.io.File" ).init( arguments.obj.getClass().getProtectionDomain().getCodeSource().getLocation().toURI() ).getAbsolutePath();
	}
}
