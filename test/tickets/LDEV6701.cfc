component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {
	function run( testResults, testBox ) {
		describe( "LDEV-6701: LuceeVersionsDetail falls back to GitHub Releases", function() {
			it( "uses the GitHub Releases download url by default", function() {
				var gh = createObject( "java", "lucee.runtime.config.maven.GitHubReleaseProvider" );
				expect( gh.DEFAULT_RELEASES_URL ).toBe( "https://github.com/lucee/Lucee/releases/download/" );
			});
			it( "builds the release asset urls for jar, light, zero and lco", function() {
				var gh = createObject( "java", "lucee.runtime.config.maven.GitHubReleaseProvider" );
				var base = "https://github.com/lucee/Lucee/releases/download/";
				expect( gh.toURL( base, "7.1.1.17-RC", "7.1.1.17-RC", "jar" ) ).toBe( base & "7.1.1.17-RC/lucee-7.1.1.17-RC.jar" );
				expect( gh.toURL( base, "7.1.1.17-RC", "7.1.1.17-RC", "light" ) ).toBe( base & "7.1.1.17-RC/lucee-light-7.1.1.17-RC.jar" );
				expect( gh.toURL( base, "7.1.1.17-RC", "7.1.1.17-RC", "zero" ) ).toBe( base & "7.1.1.17-RC/lucee-zero-7.1.1.17-RC.jar" );
				expect( gh.toURL( base, "7.1.1.17-RC", "7.1.1.17-RC", "lco" ) ).toBe( base & "7.1.1.17-RC/7.1.1.17-RC.lco" );
				expect( gh.toURL( "https://example.com/releases", "7.1.2.184", "7.1.2.184-RC", "light" ) ).toBe( "https://example.com/releases/7.1.2.184/lucee-light-7.1.2.184-RC.jar" );
				expect( isNull( gh.toURL( base, "7.1.1.17", "7.1.1.17", "unknown" ) ) ).toBeTrue();
			});
			it( "checks the tag with and without qualifier", function() {
				var gh = createObject( "java", "lucee.runtime.config.maven.GitHubReleaseProvider" );
				var tags = gh.getTags( "7.1.2.184-RC" );
				expect( tags.size() ).toBe( 2 );
				expect( tags.get( 0 ) ).toBe( "7.1.2.184-RC" );
				expect( tags.get( 1 ) ).toBe( "7.1.2.184" );
				tags = gh.getTags( "7.1.1.17" );
				expect( tags.size() ).toBe( 1 );
				expect( tags.get( 0 ) ).toBe( "7.1.1.17" );
			});
			it( "does not touch artifacts already provided", function() {
				var gh = createObject( "java", "lucee.runtime.config.maven.GitHubReleaseProvider" );
				var Version = createObject( "java", "lucee.runtime.config.maven.Version" );
				var data = createObject( "java", "java.util.LinkedHashMap" ).init();
				data.put( "jar", "https://repo1.maven.org/lucee.jar" );
				data.put( "light", "https://repo1.maven.org/lucee-light.jar" );
				data.put( "zero", "https://repo1.maven.org/lucee-zero.jar" );
				data.put( "lco", "https://repo1.maven.org/lucee.lco" );
				gh.fillMissing( Version.parseVersion( "7.1.1.17" ), data );
				expect( data.size() ).toBe( 4 );
				expect( data.get( "jar" ) ).toBe( "https://repo1.maven.org/lucee.jar" );
				expect( data.get( "light" ) ).toBe( "https://repo1.maven.org/lucee-light.jar" );
				expect( data.get( "zero" ) ).toBe( "https://repo1.maven.org/lucee-zero.jar" );
				expect( data.get( "lco" ) ).toBe( "https://repo1.maven.org/lucee.lco" );
			});
			it( "does not look up snapshots on GitHub", function() {
				var gh = createObject( "java", "lucee.runtime.config.maven.GitHubReleaseProvider" );
				var Version = createObject( "java", "lucee.runtime.config.maven.Version" );
				expect( gh.detail( Version.parseVersion( "7.1.2.200-SNAPSHOT" ) ).size() ).toBe( 0 );
			});
		});
	}
}
