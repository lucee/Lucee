component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		// the fake org.lucee artifacts served by the local repository have no Lucee signature (LDEV-1554)
		variables.sigProp = "lucee.maven.signature.verification";
		variables.orgSigPolicy = createObject( "java", "java.lang.System" ).getProperty( variables.sigProp );
		createObject( "java", "java.lang.System" ).setProperty( variables.sigProp, "off" );
		createObject( "java", "lucee.runtime.config.ConfigServerImpl" ).instance.resetMavenSignatureVerification();
		variables.root = getTempDirectory() & "LDEV6704-" & createUUID() & "/";
		variables.repo1 = root & "repo1/";
		variables.repo2 = root & "repo2/";
		variables.cache = root & "cache/";
		directoryCreate( repo1, true );
		directoryCreate( repo2, true );
		directoryCreate( cache, true );

		// lco + pom only, no jar
		addArtifact( repo1, "7.1.99.1", "lco-only", false, true );
		// jar + pom only, no lco, the core comes from core/core.lco inside the jar
		addArtifact( repo1, "7.1.99.2", "jar-only", true, false );
		// jar, lco and pom, the lco is used
		addArtifact( repo1, "7.1.99.3", "both", true, true );
		// only in the second repository, as lco + pom
		addArtifact( repo2, "7.1.99.4", "second-repo", false, true );
		// lco + pom in the first repository, the jar only in the second one
		addArtifact( repo1, "7.1.99.5", "split", false, true );
		addArtifact( repo2, "7.1.99.5", "split", true, false );

		var OutputLevel = createObject( "java", "com.sun.net.httpserver.SimpleFileServer$OutputLevel" );
		var addr = createObject( "java", "java.net.InetSocketAddress" ).init( createObject( "java", "java.net.InetAddress" ).getLoopbackAddress(), 0 );
		variables.httpServer = createObject( "java", "com.sun.net.httpserver.SimpleFileServer" )
			.createFileServer( addr, createObject( "java", "java.io.File" ).init( root ).toPath(), OutputLevel.NONE );
		httpServer.start();
		variables.baseUrl = "http://127.0.0.1:" & httpServer.getAddress().getPort() & "/";
	}

	function afterAll() {
		if ( isNull( variables.orgSigPolicy ) ) createObject( "java", "java.lang.System" ).clearProperty( variables.sigProp );
		else createObject( "java", "java.lang.System" ).setProperty( variables.sigProp, variables.orgSigPolicy );
		createObject( "java", "lucee.runtime.config.ConfigServerImpl" ).instance.resetMavenSignatureVerification();
		if ( !isNull( variables.httpServer ) ) httpServer.stop( 0 );
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6704: core update looks for the .lco first and does not require the jar", function() {

			it( "finds a version that only has .lco and pom", function() {
				var detail = provider().detail( toVersion( "7.1.99.1" ), "jar", true );
				expect( detail ).toHaveKey( "lco" );
				expect( detail ).toHaveKey( "pom" );
				expect( detail ).notToHaveKey( "jar" );
				expect( detail[ "lco" ] ).toBe( baseUrl & "repo1/org/lucee/lucee/7.1.99.1/lucee-7.1.99.1.lco" );
			});

			it( "downloads the core from the .lco when there is no jar", function() {
				expect( readCore( "7.1.99.1" ) ).toBe( "lco-only:lco" );
			});

			it( "still works for a version that only has the jar, the core is extracted from it", function() {
				var detail = provider().detail( toVersion( "7.1.99.2" ), "jar", true );
				expect( detail ).toHaveKey( "jar" );
				expect( detail ).notToHaveKey( "lco" );
				expect( readCore( "7.1.99.2" ) ).toBe( "jar-only:core-in-jar" );
			});

			it( "prefers the .lco when both exist", function() {
				var detail = provider().detail( toVersion( "7.1.99.3" ), "jar", true );
				expect( detail ).toHaveKey( "jar" );
				expect( detail ).toHaveKey( "lco" );
				expect( readCore( "7.1.99.3" ) ).toBe( "both:lco" );
			});

			it( "checks every configured repository in order", function() {
				var detail = provider().detail( toVersion( "7.1.99.4" ), "jar", true );
				expect( detail[ "lco" ] ).toBe( baseUrl & "repo2/org/lucee/lucee/7.1.99.4/lucee-7.1.99.4.lco" );
				expect( readCore( "7.1.99.4" ) ).toBe( "second-repo:lco" );
			});

			it( "fails when neither .lco nor jar exist", function() {
				expect( function() {
					provider().detail( toVersion( "7.1.99.9" ), "jar", true );
				} ).toThrow();
				expect( isNull( provider().detail( toVersion( "7.1.99.9" ), "jar", false ) ) ).toBeTrue();
			});

			it( "getLoader finds the jar in a later repository when the first one only has the .lco", function() {
				expect( readCore( "7.1.99.5" ) ).toBe( "split:lco" );
				var stream = provider().getLoader( toVersion( "7.1.99.5" ) );
				try {
					var zip = getTempDirectory() & "LDEV6704-" & createUUID() & ".jar";
					fileWrite( zip, stream.readAllBytes() );
				}
				finally {
					stream.close();
				}
				try {
					expect( fileRead( "zip://" & zip & "!/core/core.lco" ) ).toBe( "split:core-in-jar" );
				}
				finally {
					fileDelete( zip );
				}
			});

			it( "getLoader still requires the jar", function() {
				expect( function() {
					provider().getLoader( toVersion( "7.1.99.1" ) );
				} ).toThrow();
			});
		});
	}

	private function addArtifact( required string repoDir, required string version, required string marker, required boolean jar, required boolean lco ) {
		var dir = repoDir & "org/lucee/lucee/" & version & "/";
		directoryCreate( dir, true );
		var name = dir & "lucee-" & version;
		fileWrite( name & ".pom", "<project><groupId>org.lucee</groupId><artifactId>lucee</artifactId><version>#version#</version></project>" );
		if ( lco ) fileWrite( name & ".lco", marker & ":lco" );
		if ( jar ) {
			var src = root & "jarsrc-" & version & "/";
			directoryCreate( src & "core", true );
			fileWrite( src & "core/core.lco", marker & ":core-in-jar" );
			compress( "zip", src, name & ".jar", false );
			directoryDelete( src, true );
		}
	}

	private function provider() {
		var repoA = repository( baseUrl & "repo1/" );
		var repos = createObject( "java", "java.lang.reflect.Array" ).newInstance( repoA.getClass(), 2 );
		repos[ 1 ] = repoA;
		repos[ 2 ] = repository( baseUrl & "repo2/" );
		return createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider" ).init( repos, "org.lucee", "lucee" );
	}

	private function repository( required string repoUrl ) {
		var Repository = createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider$Repository" );
		// no caching, every call asks the repository
		return Repository.init( "LDEV-6704", arguments.repoUrl, Repository.TYPE_ALL, Repository.TIMEOUT_ZERO, Repository.TIMEOUT_ZERO, getPageContext().getConfig().getResource( cache ) );
	}

	private function toVersion( required string version ) {
		return createObject( "java", "lucee.runtime.config.maven.Version" ).parseVersion( version );
	}

	private string function readCore( required string version ) {
		var stream = provider().getCore( toVersion( version ) );
		try {
			return toString( stream.readAllBytes(), "UTF-8" );
		}
		finally {
			stream.close();
		}
	}
}
