component extends="org.lucee.cfml.test.LuceeTestCase" {

	// commons-logging 1.2 declares log4j, logkit, avalon-framework and servlet-api as <optional>true</optional>

	function beforeAll() {
		variables.pomPath = getTempDirectory() & "ldev6639-" & createUUID() & ".xml";
		fileWrite( variables.pomPath, '<?xml version="1.0" encoding="UTF-8"?>'
			& '<project xmlns="http://maven.apache.org/POM/4.0.0">'
			& '<modelVersion>4.0.0</modelVersion>'
			& '<groupId>org.lucee.test</groupId><artifactId>ldev6639</artifactId><version>1.0.0</version>'
			& '<dependencies>'
			& '<dependency><groupId>commons-logging</groupId><artifactId>commons-logging</artifactId><version>1.2</version></dependency>'
			& '<dependency><groupId>commons-codec</groupId><artifactId>commons-codec</artifactId><version>1.15</version><optional>true</optional></dependency>'
			& '</dependencies>'
			& '</project>' );
	}

	function afterAll() {
		if ( fileExists( variables.pomPath ) ) fileDelete( variables.pomPath );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6639 optional Maven dependencies", function() {

			it( "mavenLoad does not load optional dependencies of the requested artifact", function() {
				var jars = mavenLoad( [ "commons-logging:commons-logging:1.2" ] );
				expect( len( jars ) ).toBe( 1, serializeJSON( jars ) );
				expect( listLast( jars[ 1 ], "/\" ) ).toBe( "commons-logging-1.2.jar" );
			});

			it( "mavenImport skips transitive optional dependencies", function() {
				var q = mavenImport( variables.pomPath, true );
				var ids = valueArray( q, "artifactId" );
				expect( ids ).toInclude( "commons-logging" );
				expect( ids ).notToInclude( "log4j" );
				expect( ids ).notToInclude( "logkit" );
				expect( ids ).notToInclude( "avalon-framework" );
			});

			it( "mavenImport keeps optional dependencies declared directly in the pom", function() {
				var q = mavenImport( variables.pomPath, true );
				expect( valueArray( q, "artifactId" ) ).toInclude( "commons-codec" );
			});

			it( "mavenLoad includeOptional=true also loads optional dependencies", function() {
				var jars = mavenLoad( [ "commons-logging:commons-logging:1.2" ], true );
				var names = [];
				arrayEach( jars, function( j ) { arrayAppend( names, listLast( j, "/\" ) ); } );
				expect( len( jars ) ).toBeGT( 1, serializeJSON( names ) );
				expect( arrayToList( names ) ).toInclude( "logkit-" );
				expect( arrayToList( names ) ).toInclude( "avalon-framework-" );
			});

			it( "mavenLoad includeOptional=true restores the previous jar counts", function() {
				expect( len( mavenLoad( [ "org.apache.httpcomponents:httpclient:4.5.13" ], true ) ) ).toBe( 10 );
				expect( len( mavenLoad( [ "ch.qos.logback:logback-classic:1.2.11" ], true ) ) ).toBe( 9 );
				expect( len( mavenLoad( [ "org.apache.httpcomponents:httpclient:4.5.13" ], false ) ) ).toBe( 4 );
				expect( len( mavenLoad( [ "ch.qos.logback:logback-classic:1.2.11" ], false ) ) ).toBe( 3 );
			});

			it( "mavenImport includeOptional=true also returns transitive optional dependencies", function() {
				var ids = valueArray( mavenImport( variables.pomPath, true, true ), "artifactId" );
				expect( ids ).toInclude( "commons-logging" );
				expect( ids ).toInclude( "commons-codec" );
				expect( ids ).toInclude( "logkit" );
				expect( ids ).toInclude( "avalon-framework" );
			});

			it( "mavenImport includeOptional is ignored without includeTransitive", function() {
				var ids = valueArray( mavenImport( variables.pomPath, false, true ), "artifactId" );
				expect( ids ).toInclude( "commons-logging" );
				expect( ids ).notToInclude( "logkit" );
			});

		});
	}
}
