component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	function beforeAll() {
		variables.symbolicName = "lucee.test.ldev6145";
		variables.bundleDir = createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance().getCFMLEngineFactory().getBundleDirectory();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6145: OSGiUtil.improveFileName() must not rename a jar Felix has installed", function() {

			it( title="a bundle jar that is not installed gets the preferred file name", body=function( currentSpec ) {
				var version = "1.0." & randRange( 1, 999999 );
				var jar = createBundleJar( version );
				var preferred = createObject( "java", "java.io.File" ).init( variables.bundleDir, variables.symbolicName & "-" & version & ".jar" );
				try {
					var result = improveFileName( jar );
					expect( result.getFile().getName() ).toBe( preferred.getName() );
					expect( preferred.isFile() ).toBeTrue();
				}
				finally {
					deleteFile( jar );
					deleteFile( preferred );
				}
			} );

			it( title="a bundle jar Felix has installed from that path keeps its file name", body=function( currentSpec ) {
				var version = "1.0." & randRange( 1, 999999 );
				var jar = createBundleJar( version );
				var preferred = createObject( "java", "java.io.File" ).init( variables.bundleDir, variables.symbolicName & "-" & version & ".jar" );
				var bc = createObject( "java", "lucee.runtime.osgi.OSGiUtil" ).getBundleContext();
				var bundle = bc.installBundle( jar.getAbsolutePath(), createObject( "java", "java.io.FileInputStream" ).init( jar ) );
				try {
					var result = improveFileName( jar );
					expect( result.getFile().getName() ).toBe( jar.getName() );
					expect( jar.isFile() ).toBeTrue();
					expect( preferred.exists() ).toBeFalse();
				}
				finally {
					bundle.uninstall();
					deleteFile( jar );
					deleteFile( preferred );
				}
			} );

		} );
	}

	// a minimal bundle in the bundle directory whose file name isn't the preferred "<symbolic-name>-<version>.jar"
	private function createBundleJar( required string version ) {
		var jar = createObject( "java", "java.io.File" ).init( variables.bundleDir, "ldev6145-" & arguments.version & ".jar" );
		var manifest = createObject( "java", "java.util.jar.Manifest" ).init();
		var attrs = manifest.getMainAttributes();
		attrs.putValue( "Manifest-Version", "1.0" );
		attrs.putValue( "Bundle-ManifestVersion", "2" );
		attrs.putValue( "Bundle-SymbolicName", variables.symbolicName );
		attrs.putValue( "Bundle-Version", arguments.version );
		var out = createObject( "java", "java.util.jar.JarOutputStream" ).init( createObject( "java", "java.io.FileOutputStream" ).init( jar ), manifest );
		out.close();
		return jar;
	}

	// OSGiUtil.improveFileName( File bundleDirectory, BundleFile bf ) is private
	private function improveFileName( required jar ) {
		var bf = createObject( "java", "lucee.runtime.osgi.BundleFile" ).getInstance( arguments.jar );
		var methods = bf.getClass().getClassLoader().loadClass( "lucee.runtime.osgi.OSGiUtil" ).getDeclaredMethods();
		for ( var m in methods ) {
			if ( m.getName() == "improveFileName" ) {
				m.setAccessible( true );
				return m.invoke( javaCast( "null", "" ), [ variables.bundleDir, bf ] );
			}
		}
		throw "OSGiUtil.improveFileName() not found";
	}

	private function deleteFile( required file ) {
		if ( arguments.file.exists() ) arguments.file.delete();
	}

}
