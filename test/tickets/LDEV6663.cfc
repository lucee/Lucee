component extends="org.lucee.cfml.test.LuceeTestCase" labels="mvn" {

	function beforeAll() {
		variables.dir = getTempDirectory() & "ldev6663-" & createUUID() & "/";
		directoryCreate( variables.dir );
	}

	function afterAll() {
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6663 transient Maven download failures must not be negative-cached", body=function() {

			it( title="a network error (connection refused) does not write a .lastUpdated file", body=function( currentSpec ) {
				var res = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" ).toResourceNotExisting( getPageContext(), variables.dir );
				var pom = createObject( "java", "lucee.runtime.mvn.POM" ).getInstance( res, "org.lucee.test", "ldev6663", "1.0.0", javaCast( "null", "" ) );
				// nothing listens on the loopback discard port, so every attempt fails with a network exception
				var repo = createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider$Repository" ).init( "LDEV-6663 unreachable", "http://127.0.0.1:9/", 0, 0, 0 );
				var repos = createObject( "java", "java.util.ArrayList" ).init();
				repos.add( repo );
				var MavenUtil = createObject( "java", "lucee.runtime.mvn.MavenUtil" );

				var first = "";
				try {
					MavenUtil.download( pom, repos, "pom", javaCast( "null", "" ) );
				}
				catch ( any e ) {
					first = e.message;
				}
				expect( first ).toInclude( "Failed to download" );

				var lastUpdated = variables.dir & "org/lucee/test/ldev6663/1.0.0/ldev6663-1.0.0.pom.lastUpdated";
				expect( fileExists( lastUpdated ) ).toBeFalse( "transient failure must not be negative-cached" );

				// a second attempt must really try again, not fail from the negative cache
				var second = "";
				try {
					MavenUtil.download( pom, repos, "pom", javaCast( "null", "" ) );
				}
				catch ( any e ) {
					second = e.message;
				}
				expect( second ).notToInclude( "is cached for" );
			});

		});
	}

}
