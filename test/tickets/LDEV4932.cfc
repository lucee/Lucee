component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	variables.workDir = getTempDirectory() & "ldev4932-" & createUUID() & "/";

	function beforeAll() {
		directoryCreate( variables.workDir );
	}

	function afterAll() {
		if ( directoryExists( variables.workDir ) ) directoryDelete( variables.workDir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-4932 clear error when a bundle needs a newer Java than the running one", function() {

			it( title="bundle declaring a higher osgi.ee names the bundle, the required and the running Java version", body=function( currentSpec ) {
				var name = "org.lucee.ldev4932.bree";
				var bundle = installBundle( name, "Bundle-RequiredExecutionEnvironment: JavaSE-99" );
				var msg = startAndGetMessage( bundle );
				expect( msg ).toInclude( name & ":1.0.0" );
				expect( msg ).toInclude( "requires Java [99] or newer" );
				expect( msg ).toInclude( "running on Java [" & runningJava() & "]" );
				expect( msg ).notToInclude( "is not available in version" );
			});

			it( title="bundle with an osgi.ee Require-Capability above the running Java is reported the same way", body=function( currentSpec ) {
				var name = "org.lucee.ldev4932.capability";
				var bundle = installBundle( name, 'Require-Capability: osgi.ee;filter:="(&(osgi.ee=JavaSE)(version=98))"' );
				var msg = startAndGetMessage( bundle );
				expect( msg ).toInclude( name & ":1.0.0" );
				expect( msg ).toInclude( "requires Java [98] or newer" );
				expect( msg ).toInclude( "running on Java [" & runningJava() & "]" );
			});

			it( title="bundle with a class file newer than the running Java (UnsupportedClassVersionError) names the required Java version", body=function( currentSpec ) {
				var name = "org.lucee.ldev4932.classfile";
				// class file major version 99 = Java 55, the JVM rejects it before reading anything else
				var bundle = installBundle( name, "Bundle-Activator: ldev4932.Activator", { "ldev4932/Activator.class": binaryDecode( "CAFEBABE00000063", "hex" ) } );
				var msg = startAndGetMessage( bundle );
				expect( msg ).toInclude( name & ":1.0.0" );
				expect( msg ).toInclude( "requires Java [55] or newer" );
				expect( msg ).toInclude( "running on Java [" & runningJava() & "]" );
			});

			it( title="bundle declaring an osgi.ee at or below the running Java still gets the normal start", body=function( currentSpec ) {
				var name = "org.lucee.ldev4932.ok";
				var bundle = installBundle( name, "Bundle-RequiredExecutionEnvironment: JavaSE-1.8" );
				var OSGiUtil = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
				OSGiUtil.start( bundle );
				expect( bundle.getState() ).toBe( bundle.ACTIVE );
			});

		});
	}

	private numeric function runningJava() {
		return createObject( "java", "java.lang.Runtime" ).version().feature();
	}

	private string function startAndGetMessage( required bundle ) {
		var OSGiUtil = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
		try {
			OSGiUtil.start( arguments.bundle );
		}
		catch ( any e ) {
			return e.message;
		}
		throw( message="starting the bundle should have failed" );
	}

	private function installBundle( required string name, required string header, struct files={} ) {
		var jar = variables.workDir & arguments.name & ".jar";
		var manifest = "Manifest-Version: 1.0" & chr( 13 ) & chr( 10 )
			& "Bundle-ManifestVersion: 2" & chr( 13 ) & chr( 10 )
			& "Bundle-SymbolicName: " & arguments.name & chr( 13 ) & chr( 10 )
			& "Bundle-Version: 1.0.0" & chr( 13 ) & chr( 10 )
			& arguments.header & chr( 13 ) & chr( 10 );
		var JarOutputStream = createObject( "java", "java.util.jar.JarOutputStream" );
		var out = JarOutputStream.init( createObject( "java", "java.io.FileOutputStream" ).init( jar ) );
		try {
			var entry = createObject( "java", "java.util.zip.ZipEntry" ).init( "META-INF/MANIFEST.MF" );
			out.putNextEntry( entry );
			out.write( charsetDecode( manifest, "utf-8" ) );
			out.closeEntry();
			loop struct=arguments.files index="local.path" item="local.bytes" {
				out.putNextEntry( createObject( "java", "java.util.zip.ZipEntry" ).init( path ) );
				out.write( bytes );
				out.closeEntry();
			}
		}
		finally {
			out.close();
		}
		var bc = createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance().getBundleContext();
		return bc.installBundle( "file:" & jar );
	}

}
