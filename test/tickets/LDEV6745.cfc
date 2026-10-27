component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV6745-" & createUUID() & "/";
		directoryCreate( root, true );
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6745: extension resource defined in .CFConfig.json", function() {

			it( "resolves the {lucee-server} placeholder in the resource", function() {
				var ext = { "id": createUUID(), "version": "1.0.0.1" };
				var res = runServer( "placeholder", function( serverContext ) {
					createExtension( serverContext & "LDEV6745.lex", ext.id, ext.version );
					return { "extensions": [ { "id": ext.id, "version": ext.version, "resource": "{lucee-server}/LDEV6745.lex" } ] };
				});
				expect( res.exitCode ).toBe( 0, res.log );
				expect( isInstalled( res.serverContext, ext.id ) ).toBeTrue( "the extension defined with the resource [{lucee-server}/LDEV6745.lex] was not installed: " & res.log );
			});

			it( "does not fall back to a download when the resource does not exist", function() {
				var ext = { "id": createUUID(), "version": "1.0.0.2" };
				var res = runServer( "missing", function( serverContext ) {
					// the same extension is available locally, so a fallback (download) would succeed
					createExtension( serverContext & "extensions/available/#ext.id#-#ext.version#.lex", ext.id, ext.version );
					return { "extensions": [ { "id": ext.id, "version": ext.version, "resource": serverContext & "does-not-exist/LDEV6745.lex" } ] };
				});
				expect( res.exitCode ).toBe( 0, res.log );
				expect( isInstalled( res.serverContext, ext.id ) ).toBeFalse( "the extension was installed even though the defined resource does not exist: " & res.log );
			});

		});
	}

	// creates a minimal extension (.lex) file
	private void function createExtension( required string path, required string id, required string version ) {
		var manifest = 'Manifest-Version: 1.0#chr(10)#id: "#arguments.id#"#chr(10)#version: "#arguments.version#"#chr(10)#name: "LDEV6745 #arguments.version#"#chr(10)#release-type: server#chr(10)#start-bundles: false#chr(10)##chr(10)#';
		var dir = getDirectoryFromPath( arguments.path );
		if ( !directoryExists( dir ) ) directoryCreate( dir, true );
		var zos = createObject( "java", "java.util.zip.ZipOutputStream" ).init( createObject( "java", "java.io.FileOutputStream" ).init( arguments.path ) );
		try {
			zos.putNextEntry( createObject( "java", "java.util.zip.ZipEntry" ).init( "META-INF/MANIFEST.MF" ) );
			zos.write( manifest.getBytes( "UTF-8" ) );
			zos.closeEntry();
		}
		finally {
			zos.close();
		}
	}

	// is an extension with the given id in the installed folder of the given server
	private boolean function isInstalled( required string serverContext, required string id ) {
		var dir = arguments.serverContext & "extensions/installed/";
		if ( !directoryExists( dir ) ) return false;
		loop array=directoryList( dir, false, "path", "*.lex" ) item="local.lex" {
			if ( findNoCase( arguments.id, fileRead( "zip://" & lex & "!/META-INF/MANIFEST.MF" ) ) ) return true;
		}
		return false;
	}

	// starts a new Lucee server (fresh server directory) in a separate JVM with warmup enabled
	private struct function runServer( required string name, required function prepare ) {
		var webroot = root & arguments.name & "/";
		var serverContext = webroot & "WEB-INF/lucee-server/context/";
		var logFile = root & arguments.name & ".log";
		directoryCreate( serverContext, true );
		fileWrite( webroot & "index.cfm", "" );
		fileWrite( serverContext & ".CFConfig.json", serializeJSON( arguments.prepare( serverContext ) ) );

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
			throw "server [#arguments.name#] did not finish within 5 minutes";
		}
		return {
			"exitCode": process.exitValue(),
			"serverContext": serverContext,
			"log": fileExists( logFile ) ? fileRead( logFile ) : ""
		};
	}

	// jar (or class directory) the class of the given object is loaded from
	private string function toPath( required any obj ) {
		return createObject( "java", "java.io.File" ).init( arguments.obj.getClass().getProtectionDomain().getCodeSource().getLocation().toURI() ).getAbsolutePath();
	}
}
