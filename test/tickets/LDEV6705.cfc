component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		// the fake org.lucee artifacts served by the local repository have no Lucee signature (LDEV-1554)
		variables.sigProp = "lucee.maven.signature.verification";
		variables.orgSigPolicy = createObject( "java", "java.lang.System" ).getProperty( variables.sigProp );
		createObject( "java", "java.lang.System" ).setProperty( variables.sigProp, "off" );
		createObject( "java", "lucee.runtime.config.ConfigServerImpl" ).instance.resetMavenSignatureVerification();
		variables.root = getTempDirectory() & "LDEV6705-" & createUUID() & "/";
		variables.repo1 = root & "repo1/";
		variables.repo2 = root & "repo2/";
		directoryCreate( repo1, true );
		directoryCreate( repo2, true );

		// only in the second repository (like maven.lucee-services.com), as lco + pom
		addArtifact( repo2, "7.1.99.1", "second-lco", false, true );
		// only in the second repository, as jar + pom
		addArtifact( repo2, "7.1.99.2", "second-jar", true, false );
		// in both, the first repository wins
		addArtifact( repo1, "7.1.99.3", "first", false, true );
		addArtifact( repo2, "7.1.99.3", "second", false, true );

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
		describe( "LDEV-6705: one Maven repository list for all downloads", function() {

			it( "the shared list is the release repositories followed by the mixed repositories", function() {
				var expected = [];
				loop array=coreMUP().getDefaultRepositoryReleases() item="local.r" {
					if ( !arrayContains( expected, r.url ) ) arrayAppend( expected, r.url );
				}
				loop array=coreMUP().getDefaultRepositoryMixed() item="local.r" {
					if ( !arrayContains( expected, r.url ) ) arrayAppend( expected, r.url );
				}
				expect( toArray( coreMUP().getDefaultRepositoryURLs( false ) ) ).toBe( expected );
				expect( arrayLen( expected ) ).toBeGTE( 2 );
			});

			it( "snapshots use the snapshot repositories first, then the mixed repositories", function() {
				var expected = [];
				loop array=coreMUP().getDefaultRepositorySnapshots() item="local.r" {
					if ( !arrayContains( expected, r.url ) ) arrayAppend( expected, r.url );
				}
				loop array=coreMUP().getDefaultRepositoryMixed() item="local.r" {
					if ( !arrayContains( expected, r.url ) ) arrayAppend( expected, r.url );
				}
				expect( toArray( coreMUP().getDefaultRepositoryURLs( true ) ) ).toBe( expected );
			});

			it( "the loader (LUCEE_VERSION) uses the same list as the core", function() {
				var loaderMUP = createObject( "java", "lucee.loader.engine.mvn.MavenUpdateProvider" );
				expect( toArray( loaderMUP.getDefaultRepositoriesReleases() ) ).toBe( toArray( coreMUP().getDefaultRepositoryURLs( false ) ) );
				expect( toArray( loaderMUP.getDefaultRepositoriesSnapshots() ) ).toBe( toArray( coreMUP().getDefaultRepositoryURLs( true ) ) );
			});

			it( "the OSGi bundle provider (core) uses the same list", function() {
				var urls = [];
				loop array=createObject( "java", "lucee.runtime.config.s3.BundleProvider" ).getDefaultProviderDetailsMvn() item="local.u" {
					arrayAppend( urls, u.toExternalForm() );
				}
				var explicit = server.system.properties[ "lucee.mvn.bundle.detail" ] ?: ( server.system.environment[ "LUCEE_MVN_BUNDLE_DETAIL" ] ?: "" );
				if ( len( trim( explicit ) ) ) expect( arrayLen( urls ) ).toBe( 1 );
				else expect( urls ).toBe( toArray( coreMUP().getDefaultRepositoryURLs( false ) ) );
			});

			it( "POM dependency resolution uses the same list, in the same order", function() {
				// 8.0: POM uses the configured release repositories (MavenUpdateProvider.getReleaseRepositories)
				var pomUrls = [];
				loop array=toArray( coreMUP().getReleaseRepositories( getPageContext().getConfig() ) ) item="local.r" {
					arrayAppend( pomUrls, r.getUrl() );
				}
				var last = 0;
				loop array=toArray( coreMUP().getDefaultRepositoryURLs( false ) ) item="local.u" {
					var pos = arrayFind( pomUrls, u );
					expect( pos ).toBeGT( last, "[#u#] missing or out of order in #serializeJSON( pomUrls )#" );
					last = pos;
				}
			});

			it( "the loader finds a core (.lco) that is only in the second repository", function() {
				expect( loaderCore( "7.1.99.1" ) ).toBe( "second-lco:lco" );
			});

			it( "the loader finds a jar that is only in the second repository", function() {
				expect( loaderCore( "7.1.99.2" ) ).toBe( "second-jar:core-in-jar" );
			});

			it( "the loader uses the first repository that has the version", function() {
				expect( loaderCore( "7.1.99.3" ) ).toBe( "first:lco" );
			});

			it( "the loader fails when no repository has the version", function() {
				expect( function() {
					loaderCore( "7.1.99.9" );
				} ).toThrow();
			});
		});
	}

	private function coreMUP() {
		return createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider" );
	}

	private array function toArray( required any list ) {
		var arr = [];
		loop array=list item="local.e" {
			arrayAppend( arr, e );
		}
		return arr;
	}

	private string function loaderCore( required string version ) {
		var repos = [ baseUrl & "repo1/", baseUrl & "repo2/" ];
		var mup = createObject( "java", "lucee.loader.engine.mvn.MavenUpdateProvider" ).init(
			javacast( "String[]", repos ), javacast( "String[]", repos ), "org.lucee", "lucee" );
		var stream = mup.getCore( createObject( "java", "org.osgi.framework.Version" ).init( version ) );
		try {
			return toString( stream.readAllBytes(), "UTF-8" );
		}
		finally {
			stream.close();
		}
	}

	private function addArtifact( required string repoDir, required string version, required string marker, required boolean jar, required boolean lco ) {
		var dir = repoDir & "org/lucee/lucee/" & version & "/";
		directoryCreate( dir, true );
		var name = dir & "lucee-" & version;
		fileWrite( name & ".pom", "<project><groupId>org.lucee</groupId><artifactId>lucee</artifactId><version>#version#</version></project>" );
		if ( lco ) fileWrite( name & ".lco", marker & ":lco" );
		if ( jar ) {
			var src = root & "jarsrc-" & createUUID() & "/";
			directoryCreate( src & "core", true );
			fileWrite( src & "core/core.lco", marker & ":core-in-jar" );
			compress( "zip", src, name & ".jar", false );
			directoryDelete( src, true );
		}
	}
}
