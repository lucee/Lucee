component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		mavenLoad([
			{
				"groupId": "commons-io",
				"artifactId": "commons-io",
				"version": "2.8.0"
			}
		]);
	}

	function run( testResults, testBox ) {
		describe( "MavenExport() function", function() {

			it( "returns a string containing the pom XML", function() {
				var pom = mavenExport();
				expect( pom ).toBeTypeOf( "string" );
				expect( pom ).toInclude( "<project" );
			});

			it( "returned string parses as valid XML with a <project> root", function() {
				var xml = xmlParse( mavenExport() );
				expect( xml.XmlRoot.XmlName ).toBe( "project" );
			});

			it( "declares packaging=pom (signals synthetic manifest)", function() {
				var xml = xmlParse( mavenExport() );
				expect( xml.XmlRoot.packaging.XmlText ).toBe( "pom" );
			});

			it( "includes the primed coord as a <dependency>", function() {
				var xml = xmlParse( mavenExport() );
				expect( xml.XmlRoot.dependencies.XmlChildren ).notToBeEmpty();
				var found = false;
				for ( var dep in xml.XmlRoot.dependencies.XmlChildren ) {
					if ( dep.groupId.XmlText == "commons-io"
						&& dep.artifactId.XmlText == "commons-io"
						&& dep.version.XmlText == "2.8.0" ) {
						found = true;
						break;
					}
				}
				expect( found ).toBeTrue();
			});

			it( "round-trips via fileWrite + mavenImport", function() {
				var pomPath = getTempDirectory() & "mvn-export-roundtrip-" & createUUID() & ".xml";
				try {
					fileWrite( pomPath, mavenExport() );
					var q = mavenImport( pomPath );
					expect( q.recordCount ).toBeGT( 0 );
				}
				finally {
					if ( fileExists( pomPath ) ) fileDelete( pomPath );
				}
			});

		});
	}
}
