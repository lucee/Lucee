component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.headers = new test.general.CoreJar.CoreJar().getHeaders();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6761 the manifest of the single jar has no OSGi headers", function() {

			it( title="the version and the build info are Lucee headers", body=function( currentSpec ) {
				expect( variables.headers.get( "Lucee-Version" ) ?: "" ).toBe( server.lucee.version );
				expect( variables.headers.get( "Built-Date" ) ?: "" ).notToBeEmpty();
				expect( variables.headers.get( "Minor-Name" ) ?: "" ).notToBeEmpty();
				expect( variables.headers.get( "Main-Class" ) ?: "" ).notToBeEmpty();
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
				expect( variables.headers.get( "Bundle-Vendor" ) ?: "" ).notToBeEmpty();
			});

		});
	}

}
