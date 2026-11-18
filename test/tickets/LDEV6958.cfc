component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV6958-" & createUUID() & "/";
		directoryCreate( root, true );
	}

	function afterAll() {
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6958: startup cleanup must not uninstall a configured extension when its install/download fails", function() {

			it( "keeps the installed extension when the configured version can not be delivered", function() {
				var ext = { "id": createUUID(), "installed": "1.0.0.1", "configured": "2.0.0.1" };
				var res = runServer( "failed-download", function( serverContext ) {
					// version 1 is already installed
					createExtension( serverContext & "extensions/installed/LDEV6958-installed.lex", ext.id, ext.installed );
					// version 2 is configured, but it exists nowhere (no local file, the provider does not know the random id)
					return { "extensions": [ { "id": ext.id, "version": ext.configured } ] };
				});
				expect( res.exitCode ).toBe( 0, res.log );
				expect( isInstalled( res.serverContext, ext.id ) ).toBeTrue( "the installed extension was uninstalled although it is still configured: " & res.log );
			});

			it( "still uninstalls an installed extension that is no longer configured", function() {
				var ext = { "id": createUUID(), "installed": "1.0.0.1" };
				var res = runServer( "not-configured", function( serverContext ) {
					createExtension( serverContext & "extensions/installed/LDEV6958-installed.lex", ext.id, ext.installed );
					return { "extensions": [] };
				});
				expect( res.exitCode ).toBe( 0, res.log );
				expect( isInstalled( res.serverContext, ext.id ) ).toBeFalse( "the extension is not configured any longer and should have been removed: " & res.log );
			});

		});
	}

	// creates a minimal extension (.lex) file
	private void function createExtension( required string path, required string id, required string version ) {
		var manifest = 'Manifest-Version: 1.0#chr(10)#id: "#arguments.id#"#chr(10)#version: "#arguments.version#"#chr(10)#name: "LDEV6958 #arguments.version#"#chr(10)#release-type: server#chr(10)#start-bundles: false#chr(10)##chr(10)#';
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
			// LDEV-6958 report where the server hangs
			var threads = threadDump( process, logFile );
			process.destroyForcibly();
			throw "server [#arguments.name#] did not finish within 5 minutes, output: " & ( fileExists( logFile ) ? right( fileRead( logFile ), 5000 ) : "" ) & chr( 10 ) & "threads: " & threads;
		}
		return {
			"exitCode": process.exitValue(),
			"serverContext": serverContext,
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
