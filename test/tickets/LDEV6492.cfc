component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension,maven" skip=false {

	// a SNAPSHOT that is published with GPG signatures (lex.asc, pom.asc and lite + lex.asc).
	// mail-extension 1.1.0.9-SNAPSHOT (used before) no longer lists any .asc entry in its snapshot metadata.
	// Central only keeps snapshots for 90 days, so the test is skipped when the signed entries are gone (pick a newer signed SNAPSHOT then).
	variables.snapshotRepo = "https://central.sonatype.com/repository/maven-snapshots/";
	variables.group = "org.lucee";
	variables.artifact = "redis-extension";
	variables.version = "4.3.0.1-SNAPSHOT";

	function run( testResults, testBox ) {
		describe( "LDEV-6492 ExtensionProvider.detail() keeps lite and lite.asc separate for SNAPSHOT classifiers", function() {

			it( title="#variables.artifact# SNAPSHOT detail has lite (.lex) and lite.asc, not overwritten by the GPG signature", skip=!hasSignedLiteSnapshot(), body=function( currentSpec ) {
				// the snapshot publishes both classifier=lite extension=lex and classifier=lite extension=lex.asc.
				// Before the fix, RepoReader.read() remapped both to key "lite", so the later HashMap entry overwrote the real .lex URL with the .asc URL.
				var detail = LuceeExtension( variables.group, variables.artifact, variables.version );

				expect( isStruct( detail ) ).toBeTrue( "detail() should return a struct" );
				expect( structKeyExists( detail, "lex" ) ).toBeTrue( "expected key lex; keys=#structKeyList( detail )#" );
				expect( structKeyExists( detail, "lex.asc" ) ).toBeTrue( "expected key lex.asc; keys=#structKeyList( detail )#" );
				expect( structKeyExists( detail, "pom" ) ).toBeTrue( "expected key pom; keys=#structKeyList( detail )#" );
				expect( structKeyExists( detail, "pom.asc" ) ).toBeTrue( "expected key pom.asc; keys=#structKeyList( detail )#" );

				expect( structKeyExists( detail, "lite" ) ).toBeTrue( "expected key lite; keys=#structKeyList( detail )#" );
				expect( structKeyExists( detail, "lite.asc" ) ).toBeTrue( "expected key lite.asc (must not be missing / folded into lite); keys=#structKeyList( detail )#" );

				var liteUrl = toString( detail.lite );
				var liteAscUrl = toString( detail[ "lite.asc" ] );

				expect( liteUrl ).toInclude( "-lite.lex", "lite should be the .lex artifact URL, got: #liteUrl#" );
				expect( right( liteUrl, 4 ) ).notToBe( ".asc", "lite must not end in .asc (GPG signature); got: #liteUrl#" );
				expect( liteAscUrl ).toInclude( "-lite.lex.asc", "lite.asc should be the GPG signature URL, got: #liteAscUrl#" );
			} );

		} );
	}

	// true when the remote snapshot metadata still lists lex.asc, pom.asc and lite + lex.asc
	private boolean function hasSignedLiteSnapshot() {
		try {
			var metaUrl = variables.snapshotRepo & replace( variables.group, ".", "/", "all" ) & "/" & variables.artifact & "/" & variables.version & "/maven-metadata.xml";
			http url=metaUrl method="get" timeout=10 result="local.res";
			if ( !find( "200", res.statuscode ) ) return false;
			var entries = xmlSearch( xmlParse( res.fileContent ), "//*[local-name()='snapshotVersion']" );
			var found = {};
			for ( var e in entries ) {
				var classifier = structKeyExists( e, "classifier" ) ? trim( e.classifier.xmlText ) : "";
				found[ classifier & ":" & trim( e.extension.xmlText ) ] = true;
			}
			return structKeyExists( found, ":lex.asc" ) && structKeyExists( found, ":pom.asc" ) && structKeyExists( found, "lite:lex.asc" );
		}
		catch ( any e ) {
			return false;
		}
	}

}
