component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	function beforeAll() {
		variables.service = getPageContext().getConfig().getFactory().getEngine().getBundleService();
		// the framework itself is the bundle with the id 0
		loop array=service.listBundles() item="local.b" {
			if ( b.id == 0 ) variables.system = b;
		}
	}

	function run( testResults, testBox ) {
		describe( "the BundleService, OSGi without OSGi types (LDEV-6627)", function() {

			it( "is available and implemented in the core", function() {
				expect( service.isAvailable() ).toBeTrue();
			});

			it( "finds an installed bundle by name and version", function() {
				var info = service.getBundleInfo( system.name, "" );
				expect( isNull( info ) ).toBeFalse();
				expect( info.name ).toBe( system.name );
				expect( info.state ).toBe( "active" );
				expect( info.fragment ).toBeFalse();
				expect( service.getBundleInfo( system.name, info.version ).id ).toBe( info.id );
			});

			it( "returns null for a bundle that does not exist", function() {
				expect( isNull( service.getBundleInfo( "does.not.exist", "" ) ) ).toBeTrue();
				expect( isNull( service.getBundleInfo( system.name, "0.0.1" ) ) ).toBeTrue();
			});

			it( "lists the installed bundles", function() {
				var names = [];
				loop array=service.listBundles() item="local.b" {
					arrayAppend( names, b.name );
				}
				expect( arrayLen( names ) ).toBeGT( 1 );
				expect( names ).toInclude( system.name );
			});

			it( "loads a class from a bundle", function() {
				var info = service.getBundleInfo( system.name, "" );
				var clazz = service.loadClass( system.name, info.version, "org.osgi.framework.Constants" );
				expect( clazz.getName() ).toBe( "org.osgi.framework.Constants" );
			});

			it( "tells the bundle a class comes from, null for a class that is not from a bundle", function() {
				var info = service.getBundleInfo( createObject( "java", "java.lang.String" ).getClass() );
				expect( isNull( info ) ).toBeTrue();
			});

			it( "has the manifest headers in the info", function() {
				var info = service.getBundleInfo( system.name, "" );
				expect( isStruct( info.headers ) ).toBeTrue();
				expect( info.headers ).toHaveKey( "Bundle-SymbolicName" );
			});

			it( "loadFromLocal returns the installed bundle and null for an unknown one", function() {
				var info = service.loadFromLocal( system.name, "", false );
				expect( isNull( info ) ).toBeFalse();
				expect( info.id ).toBe( 0 );
				expect( isNull( service.loadFromLocal( "does.not.exist", "", false ) ) ).toBeTrue();
			});

			it( "removeLocal and uninstall do nothing for a bundle that does not exist", function() {
				service.removeLocal( "does.not.exist", "1.0.0", true );
				service.uninstall( "does.not.exist", "1.0.0" );
			});

			it( "returns the requirements of a bundle, null for an unknown one", function() {
				var core = service.getBundleInfo( "lucee.core", "" );
				if ( isNull( core ) ) core = service.getBundleInfo( system.name, "" );
				var req = service.getRequirements( core.name, "" );
				expect( isNull( req ) ).toBeFalse();
				expect( req ).toHaveKey( "requiredBundles" );
				expect( req ).toHaveKey( "requiredPackages" );
				expect( isNull( service.getRequirements( "does.not.exist", "" ) ) ).toBeTrue();
			});

			it( "lists the core bundles, the core first", function() {
				var core = service.listCoreBundles();
				expect( core ).notToBeEmpty();
				expect( isSimpleValue( core[ 1 ].name ) ).toBeTrue();
			});

			it( "has no bundle state for a class that is not from a bundle", function() {
				expect( isNull( service.getBundleState( createObject( "java", "java.lang.String" ).getClass() ) ) ).toBeTrue();
			});

			it( "has no bundle for a class loader that is not a bundle class loader", function() {
				var cl = createObject( "java", "java.lang.ClassLoader" ).getSystemClassLoader();
				expect( isNull( service.getBundleInfo( cl ) ) ).toBeTrue();
			});

			it( "gives name, version, id and state for the class loader of a bundle", function() {
				var checked = false;
				for ( var b in service.listBundles() ) {
					if ( b.id != 0 && b.state == "active" && !b.fragment ) {
						try {
							var cl = service.getClassLoader( b.name, b.version );
							var info = service.getBundleInfo( cl );
							if ( !isNull( info ) ) {
								expect( info.name ).toBe( b.name );
								expect( info.version ).toBe( b.version );
								expect( info.id ).toBe( b.id );
								expect( info.state ).toBe( "active" );
								checked = true;
								break;
							}
						}
						catch ( any e ) {}
					}
				}
				expect( checked ).toBeTrue( "no active bundle besides the system bundle to check" );
			});

			it( "loadBundle returns the info of an installed bundle", function() {
				var info = service.loadBundle( system.name, "" );
				expect( info.id ).toBe( 0 );
			});

			it( "removes the other versions of a bundle (engine startup cleanup)", function() {
				var dir = getTempDirectory() & "BundleServiceStale" & createUniqueId() & "/";
				var osgiUtil = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
				var name = "lucee.test.stale" & createUniqueId();
				directoryCreate( dir );
				try {
					var jar1 = createBundleJar( dir, name, "1.0.0.1" );
					var jar2 = createBundleJar( dir, name, "1.0.0.2" );
					service.install( createObject( "java", "java.io.File" ).init( jar1 ), false );
					service.install( createObject( "java", "java.io.File" ).init( jar2 ), false );
					expect( isNull( service.getBundleInfo( name, "1.0.0.1" ) ) ).toBeFalse();
					expect( isNull( service.getBundleInfo( name, "1.0.0.2" ) ) ).toBeFalse();

					var context = osgiUtil.getBundleContext();
					// without a version to keep nothing is removed
					expect( osgiUtil.uninstallOtherVersions( context, name, "" ) ).toBeEmpty();
					expect( isNull( service.getBundleInfo( name, "1.0.0.1" ) ) ).toBeFalse();

					var removed = osgiUtil.uninstallOtherVersions( context, name, "1.0.0.2" );
					expect( removed.size() ).toBe( 1 );
					expect( isNull( service.getBundleInfo( name, "1.0.0.1" ) ) ).toBeTrue();
					expect( isNull( service.getBundleInfo( name, "1.0.0.2" ) ) ).toBeFalse();

					// the core bundle (with its own version) and unknown bundles are not touched
					var core = service.listCoreBundles()[ 1 ];
					expect( osgiUtil.uninstallOtherVersions( context, core.name, core.version ) ).toBeEmpty();
					expect( osgiUtil.uninstallOtherVersions( context, "does.not.exist", "1.0.0" ) ).toBeEmpty();
				}
				finally {
					for ( var v in [ "1.0.0.1", "1.0.0.2" ] ) {
						if ( !isNull( service.getBundleInfo( name, v ) ) ) service.uninstall( name, v );
					}
					if ( directoryExists( dir ) ) directoryDelete( dir, true );
				}
			});

			it( "throws an IOException for a class the bundle does not have", function() {
				expect( function() {
					service.loadClass( system.name, "", "does.not.Exist" );
				} ).toThrow();
			});
		});
	}

	private string function createBundleJar( required string dir, required string name, required string version ) {
		var src = arguments.dir & arguments.name & "-" & arguments.version & "/";
		directoryCreate( src & "META-INF", true );
		fileWrite( src & "META-INF/MANIFEST.MF", "Manifest-Version: 1.0" & chr( 10 ) & "Bundle-ManifestVersion: 2" & chr( 10 ) & "Bundle-SymbolicName: " & arguments.name & chr( 10 ) & "Bundle-Version: " & arguments.version & chr( 10 ) );
		var jar = arguments.dir & arguments.name & "-" & arguments.version & ".jar";
		zip action="zip" file=jar source=src recurse=true;
		return jar;
	}

}
