component extends="org.lucee.cfml.test.LuceeTestCase" labels="osgi" {

	function beforeAll() {
		variables.factory = createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance();
		variables.bundleContext = variables.factory.getBundleContext();
		variables.jar = getJar();
	}

	private string function getJar() {
		var location = createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		if ( !location.endsWith( ".jar" ) ) return "";
		return createObject( "java", "java.net.URI" ).init( location ).getPath();
	}

	private boolean function canLoad( required string className ) {
		try {
			createObject( "java", arguments.className );
			return true;
		}
		catch ( any e ) {
			return false;
		}
	}

	function run( testResults, testBox ) {
		describe( "Felix is shaded into the Lucee jar", function() {

			it( title="the framework is the system bundle with id 0 and the relocated symbolic name", body=function( currentSpec ) {
				var systemBundle = variables.bundleContext.getBundle( 0 );
				expect( systemBundle.getBundleId() ).toBe( 0 );
				expect( systemBundle.getSymbolicName() ).toBe( "lucee.shaded.felix.framework" );
				expect( systemBundle.getClass().getName() ).toBe( "lucee.shaded.felix.framework.Felix" );
			});

			it( title="the system bundle is recognized as the framework bundle", body=function( currentSpec ) {
				var OSGiUtil = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
				expect( OSGiUtil.isFrameworkBundle( variables.bundleContext.getBundle( 0 ) ) ).toBeTrue();
				var others = 0;
				for ( var b in variables.bundleContext.getBundles() ) {
					if ( b.getBundleId() == 0 ) continue;
					expect( OSGiUtil.isFrameworkBundle( b ) ).toBeFalse( b.getSymbolicName() );
					others++;
				}
				expect( others ).toBeGT( 0 );
			});

			it( title="the class loader of a bundle is found by the OSGi API (BundleReference)", body=function( currentSpec ) {
				// OSGiUtil is a class of the core bundle
				var obj = createObject( "java", "lucee.runtime.osgi.OSGiUtil" );
				var cl = obj.getClass().getClassLoader();
				expect( cl.getClass().getName() ).toBe( "lucee.shaded.felix.framework.BundleWiringImpl$BundleClassLoader" );
				expect( cl.getBundle().getSymbolicName() ).toBe( "lucee.core" );
				var info = bundleInfo( obj );
				expect( info.name ).toBe( "lucee.core" );
			});

			it( title="the framework classes are only available under the relocated name", body=function( currentSpec ) {
				expect( canLoad( "lucee.shaded.felix.framework.Felix" ) ).toBeTrue();
				expect( canLoad( "org.apache.felix.framework.Felix" ) ).toBeFalse();
				expect( canLoad( "org.apache.felix.framework.BundleWiringImpl$BundleClassLoader" ) ).toBeFalse();
				// the OSGi API is not relocated
				expect( canLoad( "org.osgi.framework.BundleReference" ) ).toBeTrue();
			});

			it( title="the jar has the relocated classes and the service file points to the relocated factory", skip=!len( getJar() ), body=function( currentSpec ) {
				zip action="list" file=variables.jar name="local.entries";
				var names = valueArray( entries, "name" );
				var shaded = 0;
				for ( var n in names ) {
					var path = replace( n, "\", "/", "all" );
					expect( path ).notToMatch( "^org/apache/felix/", path );
					if ( left( path, 19 ) == "lucee/shaded/felix/" ) shaded++;
				}
				expect( shaded ).toBeGT( 100 );

				zip action="read" file=variables.jar entryPath="META-INF/services/org.osgi.framework.launch.FrameworkFactory" variable="local.factory";
				expect( trim( factory ) ).toBe( "lucee.shaded.felix.framework.FrameworkFactory" );
			});

			it( title="the license and notice of Felix are in the jar", skip=!len( getJar() ), body=function( currentSpec ) {
				zip action="read" file=variables.jar entryPath="META-INF/licenses/org.apache.felix.framework/LICENSE" variable="local.license";
				expect( license ).toInclude( "Apache License" );
				zip action="read" file=variables.jar entryPath="META-INF/licenses/org.apache.felix.framework/NOTICE" variable="local.notice";
				expect( notice ).toInclude( "Apache Felix" );
			});
		});
	}
}
