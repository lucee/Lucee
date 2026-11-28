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
			"7.1.99.1-SNAPSHOT", "7.1.99.1-ALPHA", "7.1.99.1",
			"7.1.99.2-SNAPSHOT", "7.1.99.2-BETA", "7.1.99.2-RC", "7.1.99.2",
			"7.1.99.3-SNAPSHOT", "7.1.99.3-RC",
			"7.1.100.1-SNAPSHOT",
			"8.0.99.1-SNAPSHOT", "8.0.99.1"
		];
		writeMetadata( repo, versions );

		// valid checksum
		addCore( repo, "7.1.99.2", "core-7.1.99.2", hash( "core-7.1.99.2", "SHA-1" ) );
		// checksum does not match the content
		addCore( repo, "7.1.99.3-RC", "core-7.1.99.3-RC", hash( "something else", "SHA-1" ) );

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

			it( "stable installations only get RC or stable versions of the same major", function() {
				expect( select( "7.1.99.1" ) ).toBe( "7.1.99.3-RC" );
				expect( select( "7.1.99.2" ) ).toBe( "7.1.99.3-RC" );
				expect( select( "8.0.99.1" ) ).toBe( "" );
			});

			it( "BETA/RC installations get snapshots only of the same minor version", function() {
				// the RC is ranked above the snapshot of the same version
				expect( select( "7.1.99.2-RC" ) ).toBe( "7.1.99.3-RC" );
				expect( select( "7.1.99.2-BETA" ) ).toBe( "7.1.99.3-RC" );
				expect( MUP().selectUpdate( toList( [ "7.1.99.2-RC", "7.1.99.3-SNAPSHOT", "7.1.100.1-SNAPSHOT" ] ), toVersion( "7.1.99.2-RC" ) ).toString() )
					.toBe( "7.1.99.3-SNAPSHOT" );
			});

			it( "snapshot installations get snapshots or newer, never ALPHA", function() {
				expect( select( "7.1.99.3-SNAPSHOT" ) ).toBe( "7.1.100.1-SNAPSHOT" );
				expect( select( "7.1.99.1-SNAPSHOT" ) ).toBe( "7.1.100.1-SNAPSHOT" );
				expect( isNull( MUP().selectUpdate( toList( [ "7.1.99.2-ALPHA" ] ), toVersion( "7.1.99.1-SNAPSHOT" ) ) ) ).toBeTrue();
			});

			it( "ALPHA installations get anything newer of the same major", function() {
				expect( select( "7.1.99.1-ALPHA" ) ).toBe( "7.1.100.1-SNAPSHOT" );
			});

			it( "stable is newer than a pre release of the same version", function() {
				expect( MUP().selectUpdate( toList( [ "7.1.99.2-RC", "7.1.99.2" ] ), toVersion( "7.1.99.2-RC" ) ).toString() ).toBe( "7.1.99.2" );
				expect( isNull( MUP().selectUpdate( toList( [ "7.1.99.2-RC" ] ), toVersion( "7.1.99.2" ) ) ) ).toBeTrue();
			});

			it( "updateFor reads the versions from the repository metadata", function() {
				expect( provider().updateFor( toVersion( "7.1.99.1" ) ).toString() ).toBe( "7.1.99.3-RC" );
				expect( provider().updateFor( toVersion( "7.1.99.1-SNAPSHOT" ) ).toString() ).toBe( "7.1.100.1-SNAPSHOT" );
				expect( isNull( provider().updateFor( toVersion( "8.0.99.1" ) ) ) ).toBeTrue();
			});

			it( "downloadCore stores the verified .lco in the given directory", function() {
				var file = provider().downloadCore( toVersion( "7.1.99.2" ), createObject( "java", "java.io.File" ).init( patches ) );
				expect( file.getName() ).toBe( "7.1.99.2.lco" );
				expect( fileRead( file.getAbsolutePath() ) ).toBe( "core-7.1.99.2" );
				expect( fileExists( patches & "7.1.99.2.lco.download" ) ).toBeFalse();
			});

			it( "downloadCore fails on a checksum mismatch and leaves no file behind", function() {
				expect( function() {
					provider().downloadCore( toVersion( "7.1.99.3-RC" ), createObject( "java", "java.io.File" ).init( patches ) );
				} ).toThrow();
				expect( fileExists( patches & "7.1.99.3-RC.lco" ) ).toBeFalse();
				expect( fileExists( patches & "7.1.99.3-RC.lco.download" ) ).toBeFalse();
			});

			it( "the admin action getCoreUpdate returns the result", function() {
				admin action="getCoreUpdate" type="server" password=request.SERVERADMINPASSWORD returnVariable="local.res";
				expect( res ).toHaveKey( "hasUpdate" );
				expect( res ).toHaveKey( "availableVersion" );
				expect( res.current ).toBe( server.lucee.version );
			});
		});
	}

	private string function select( required string current ) {
		var v = MUP().selectUpdate( toList( variables.versions ), toVersion( current ) );
		return isNull( v ) ? "" : v.toString();
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
		var repoA = repository( baseUrl & "repo/" );
		var repos = createObject( "java", "java.lang.reflect.Array" ).newInstance( repoA.getClass(), 1 );
		repos[ 1 ] = repoA;
		return MUP().init( repos, "org.lucee", "lucee" );
	}

	private function repository( required string repoUrl ) {
		var Repository = createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider$Repository" );
		// no caching, every call asks the repository
		return Repository.init( "LDEV-7027", arguments.repoUrl, Repository.TYPE_ALL, Repository.TIMEOUT_ZERO, Repository.TIMEOUT_ZERO, getPageContext().getConfig().getResource( cache ) );
	}

	private function toVersion( required string version ) {
		return createObject( "java", "lucee.runtime.config.maven.Version" ).parseVersion( version );
	}
}
