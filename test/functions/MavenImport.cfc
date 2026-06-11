component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.pomPath = getTempDirectory() & "mvn-import-test-" & createUUID() & ".xml";
		// prime cache with everything this test suite depends on so tests don't rely on run order
		mavenLoad([
			{
				"groupId": "commons-io",
				"artifactId": "commons-io",
				"version": "2.8.0"
			},
			{
				"groupId": "org.apache.httpcomponents",
				"artifactId": "httpclient",
				"version": "4.5.13"
			}
		]);
		fileWrite( variables.pomPath, mavenExport() );
	}

	function afterAll() {
		if ( fileExists( variables.pomPath ) ) fileDelete( variables.pomPath );
	}

	function run( testResults, testBox ) {
		describe( "MavenImport() function", function() {

			it( "returns a query of imported dependencies", function() {
				var q = mavenImport( variables.pomPath );
				expect( q ).toBeTypeOf( "query" );
				expect( q.recordCount ).toBeGT( 0 );
			});

			it( "query has the MavenInfo-shape columns", function() {
				var q = mavenImport( variables.pomPath );
				var cols = listToArray( q.columnList );
				for ( var expected in [ "groupId", "artifactId", "version" ] ) {
					expect( cols ).toInclude( expected );
				}
			});

			it( "includes the round-tripped commons-io coord", function() {
				var q = mavenImport( variables.pomPath );
				var found = false;
				for ( var i = 1; i <= q.recordCount; i++ ) {
					if ( q.groupId[ i ] == "commons-io"
						&& q.artifactId[ i ] == "commons-io"
						&& q.version[ i ] == "2.8.0" ) {
						found = true;
						break;
					}
				}
				expect( found ).toBeTrue();
			});

			it( "missing pom file throws", function() {
				expect( function() {
					mavenImport( getTempDirectory() & "does-not-exist-" & createUUID() & ".xml" );
				}).toThrow();
			});

			it( "includeTransitive flag pulls nested deps when true", function() {
				var transitivePomPath = getTempDirectory() & "mvn-import-transitive-" & createUUID() & ".xml";
				fileWrite( transitivePomPath,
					'<?xml version="1.0" encoding="UTF-8"?>'
					& '<project xmlns="http://maven.apache.org/POM/4.0.0">'
					& '<modelVersion>4.0.0</modelVersion>'
					& '<groupId>com.example.lucee</groupId>'
					& '<artifactId>import-test</artifactId>'
					& '<version>0</version>'
					& '<packaging>pom</packaging>'
					& '<dependencies>'
					& '<dependency>'
					& '<groupId>org.apache.httpcomponents</groupId>'
					& '<artifactId>httpclient</artifactId>'
					& '<version>4.5.13</version>'
					& '</dependency>'
					& '</dependencies>'
					& '</project>'
				);
				try {
					var literal = mavenImport( transitivePomPath, false );
					var full = mavenImport( transitivePomPath, true );
					expect( full.recordCount ).toBeGT( literal.recordCount );
				}
				finally {
					if ( fileExists( transitivePomPath ) ) fileDelete( transitivePomPath );
				}
			});

		});
	}
}
