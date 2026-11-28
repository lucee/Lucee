component extends="org.lucee.cfml.test.LuceeTestCase" labels="maven" {

	function beforeAll() {
		variables.root = getTempDirectory() & "LDEV7027-" & createUUID() & "/";
		variables.repo = root & "repo/";
		variables.cache = root & "cache/";
		variables.patches = root & "patches/";
		directoryCreate( repo, true );
		directoryCreate( cache, true );
		directoryCreate( patches, true );

		variables.versions = [
			"4.5.0.2",
			"7.0.0.202",
			"7.1.99", "7.1.99.1", "7.1.99.2-SNAPSHOT", "7.1.99.2-RC", "7.1.99.3-ALPHA", "7.1.99.4-FOO",
			"7.2.0.1-SNAPSHOT", "7.2.0.1-RC",
			"8.0.99.1", "8.0.99.2-SNAPSHOT"
		];
		writeMetadata( repo, versions );

		// valid checksum
		addCore( repo, "7.1.99.1", "core-7.1.99.1", hash( "core-7.1.99.1", "SHA-1" ) );

		var OutputLevel = createObject( "java", "com.sun.net.httpserver.SimpleFileServer$OutputLevel" );
		var addr = createObject( "java", "java.net.InetSocketAddress" ).init( createObject( "java", "java.net.InetAddress" ).getLoopbackAddress(), 0 );
		variables.httpServer = createObject( "java", "com.sun.net.httpserver.SimpleFileServer" )
			.createFileServer( addr, createObject( "java", "java.io.File" ).init( root ).toPath(), OutputLevel.NONE );
		httpServer.start();
		variables.baseUrl = "http://127.0.0.1:" & httpServer.getAddress().getPort() & "/";
	}

	function afterAll() {
		if ( !isNull( variables.httpServer ) ) httpServer.stop( 0 );
		if ( directoryExists( variables.root ) ) directoryDelete( variables.root, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-7027: core update check and download via Maven instead of the update provider", function() {

			it( "a stable installation gets stable versions and pre-releases of the same major.minor, never ALPHA", function() {
				// 7.1.99.2-RC beats the snapshot of the same build, the ALPHA and the 7.2 pre-releases are not offered
				expect( select( "7.1.99.1" ) ).toBe( "7.1.99.2-RC" );
				// 7.0.0.202 is hidden, 7.1 pre-releases are another minor
				expect( select( "7.0.0.100" ) ).toBe( "7.1.99.1" );
				// same major.minor snapshot, like the update provider
				expect( select( "8.0.99.1" ) ).toBe( "8.0.99.2-SNAPSHOT" );
			});

			it( "a pre-release installation gets any newer version of the same major", function() {
				expect( select( "7.1.99.2-SNAPSHOT" ) ).toBe( "7.2.0.1-RC" );
				expect( select( "7.1.99.3-ALPHA" ) ).toBe( "7.2.0.1-RC" );
				expect( select( "7.0.0.1-RC" ) ).toBe( "7.2.0.1-RC" );
				expect( select( "8.0.99.2-SNAPSHOT" ) ).toBe( "" );
			});

			it( "only versions the update provider offers, no update below 5.0.0.254", function() {
				expect( select( "4.5.0.1" ) ).toBe( "" );
				expect( select( "7.1.99.3-SNAPSHOT" ) ).toBe( "7.2.0.1-RC" );
				expect( isNull( MUP().selectUpdate( toList( [ "7.1.99.4-FOO", "7.1.100" ] ), toVersion( "7.1.99.3-SNAPSHOT" ) ) ) ).toBeTrue();
			});

			it( "orders like the update provider: SNAPSHOT < BETA < RC < stable for the same build", function() {
				expect( MUP().selectUpdate( toList( [ "7.1.99.2-SNAPSHOT", "7.1.99.2-RC", "7.1.99.2-BETA" ] ), toVersion( "7.1.99.1-SNAPSHOT" ) ).toString() ).toBe( "7.1.99.2-RC" );
				expect( MUP().selectUpdate( toList( [ "7.1.99.2-RC", "7.1.99.2" ] ), toVersion( "7.1.99.2-RC" ) ).toString() ).toBe( "7.1.99.2" );
				expect( isNull( MUP().selectUpdate( toList( [ "7.1.99.2-RC" ] ), toVersion( "7.1.99.2" ) ) ) ).toBeTrue();
			});

			it( "updateFor reads the versions from the repository metadata", function() {
				expect( provider().updateFor( toVersion( "7.1.99.1" ) ).toString() ).toBe( "7.1.99.2-RC" );
				expect( provider().updateFor( toVersion( "7.1.99.2-SNAPSHOT" ) ).toString() ).toBe( "7.2.0.1-RC" );
				expect( isNull( provider().updateFor( toVersion( "8.0.99.2-SNAPSHOT" ) ) ) ).toBeTrue();
			});

			it( "the update type picks the repositories", function() {
				var MUPc = MUP();
				var rel = urls( MUPc.getDefaultRepositoryReleases() ) ;
				rel = listAppend( rel, urls( MUPc.getDefaultRepositoryMixed() ) );
				var snap = listAppend( urls( MUPc.getDefaultRepositorySnapshots() ), rel );
				expect( urls( MUPc.getCoreUpdateRepositories( toVersion( "7.1.99.1" ) ) ) ).toBe( dedupe( rel ) );
				for ( var q in [ "-SNAPSHOT", "-RC", "-BETA", "-ALPHA" ] ) {
					expect( urls( MUPc.getCoreUpdateRepositories( toVersion( "7.1.99.1" & q ) ) ) ).toBe( dedupe( snap ) );
				}
				// every repository only once
				var repos = MUPc.getCoreUpdateRepositories( toVersion( "7.1.99.1-RC" ) );
				expect( repos.size() ).toBe( listLen( urls( repos ) ) );
			});

			it( "downloadCore stores the verified .lco in the given directory", function() {
				var file = provider().downloadCore( toVersion( "7.1.99.1" ), createObject( "java", "java.io.File" ).init( patches ) );
				expect( file.getName() ).toBe( "7.1.99.1.lco" );
				expect( fileRead( file.getAbsolutePath() ) ).toBe( "core-7.1.99.1" );
				expect( fileExists( patches & "7.1.99.1.lco.download" ) ).toBeFalse();
			});

			it( "downloadCore fails for a version that does not exist and leaves no file behind", function() {
				expect( function() {
					provider().downloadCore( toVersion( "7.1.99.9" ), createObject( "java", "java.io.File" ).init( patches ) );
				} ).toThrow();
				expect( fileExists( patches & "7.1.99.9.lco" ) ).toBeFalse();
				expect( fileExists( patches & "7.1.99.9.lco.download" ) ).toBeFalse();
			});

			it( "the admin action getCoreUpdate returns the result", function() {
				admin action="getCoreUpdate" type="server" password=request.SERVERADMINPASSWORD otherVersions=true returnVariable="local.res";
				expect( res ).toHaveKey( "hasUpdate" );
				expect( res.otherVersions ).toBeArray();
				expect( res ).toHaveKey( "availableVersion" );
				expect( res.current ).toBe( server.lucee.version );
			});
		});
	}

	private string function select( required string current ) {
		var v = MUP().selectUpdate( toList( variables.versions ), toVersion( current ) );
		return isNull( v ) ? "" : v.toString();
	}

	// the distinct urls, in order
	private string function urls( required any repos ) {
		var list = "";
		for ( var r in repos ) {
			if ( !listFind( list, r.url ) ) list = listAppend( list, r.url );
		}
		return list;
	}

	private string function dedupe( required string list ) {
		var res = "";
		for ( var u in listToArray( list ) ) {
			if ( !listFind( res, u ) ) res = listAppend( res, u );
		}
		return res;
	}

	private function toList( required array versions ) {
		var list = createObject( "java", "java.util.ArrayList" ).init();
		for ( var v in versions ) list.add( toVersion( v ) );
		return list;
	}

	private function writeMetadata( required string repoDir, required array versions ) {
		var dir = repoDir & "org/lucee/lucee/";
		directoryCreate( dir, true );
		var xml = "<?xml version=""1.0"" encoding=""UTF-8""?><metadata><groupId>org.lucee</groupId><artifactId>lucee</artifactId><versioning><versions>";
		for ( var v in versions ) xml &= "<version>#v#</version>";
		xml &= "</versions></versioning></metadata>";
		fileWrite( dir & "maven-metadata.xml", xml );
	}

	private function addCore( required string repoDir, required string version, required string content, required string sha1 ) {
		var dir = repoDir & "org/lucee/lucee/" & version & "/";
		directoryCreate( dir, true );
		var name = dir & "lucee-" & version;
		fileWrite( name & ".pom", "<project><groupId>org.lucee</groupId><artifactId>lucee</artifactId><version>#version#</version></project>" );
		fileWrite( name & ".lco", content );
		fileWrite( name & ".lco.sha1", lcase( sha1 ) );
	}

	private function MUP() {
		return createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider" );
	}

	private function provider() {
		var repos = createObject( "java", "java.util.ArrayList" ).init();
		repos.add( repository( baseUrl & "repo/" ) );
		return MUP().init( repos, "org.lucee", "lucee" );
	}

	private function repository( required string repoUrl ) {
		var Repository = createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider$Repository" );
		// no caching, every call asks the repository
		return Repository.init( "LDEV-7027", arguments.repoUrl, Repository.TIMEOUT_ZERO, Repository.TIMEOUT_ZERO, getPageContext().getConfig().getResource( cache ) );
	}

	private function toVersion( required string version ) {
		return createObject( "java", "lucee.runtime.config.maven.Version" ).parseVersion( version );
	}
}
