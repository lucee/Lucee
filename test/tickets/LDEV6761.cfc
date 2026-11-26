component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		var corejar = new test.general.CoreJar.CoreJar();
		variables.headers = corejar.getHeaders();
		// in the old build the core is a separate file, Main-Class and Bundle-Vendor are headers of the loader jar
		variables.singleJar = !corejar.isCoreFile();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6761 the manifest of the single jar has no OSGi headers", function() {

			it( title="the version and the build info are Lucee headers", body=function( currentSpec ) {
				expect( variables.headers.get( "Lucee-Version" ) ?: "" ).toBe( server.lucee.version );
				expect( variables.headers.get( "Built-Date" ) ?: "" ).notToBeEmpty();
				expect( variables.headers.get( "Minor-Name" ) ?: "" ).notToBeEmpty();
				if ( variables.singleJar ) expect( variables.headers.get( "Main-Class" ) ?: "" ).notToBeEmpty();
				expect( variables.headers.get( "Multi-Release" ) ?: "" ).toBe( "true" );
			});

			it( title="Require-Extension is still there", body=function( currentSpec ) {
				expect( variables.headers.get( "Require-Extension" ) ?: "" ).notToBeEmpty();
			});

			it( title="the OSGi headers of the former core bundle are gone", body=function( currentSpec ) {
				loop list="Bundle-Version,Bundle-Name,Bundle-SymbolicName,Bundle-ManifestVersion,Bundle-RequiredExecutionEnvironment,Bundle-ClassPath,Import-Package,Export-Package" item="local.name" {
					expect( variables.headers.get( name ) ?: "" ).toBeEmpty( name );
				}
			});

			it( title="the descriptive headers of the loader stay", body=function( currentSpec ) {
				if ( variables.singleJar ) expect( variables.headers.get( "Bundle-Vendor" ) ?: "" ).notToBeEmpty();
			});

		});
	}

}
