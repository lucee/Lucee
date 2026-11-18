component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV5847" {

	function beforeAll() {
		variables.webroot = getTempDirectory() & "LDEV5847-" & createUUID() & "/";
		variables.serverContext = webroot & "WEB-INF/lucee-server/context/";
		directoryCreate( serverContext, true );
	}

	function afterAll() {
		if ( directoryExists( variables.webroot ) ) directoryDelete( variables.webroot, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5847 extension installed via .CFConfig.json", function() {

			it( "deploys the admin plugin of the extension", function() {
				createExtension( serverContext & "LDEV5847.lex" );
				fileWrite( serverContext & ".CFConfig.json", serializeJSON( { "extensions": [ { "id": variables.extId, "version": "1.0.0.1", "resource": "{lucee-server}/LDEV5847.lex" } ] } ) );

				var log = startServer();

				expect( fileExists( serverContext & "context/admin/plugin/LDEV5847/Action.cfc" ) ).toBeTrue( log );
				expect( fileExists( serverContext & "context/admin/plugin/LDEV5847/language.xml" ) ).toBeTrue( log );
			});

		});
	}

	// minimal extension (.lex) that contains an admin plugin
	private void function createExtension( required string path ) {
		variables.extId = createUUID();
		var files = [
			"META-INF/MANIFEST.MF": 'Manifest-Version: 1.0#chr(10)#id: "#extId#"#chr(10)#version: "1.0.0.1"#chr(10)#name: "LDEV5847"#chr(10)#release-type: server#chr(10)#start-bundles: false#chr(10)##chr(10)#',
			"plugins/LDEV5847/Action.cfc": 'component extends="lucee.admin.plugin.Plugin" {}',
			"plugins/LDEV5847/language.xml": '<?xml version="1.0" encoding="UTF-8"?><languages action="ldev5847"><language key="en"><group>LDEV5847</group><title>LDEV5847</title><text>LDEV5847</text></language></languages>'
		];
		var zos = createObject( "java", "java.util.zip.ZipOutputStream" ).init( createObject( "java", "java.io.FileOutputStream" ).init( arguments.path ) );
		try {
			loop collection=files key="local.name" value="local.content" {
				zos.putNextEntry( createObject( "java", "java.util.zip.ZipEntry" ).init( name ) );
				zos.write( content.getBytes( "UTF-8" ) );
				zos.closeEntry();
			}
		}
		finally {
			zos.close();
		}
	}

	// starts a new Lucee server in a separate JVM, so the extension is installed on startup before a web context exists (same as LDEV6745)
	private string function startServer() {
		var System = createObject( "java", "java.lang.System" );
		var File = createObject( "java", "java.io.File" );
		var logFile = webroot & "server.log";
		fileWrite( webroot & "index.cfm", "" );

		var java = System.getProperty( "java.home" ) & "/bin/java";
		if ( !fileExists( java ) ) java &= ".exe";

		var cp = listToArray( System.getProperty( "java.class.path" ), File.pathSeparator );
		loop array=[
			createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance().getCFMLEngineFactory(),
			createObject( "java", "jakarta.servlet.ServletException" ).init( "" ),
			createObject( "java", "jakarta.servlet.jsp.JspException" ).init( "" )
		] item="local.obj" {
			var path = File.init( obj.getClass().getProtectionDomain().getCodeSource().getLocation().toURI() ).getAbsolutePath();
			if ( !arrayContains( cp, path ) ) arrayAppend( cp, path );
		}

		var bundleDir = createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance().getCFMLEngineFactory().getBundleDirectory().getAbsolutePath();
		var pb = createObject( "java", "java.lang.ProcessBuilder" ).init( [ java, "-Dlucee.enable.warmup=true", "-Dlucee.bundles.dir=" & bundleDir, "-cp", arrayToList( cp, File.pathSeparator ), "lucee.cli.CLI", "webroot=" & webroot, "uri=/index.cfm" ] );
		// the new server must not pick up settings from the server running the tests
		loop array=[ "LUCEE_ENABLE_WARMUP", "LUCEE_BUILD", "LUCEE_EXTENSIONS", "LUCEE_EXTENSIONS_INSTALL", "LUCEE_BASE_CONFIG" ] item="local.k" {
			pb.environment().remove( k );
		}
		pb.directory( File.init( webroot ) );
		pb.redirectErrorStream( true );
		pb.redirectOutput( File.init( logFile ) );

		var process = pb.start();
		if ( !process.waitFor( 5, createObject( "java", "java.util.concurrent.TimeUnit" ).MINUTES ) ) process.destroyForcibly();
		return fileExists( logFile ) ? fileRead( logFile ) : "";
	}
}
