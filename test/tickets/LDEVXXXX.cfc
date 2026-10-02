component extends="org.lucee.cfml.test.LuceeTestCase" labels="extension,maven" skip=false {

	function run( testResults, testBox ) {
		describe( "LDEV-XXXX ExtensionProvider.detail() keeps lite and lite.asc separate for SNAPSHOT classifiers", function() {

			it( title="mail-extension SNAPSHOT detail has lite (.lex) and lite.asc, not overwritten by the GPG signature", body=function( currentSpec ) {
				// mail-extension 1.1.0.9-SNAPSHOT publishes both classifier=lite extension=lex and classifier=lite extension=lex.asc.
				// Before the fix, RepoReader.read() remapped both to key "lite", so the later HashMap entry overwrote the real .lex URL with the .asc URL.
				var detail = LuceeExtension( "org.lucee", "mail-extension", "1.1.0.9-SNAPSHOT" );

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

}
