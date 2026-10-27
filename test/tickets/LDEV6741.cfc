component extends="org.lucee.cfml.test.LuceeTestCase" labels="ssl" {

	function beforeAll() {
		variables.dir = server._getTempDir( "LDEV6741" );
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
		directoryCreate( variables.dir, true );
		variables.dir &= "/";
		variables.oldStore = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV6741/cacerts-ldev4497";
		variables.bts = createObject( "java", "lucee.runtime.config.BundledTrustStore" );
		// SHA-256 fingerprints of the roots affected by CVE-2026-91403
		variables.affectedFingerprints = [
			"40F6AF0346A99AA1CD1D555A4E9CCE62C7F9634603EE406615833DC8C8D00367", // emSign Root CA - G1
			"86A1ECBA089C4A8D3BBE2734C612BA341D813E043CF9E8A862CD5C57A36BBE6B", // emSign ECC Root CA - G3
			"D7A7A0FB5D7E2731D771E9484EBCDEF71D5F0C3E0A2948782BC83EE0EA699EF4" // AAA Certificate Services
		];
		variables.affectedSubjects = [
			"CN=emSign Root CA - G1,",
			"CN=emSign Root CA - G2,",
			"CN=emSign ECC Root CA - G3,",
			"CN=AAA Certificate Services,"
		];
	}

	function afterAll() {
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6741: bundled trust store without the roots affected by CVE-2026-91403", function() {

			it( "the bundled trust store does not contain any of the affected roots", function() {
				var ks = loadKeyStore( bts.getBundled() );
				expect( ks.size() ).toBeGT( 100 );
				var aliases = ks.aliases();
				while ( aliases.hasMoreElements() ) {
					var alias = aliases.nextElement();
					var cert = ks.getCertificate( alias );
					var fingerprint = hash( cert.getEncoded(), "SHA-256" );
					expect( arrayFindNoCase( variables.affectedFingerprints, fingerprint ) ).toBe( 0, "found affected root [#alias#]" );
					var subject = cert.getSubjectX500Principal().getName() & ",";
					loop array=variables.affectedSubjects item="local.affected" {
						expect( find( affected, subject ) ).toBe( 0, "found affected root [#alias#]" );
					}
				}
			});

			it( "the trust store bundled by LDEV-4497 is known as a previous bundled version", function() {
				var file = toResource( copyOldStore( "known" ) );
				expect( bts.isPreviousBundledVersion( file ) ).toBeTrue();
				expect( bts.getPreviousHashes().contains( bts.getBundledHash() ) ).toBeFalse();
			});

			it( "an unmodified trust store from a previous release gets updated", function() {
				var path = copyOldStore( "unmodified" );
				expect( bts.updateIfUnmodified( toResource( path ) ) ).toBeTrue();
				expect( hash( fileReadBinary( path ), "SHA-256" ) ).toBe( bts.getBundledHash() );
				expect( fileExists( path & ".tmp" ) ).toBeFalse();

				var ks = loadKeyStore( fileReadBinary( path ) );
				loop array=getFingerprints( ks ) item="local.fingerprint" {
					expect( arrayFindNoCase( variables.affectedFingerprints, fingerprint ) ).toBe( 0 );
				}
			});

			it( "a trust store modified by the user is never touched", function() {
				var path = copyOldStore( "modified" );
				var ks = loadKeyStore( fileReadBinary( path ) );
				var aliases = ks.aliases();
				ks.setCertificateEntry( "ldev6741-custom", ks.getCertificate( aliases.nextElement() ) );
				var os = createObject( "java", "java.io.FileOutputStream" ).init( path );
				try {
					ks.store( os, javaCast( "string", "changeit" ).toCharArray() );
				}
				finally {
					os.close();
				}
				var before = hash( fileReadBinary( path ), "SHA-256" );

				expect( bts.updateIfUnmodified( toResource( path ) ) ).toBeFalse();
				expect( hash( fileReadBinary( path ), "SHA-256" ) ).toBe( before );
				expect( loadKeyStore( fileReadBinary( path ) ).containsAlias( "ldev6741-custom" ) ).toBeTrue();
			});

			it( "the current bundled trust store is not rewritten", function() {
				var path = variables.dir & "current/cacerts";
				directoryCreate( getDirectoryFromPath( path ), true, true );
				fileWrite( path, bts.getBundled() );
				expect( bts.updateIfUnmodified( toResource( path ) ) ).toBeFalse();
				expect( hash( fileReadBinary( path ), "SHA-256" ) ).toBe( bts.getBundledHash() );
			});

			it( "deploy creates the trust store when it does not exist yet", function() {
				var secDir = variables.dir & "deploy/security";
				var res = bts.deploy( toResource( secDir ) );
				expect( res.getName() ).toBe( "cacerts" );
				expect( hash( fileReadBinary( secDir & "/cacerts" ), "SHA-256" ) ).toBe( bts.getBundledHash() );
			});

			it( "deploy updates an unmodified trust store from a previous release", function() {
				var secDir = variables.dir & "deploy-old/security";
				directoryCreate( secDir, true, true );
				fileCopy( variables.oldStore, secDir & "/cacerts" );
				bts.deploy( toResource( secDir ) );
				expect( hash( fileReadBinary( secDir & "/cacerts" ), "SHA-256" ) ).toBe( bts.getBundledHash() );
			});

		});
	}

	private function copyOldStore( required string name ) {
		var path = variables.dir & arguments.name & "/cacerts";
		directoryCreate( getDirectoryFromPath( path ), true, true );
		fileCopy( variables.oldStore, path );
		expect( hash( fileReadBinary( path ), "SHA-256" ) ).toBe( "6FE983076DEF1AD7C40DA7093D7D52D897872872D4E9D2089826AF7AE5F05822" );
		return path;
	}

	private function toResource( required string path ) {
		return getPageContext().getConfig().getResource( arguments.path );
	}

	private function loadKeyStore( required binary data ) {
		var ks = createObject( "java", "java.security.KeyStore" ).getInstance( "JKS" );
		ks.load( createObject( "java", "java.io.ByteArrayInputStream" ).init( arguments.data ), javaCast( "string", "changeit" ).toCharArray() );
		return ks;
	}

	private function getFingerprints( required ks ) {
		var result = [];
		var aliases = arguments.ks.aliases();
		while ( aliases.hasMoreElements() ) {
			arrayAppend( result, hash( arguments.ks.getCertificate( aliases.nextElement() ).getEncoded(), "SHA-256" ) );
		}
		return result;
	}
}
