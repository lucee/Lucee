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
		describe( "MavenExists() function", function() {

			it( "returns true for a cached coord (g, a, v)", function() {
				expect( mavenExists( "commons-io", "commons-io", "2.8.0" ) ).toBeTrue();
			});

			it( "returns true for a cached coord with version omitted (any version)", function() {
				expect( mavenExists( "commons-io", "commons-io" ) ).toBeTrue();
			});

			it( "returns false for an uncached coord", function() {
				expect( mavenExists( "com.example.nope", "does-not-exist", "1.0.0" ) ).toBeFalse();
			});

			it( "returns false for cached artifact with wrong version", function() {
				expect( mavenExists( "commons-io", "commons-io", "0.0.0-notcached" ) ).toBeFalse();
			});

			it( "accepts gradle-style g:a:v string", function() {
				expect( mavenExists( "commons-io:commons-io:2.8.0" ) ).toBeTrue();
			});

			it( "accepts gradle-style g:a string (any version)", function() {
				expect( mavenExists( "commons-io:commons-io" ) ).toBeTrue();
			});

			it( "gradle string without colon throws", function() {
				expect( function() {
					mavenExists( "not-a-gradle-coord" );
				}).toThrow();
			});

			it( "empty groupId throws", function() {
				expect( function() {
					mavenExists( "", "commons-io", "2.8.0" );
				}).toThrow();
			});

		});
	}
}
