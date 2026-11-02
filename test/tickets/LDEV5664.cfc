component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	// the flag is read once when the JVM starts, so every scenario needs its own JVM
	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV5664-" & createUUID() & "/";
		directoryCreate( root, true );
		// a cfid has the format of a java UUID, createUUID() returns a different format that is not accepted as cfid
		variables.urlCfid = createObject( "java", "java.util.UUID" ).randomUUID().toString();
		variables.urlCftoken = "0";
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5664: CFID and CFTOKEN from the URL", function() {

			it( "are not accepted by default", function() {
				var res = runRequest( "default", {}, "url" );
				expect( res.cfid ).notToBe( urlCfid, res.log );
				expect( len( res.cfid ) ).toBe( 36, res.log );
			});

			it( "are accepted when lucee.cfid.url.allow is true", function() {
				var res = runRequest( "allow-true", { "lucee.cfid.url.allow": "true" }, "url" );
				expect( res.cfid ).toBe( urlCfid, res.log );
				expect( res.cftoken ).toBe( urlCftoken, res.log );
			});

			it( "are not accepted when lucee.cfid.url.allow is false", function() {
				var res = runRequest( "allow-false", { "lucee.cfid.url.allow": "false" }, "url" );
				expect( res.cfid ).notToBe( urlCfid, res.log );
			});

			it( "are accepted when the legacy lucee.read.cfid.from.url is true, it takes precedence", function() {
				var res = runRequest( "legacy-true", { "lucee.read.cfid.from.url": "true", "lucee.cfid.url.allow": "false" }, "url" );
				expect( res.cfid ).toBe( urlCfid, res.log );
			});

			it( "are not accepted when the legacy lucee.read.cfid.from.url is false, it takes precedence", function() {
				var res = runRequest( "legacy-false", { "lucee.read.cfid.from.url": "false", "lucee.cfid.url.allow": "true" }, "url" );
				expect( res.cfid ).notToBe( urlCfid, res.log );
			});

			it( "from a cookie are still accepted by default", function() {
				var res = runRequest( "cookie", {}, "cookie" );
				expect( res.cfid ).toBe( urlCfid, res.log );
				expect( res.cftoken ).toBe( urlCftoken, res.log );
			});

		});
	}

	// starts a new Lucee (fresh server directory) in a separate JVM and executes one request, sending the cfid and cftoken in the url or as cookies
	private struct function runRequest( required string name, required struct sysprops, required string transport ) {
		var webroot = root & arguments.name & "/";
		var serverContext = webroot & "WEB-INF/lucee-server/context/";
		var logFile = root & arguments.name & ".log";
		var resultFile = webroot & "result.json";
		directoryCreate( serverContext & "context", true );
		fileWrite( webroot & "Application.cfc", 'component { this.name="LDEV5664-#arguments.name#"; this.sessionManagement=true; }' );
		fileWrite( webroot & "index.cfm", '<cfscript>
			fileWrite( "#webroot#result.tmp", serializeJSON( { "cfid": session.cfid, "cftoken": session.cftoken } ) );
			fileMove( "#webroot#result.tmp", "#resultFile#" );
		</cfscript>' );
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
		loop struct=arguments.sysprops index="local.k" item="local.v" {
			arrayAppend( cmd, "-D#k#=#v#" );
		}
		arrayAppend( cmd, "-Dlucee.bundles.dir=" & bundleDir );
		arrayAppend( cmd, "-cp" );
		arrayAppend( cmd, arrayToList( cp, File.pathSeparator ) );
		arrayAppend( cmd, "lucee.cli.CLI" );
		arrayAppend( cmd, "webroot=" & webroot );
		if ( arguments.transport == "url" ) {
			arrayAppend( cmd, "uri=/index.cfm?cfid=#urlCfid#&cftoken=#urlCftoken#" );
		}
		else {
			arrayAppend( cmd, "uri=/index.cfm" );
			arrayAppend( cmd, "cookie=cfid=#urlCfid#&cftoken=#urlCftoken#" );
		}
		var pb = createObject( "java", "java.lang.ProcessBuilder" ).init( cmd );
		var penv = pb.environment();
		// the new server must not pick up settings from the server running the tests
		loop array=[ "LUCEE_CFID_URL_ALLOW", "LUCEE_READ_CFID_FROM_URL", "LUCEE_ENABLE_WARMUP", "LUCEE_BUILD", "LUCEE_EXTENSIONS", "LUCEE_EXTENSIONS_INSTALL", "LUCEE_EXTENSIONS_FAIL_ON_ERROR", "LUCEE_BASE_CONFIG", "LUCEE_REQUEST_TIMEOUT" ] item="local.k" {
			penv.remove( k );
		}
		pb.directory( File.init( root ) );
		pb.redirectErrorStream( true );
		pb.redirectOutput( File.init( logFile ) );

		var process = pb.start();
		var TimeUnit = createObject( "java", "java.util.concurrent.TimeUnit" );
		var begin = getTickCount();
		while ( !process.waitFor( 250, TimeUnit.MILLISECONDS ) ) {
			// the CLI does not end the JVM on its own after a successful request, so once the request was executed we are done
			if ( fileExists( resultFile ) ) {
				process.destroyForcibly();
				process.waitFor( 30, TimeUnit.SECONDS );
				break;
			}
			if ( getTickCount() - begin > 120000 ) {
				process.destroyForcibly();
				throw "server [#arguments.name#] did not finish within 2 minutes, output: " & ( fileExists( logFile ) ? right( fileRead( logFile ), 5000 ) : "" );
			}
		}
		var log = fileExists( logFile ) ? fileRead( logFile ) : "";
		if ( !fileExists( resultFile ) ) throw "the request of [#arguments.name#] was not executed, output: " & log;
		var result = deserializeJSON( fileRead( resultFile ) );
		result.log = log;
		return result;
	}

	// jar (or class directory) the class of the given object is loaded from
	private string function toPath( required any obj ) {
		return createObject( "java", "java.io.File" ).init( arguments.obj.getClass().getProtectionDomain().getCodeSource().getLocation().toURI() ).getAbsolutePath();
	}
}
