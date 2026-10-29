component extends="org.lucee.cfml.test.LuceeTestCase" labels="mvn" {

	function beforeAll() {
		variables.fixtures = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV1554/";
		variables.tmp = getTempDirectory() & "ldev1554-" & createUUID() & "/";
		directoryCreate( variables.tmp );
		variables.Verifier = createObject( "java", "lucee.loader.util.OpenPGPVerifier" );
		variables.ArtifactSignature = createObject( "java", "lucee.runtime.mvn.ArtifactSignature" );
		variables.ConfigServerImpl = createObject( "java", "lucee.runtime.config.ConfigServerImpl" );
		variables.sysProp = "lucee.maven.signature.verification";
		variables.server = startServer();
	}

	function afterAll() {
		if ( !isNull( variables.server ) ) variables.server.stop( 0 );
		if ( directoryExists( variables.tmp ) ) directoryDelete( variables.tmp, true );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-1554 verify the OpenPGP signature of released Lucee artifacts", body=function() {

			describe( "the bundled Lucee release key", function() {

				it( title="is the Lucee release key", body=function( currentSpec ) {
					var keys = variables.ArtifactSignature.getKeys();
					expect( keys.size() ).toBe( 1 );
					expect( keys[ 1 ].getFingerprint() ).toBe( "26BA3E8B6A185070F538B1C15C3F925A060D5930" );
				});

				it( title="verifies a real artifact signature from Maven Central", body=function( currentSpec ) {
					var key = variables.Verifier.verify( javaFile( pomFixture() ), fileReadBinary( pomFixture() & ".asc" ), variables.ArtifactSignature.getKeys() );
					expect( key.getKeyId() ).toBe( "5C3F925A060D5930" );
				});

				it( title="rejects an artifact that was modified", body=function( currentSpec ) {
					var tampered = variables.tmp & "tampered.pom";
					fileWrite( tampered, fileRead( pomFixture() ) & " " );
					expectRejected( function() {
						variables.Verifier.verify( javaFile( tampered ), fileReadBinary( pomFixture() & ".asc" ), variables.ArtifactSignature.getKeys() );
					}, "does not match" );
				});

				it( title="rejects a signature of another key", body=function( currentSpec ) {
					expectRejected( function() {
						variables.Verifier.verify( javaFile( variables.fixtures & "data.txt" ), fileReadBinary( variables.fixtures & "data.txt.asc" ), variables.ArtifactSignature.getKeys() );
					}, "not a trusted key" );
				});

				it( title="rejects an empty signature and garbage", body=function( currentSpec ) {
					var f = javaFile( pomFixture() );
					var keys = variables.ArtifactSignature.getKeys();
					expectRejected( function() {
						variables.Verifier.verify( f, charsetDecode( "", "utf-8" ), keys );
					}, "empty" );
					expectRejected( function() {
						variables.Verifier.verify( f, charsetDecode( "this is not a signature", "utf-8" ), keys );
					}, "" );
					expectRejected( function() {
						variables.Verifier.verify( f, charsetDecode( "-----BEGIN PGP SIGNATURE-----" & chr( 10 ) & chr( 10 ) & "AAAA" & chr( 10 ) & "-----END PGP SIGNATURE-----", "utf-8" ), keys );
					}, "" );
				});

			});

			describe( "the verifier", function() {

				it( title="verifies armored and binary signatures of any RSA key", body=function( currentSpec ) {
					var keys = variables.Verifier.readKeys( createObject( "java", "java.io.FileInputStream" ).init( variables.fixtures & "test-key.asc" ) );
					expect( keys.size() ).toBe( 1 );
					var f = javaFile( variables.fixtures & "data.txt" );
					// SHA-256, armored
					variables.Verifier.verify( f, fileReadBinary( variables.fixtures & "data.txt.asc" ), keys );
					// SHA-512, binary
					variables.Verifier.verify( f, fileReadBinary( variables.fixtures & "data.txt.sig" ), keys );
				});

				it( title="rejects modified data", body=function( currentSpec ) {
					var keys = variables.Verifier.readKeys( createObject( "java", "java.io.FileInputStream" ).init( variables.fixtures & "test-key.asc" ) );
					var tampered = variables.tmp & "data.txt";
					fileWrite( tampered, fileRead( variables.fixtures & "data.txt" ) & "x" );
					expectRejected( function() {
						variables.Verifier.verify( javaFile( tampered ), fileReadBinary( variables.fixtures & "data.txt.asc" ), keys );
					}, "does not match" );
				});

				it( title="rejects a signature that uses SHA-1", body=function( currentSpec ) {
					var keys = variables.Verifier.readKeys( createObject( "java", "java.io.FileInputStream" ).init( variables.fixtures & "test-key.asc" ) );
					expectRejected( function() {
						variables.Verifier.verify( javaFile( variables.fixtures & "data.txt" ), fileReadBinary( variables.fixtures & "data.txt.sha1.asc" ), keys );
					}, "unsupported hash algorithm" );
				});

			});

			describe( "the policy", function() {

				it( title="'verify' rejects, 'warn' and 'off' accept an invalid or missing signature", body=function( currentSpec ) {
					var f = javaFile( variables.fixtures & "data.txt" );
					var sig = fileReadBinary( variables.fixtures & "data.txt.asc" ); // valid, but of another key
					var VERIFY = 2;
					var WARN = 1;
					var OFF = 0;
					expectRejected( function() {
						variables.ArtifactSignature.verify( f, sig, javaCast( "null", "" ), "test", VERIFY );
					}, "could not be verified" );
					expectRejected( function() {
						variables.ArtifactSignature.verify( f, javaCast( "null", "" ), "there is no signature", "test", VERIFY );
					}, "there is no signature" );
					variables.ArtifactSignature.verify( f, sig, javaCast( "null", "" ), "test", WARN );
					variables.ArtifactSignature.verify( f, javaCast( "null", "" ), "there is no signature", "test", WARN );
					variables.ArtifactSignature.verify( f, sig, javaCast( "null", "" ), "test", OFF );
				});

				it( title="only org.lucee artifacts are verified, releases need a signature", body=function( currentSpec ) {
					expect( variables.ArtifactSignature.requirement( "org.lucee", "7.0.1.1" ) ).toBe( 2 );
					expect( variables.ArtifactSignature.requirement( "org.lucee", "7.0.0.395" ) ).toBe( 2 );
					expect( variables.ArtifactSignature.requirement( "org.lucee", "9.0.0.279-ALPHA" ) ).toBe( 1 );
					expect( variables.ArtifactSignature.requirement( "org.lucee", "2.2.0.2-RC" ) ).toBe( 1 );
					expect( variables.ArtifactSignature.requirement( "org.lucee", "7.1.0.1-SNAPSHOT" ) ).toBe( 0 );
					expect( variables.ArtifactSignature.requirement( "org.example", "1.0.0" ) ).toBe( 0 );
				});

				it( title="the default is 'verify'", body=function( currentSpec ) {
					expect( variables.ConfigServerImpl.instance.getMavenSignatureVerification() ).toBe( 2 );
				});

			});

			describe( "the Maven download", function() {

				it( title="loads the signature from Maven Central", body=function( currentSpec ) {
					var central = createObject( "java", "java.net.URL" ).init( "https://repo1.maven.org/maven2/org/lucee/lucee/5.3.7.47/lucee-5.3.7.47.pom" );
					variables.ArtifactSignature.verify( javaFile( pomFixture() ), central, "org.lucee:lucee:5.3.7.47:pom", 2 );
					var tampered = variables.tmp & "central-tampered.pom";
					fileWrite( tampered, fileRead( pomFixture() ) & " " );
					expectRejected( function() {
						variables.ArtifactSignature.verify( javaFile( tampered ), central, "org.lucee:lucee:5.3.7.47:pom", 2 );
					}, "could not be verified" );
				});

				it( title="installs a correctly signed org.lucee artifact", body=function( currentSpec ) {
					var r = download( "ok", "org.lucee" );
					expect( r.error ).toBe( "" );
					expect( r.installed ).toBeTrue();
				});

				it( title="rejects a modified org.lucee artifact and does not install it", body=function( currentSpec ) {
					var r = download( "tampered", "org.lucee", { tamper: true } );
					expect( r.error ).toInclude( "signature" );
					expect( r.error ).toInclude( "could not be verified" );
					expect( r.installed ).toBeFalse();
				});

				it( title="rejects an org.lucee artifact without a signature", body=function( currentSpec ) {
					var r = download( "nosig", "org.lucee", { withSignature: false } );
					expect( r.error ).toInclude( "there is no signature" );
					expect( r.installed ).toBeFalse();
				});

				it( title="accepts a pre-release without a signature", body=function( currentSpec ) {
					var r = download( "pre-nosig", "org.lucee", { version: "5.3.7.47-RC", withSignature: false } );
					expect( r.error ).toBe( "" );
					expect( r.installed ).toBeTrue();
				});

				it( title="rejects a pre-release with an invalid signature", body=function( currentSpec ) {
					var r = download( "pre-tampered", "org.lucee", { version: "5.3.7.47-RC", tamper: true } );
					expect( r.error ).toInclude( "could not be verified" );
					expect( r.installed ).toBeFalse();
				});

				it( title="'strict' rejects a pre-release without a signature", body=function( currentSpec ) {
					setPolicy( "strict" );
					try {
						var r = download( "pre-strict", "org.lucee", { version: "5.3.7.47-RC", withSignature: false } );
					}
					finally {
						setPolicy( "" );
					}
					expect( r.error ).toInclude( "there is no signature" );
					expect( r.installed ).toBeFalse();
				});

				it( title="does not verify other groups", body=function( currentSpec ) {
					var r = download( "other", "org.example", { tamper: true, withSignature: false } );
					expect( r.error ).toBe( "" );
					expect( r.installed ).toBeTrue();
				});

				it( title="'warn' installs an artifact with an invalid signature", body=function( currentSpec ) {
					setPolicy( "warn" );
					try {
						var r = download( "warn", "org.lucee", { tamper: true } );
					}
					finally {
						setPolicy( "" );
					}
					expect( r.error ).toBe( "" );
					expect( r.installed ).toBeTrue();
				});

				it( title="'off' installs an artifact with an invalid signature", body=function( currentSpec ) {
					setPolicy( "off" );
					try {
						var r = download( "off", "org.lucee", { tamper: true, withSignature: false } );
					}
					finally {
						setPolicy( "" );
					}
					expect( r.error ).toBe( "" );
					expect( r.installed ).toBeTrue();
				});

			});

		});
	}

	private function javaFile( path ) {
		return createObject( "java", "java.io.File" ).init( path );
	}

	private function pomFixture() {
		return variables.fixtures & "repo/org/lucee/lucee/5.3.7.47/lucee-5.3.7.47.pom";
	}

	private function expectRejected( fn, string message ) {
		var msg = "";
		try {
			arguments.fn();
		}
		catch ( any e ) {
			msg = e.message;
		}
		expect( msg ).notToBe( "", "the signature must be rejected" );
		if ( len( arguments.message ) ) expect( msg ).toInclude( arguments.message );
	}

	private function setPolicy( value ) {
		if ( len( value ) ) createObject( "java", "java.lang.System" ).setProperty( variables.sysProp, value );
		else createObject( "java", "java.lang.System" ).clearProperty( variables.sysProp );
		variables.ConfigServerImpl.instance.resetMavenSignatureVerification();
	}

	private function startServer() {
		var addr = createObject( "java", "java.net.InetSocketAddress" ).init( createObject( "java", "java.net.InetAddress" ).getLoopbackAddress(), 0 );
		var level = createObject( "java", "com.sun.net.httpserver.SimpleFileServer$OutputLevel" ).NONE;
		var root = javaFile( variables.tmp & "served" );
		root.mkdirs();
		var srv = createObject( "java", "com.sun.net.httpserver.SimpleFileServer" ).createFileServer( addr, root.toPath(), level );
		srv.start();
		return srv;
	}

	/**
	 * serves the fixture from a local Maven repository and downloads it with the Maven resolver
	 */
	private struct function download( required string name, required string group, struct options={} ) {
		var opt = { tamper: false, withSignature: true, version: "5.3.7.47" };
		structAppend( opt, arguments.options );
		var version = opt.version;
		var rel = replace( arguments.group, ".", "/", "all" ) & "/lucee/" & version & "/";
		var served = variables.tmp & "served/" & arguments.name & "/" & rel;
		directoryCreate( served, true );
		var pomName = "lucee-" & version & ".pom";
		var content = fileRead( pomFixture() );
		fileWrite( served & pomName, opt.tamper ? content & " " : content );
		if ( opt.withSignature ) fileCopy( pomFixture() & ".asc", served & pomName & ".asc" );

		var localDir = variables.tmp & "local/" & arguments.name & "/";
		directoryCreate( localDir, true );
		var res = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" ).toResourceNotExisting( getPageContext(), localDir );
		var pom = createObject( "java", "lucee.runtime.mvn.POM" ).getInstance( res, arguments.group, "lucee", version, javaCast( "null", "" ) );
		var repoUrl = "http://127.0.0.1:" & variables.server.getAddress().getPort() & "/" & arguments.name & "/";
		var repo = createObject( "java", "lucee.runtime.config.maven.MavenUpdateProvider$Repository" ).init( "LDEV-1554", repoUrl, 0, 0, 0 );
		var repos = createObject( "java", "java.util.ArrayList" ).init();
		repos.add( repo );

		var error = "";
		try {
			createObject( "java", "lucee.runtime.mvn.MavenUtil" ).download( pom, repos, "pom", javaCast( "null", "" ) );
		}
		catch ( any e ) {
			error = e.message;
		}
		return { error: error, installed: fileExists( localDir & rel & pomName ) };
	}

}
