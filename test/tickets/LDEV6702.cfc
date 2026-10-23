component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function run( testResults, testBox ) {
		describe( title="LDEV-6702 loader Maven repository defaults", body=function() {

			it( title="release repository is not the retired oss.sonatype.org", body=function( currentSpec ) {
				var repo = createObject( "java", "lucee.loader.engine.mvn.MavenUpdateProvider" ).getDefaultRepositoryReleases();
				expect( repo ).notToInclude( "oss.sonatype.org" );
			});

			it( title="snapshot repository is not the retired oss.sonatype.org", body=function( currentSpec ) {
				var repo = createObject( "java", "lucee.loader.engine.mvn.MavenUpdateProvider" ).getDefaultRepositorySnapshots();
				expect( repo ).notToInclude( "oss.sonatype.org" );
			});

			it( title="release repository serves a known 6.2 core (.lco)", body=function( currentSpec ) {
				var repo = createObject( "java", "lucee.loader.engine.mvn.MavenUpdateProvider" ).getDefaultRepositoryReleases();
				if ( right( repo, 1 ) != "/" ) repo &= "/";
				http url="#repo#org/lucee/lucee/6.2.9.4/lucee-6.2.9.4.lco" method="head" timeout="30" result="local.res";
				expect( res.status_code ).toBe( 200 );
			});

			it( title="snapshot repository serves the lucee maven-metadata.xml", body=function( currentSpec ) {
				var repo = createObject( "java", "lucee.loader.engine.mvn.MavenUpdateProvider" ).getDefaultRepositorySnapshots();
				if ( right( repo, 1 ) != "/" ) repo &= "/";
				http url="#repo#org/lucee/lucee/maven-metadata.xml" method="get" timeout="30" result="local.res";
				expect( res.status_code ).toBe( 200 );
			});

		});
	}

}
