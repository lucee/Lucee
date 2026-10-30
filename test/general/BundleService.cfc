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

			it( "throws an IOException for a class the bundle does not have", function() {
				expect( function() {
					service.loadClass( system.name, "", "does.not.Exist" );
				} ).toThrow();
			});
		});
	}
}
