component extends="org.lucee.cfml.test.LuceeTestCase" labels="urldecoder" {

	function beforeAll() {
		variables.Dec = createObject( "java", "lucee.commons.net.URLDecoder" );
		variables.uml = chr( 246 ) & chr( 228 ) & chr( 252 ) & chr( 233 ) & chr( 224 ) & chr( 232 ); // öäüéàè
	}

	function run( testResults, testBox ) {
		describe( "lucee.commons.net.URLDecoder.decode( string, encoding, force )", function() {

			it( title="plain text, plus and percent escapes", body=function( currentSpec ) {
				expect( variables.Dec.decode( "abc", "UTF-8", true ) ).toBe( "abc" );
				expect( variables.Dec.decode( "a+b%20c", "UTF-8", true ) ).toBe( "a b c" );
				expect( variables.Dec.decode( "%41%42%43", "UTF-8", true ) ).toBe( "ABC" );
				expect( variables.Dec.decode( "", "UTF-8", true ) ).toBe( "" );
			});

			it( title="hex digits can be upper or lower case", body=function( currentSpec ) {
				expect( variables.Dec.decode( "%c3%bc", "UTF-8", true ) ).toBe( chr( 252 ) );
				expect( variables.Dec.decode( "%C3%BC", "UTF-8", true ) ).toBe( chr( 252 ) );
				expect( variables.Dec.decode( "%c3%Bc", "UTF-8", true ) ).toBe( chr( 252 ) );
			});

			it( title="umlauts and accents encoded as UTF-8", body=function( currentSpec ) {
				expect( variables.Dec.decode( "%C3%B6%C3%A4%C3%BC%C3%A9%C3%A0%C3%A8", "UTF-8", true ) ).toBe( variables.uml );
				expect( variables.Dec.decode( "M%C3%BCller+%C3%A9t%C3%A9", "UTF-8", true ) ).toBe( "M" & chr( 252 ) & "ller " & chr( 233 ) & "t" & chr( 233 ) );
			});

			it( title="umlauts and accents encoded as ISO-8859-1", body=function( currentSpec ) {
				expect( variables.Dec.decode( "%F6%E4%FC%E9%E0%E8", "ISO-8859-1", true ) ).toBe( variables.uml );
			});

			it( title="umlauts and accents that are not encoded at all", body=function( currentSpec ) {
				expect( variables.Dec.decode( variables.uml, "UTF-8", true ) ).toBe( variables.uml );
				expect( variables.Dec.decode( variables.uml, "ISO-8859-1", true ) ).toBe( variables.uml );
				expect( variables.Dec.decode( "a+" & variables.uml & "%20b", "UTF-8", true ) ).toBe( "a " & variables.uml & " b" );
				expect( variables.Dec.decode( "a+" & variables.uml & "%20b", "ISO-8859-1", true ) ).toBe( "a " & variables.uml & " b" );
			});

			it( title="characters outside of ISO-8859-1", body=function( currentSpec ) {
				expect( variables.Dec.decode( "%E2%82%AC", "UTF-8", true ) ).toBe( chr( 8364 ) );
				expect( variables.Dec.decode( chr( 8364 ), "UTF-8", true ) ).toBe( chr( 8364 ) );
				expect( variables.Dec.decode( "x" & chr( 8364 ) & "%20y", "UTF-8", true ) ).toBe( "x" & chr( 8364 ) & " y" );
			});

			it( title="other encodings", body=function( currentSpec ) {
				expect( variables.Dec.decode( "%00%FC", "UTF-16BE", true ) ).toBe( chr( 252 ) );
				expect( variables.Dec.decode( "%FC", "windows-1252", true ) ).toBe( chr( 252 ) );
			});

			it( title="reserved characters", body=function( currentSpec ) {
				expect( variables.Dec.decode( "a%40b%2Fc%3Fd%3De%26f%25g%7Eh%2B", "UTF-8", true ) ).toBe( "a@b/c?d=e&f%g~h+" );
				expect( variables.Dec.decode( "%2E%2D%2A%5F", "UTF-8", true ) ).toBe( ".-*_" );
			});

			it( title="an invalid UTF-8 byte sequence is replaced", body=function( currentSpec ) {
				expect( len( variables.Dec.decode( "%C3", "UTF-8", true ) ) ).toBe( 1 );
				expect( asc( variables.Dec.decode( "%C3", "UTF-8", true ) ) ).toBe( 65533 );
			});

			it( title="without force, a string that needs no decoding is returned as it is", body=function( currentSpec ) {
				expect( variables.Dec.decode( "abc", "UTF-8", false ) ).toBe( "abc" );
				expect( variables.Dec.decode( "a%20b", "UTF-8", false ) ).toBe( "a b" );
				expect( variables.Dec.decode( "a+b", "UTF-8", false ) ).toBe( "a b" );
			});

			it( title="invalid escapes fail with 'Invalid URL encoding'", body=function( currentSpec ) {
				var inputs = [ "%zz", "%4", "abc%", "%4z", "%%41", "a%2" ];
				for ( var input in inputs ) {
					var msg = "";
					try {
						variables.Dec.decode( input, "UTF-8", true );
					}
					catch ( any e ) {
						msg = e.message;
					}
					expect( msg ).toBe( "Invalid URL encoding in string [#input#]", input );
				}
			});

			it( title="an unknown encoding fails", body=function( currentSpec ) {
				var type = "";
				try {
					variables.Dec.decode( "abc", "UTF-99", true );
				}
				catch ( any e ) {
					type = e.message;
				}
				expect( type ).toBe( "UTF-99" );
			});
		});

		describe( "lucee.commons.net.URLDecoder.decode( string, force )", function() {
			it( title="uses the default charset", body=function( currentSpec ) {
				expect( variables.Dec.decode( "a+b%20c", true ) ).toBe( "a b c" );
			});
		});

		describe( "lucee.commons.net.URLDecoder.decodeLax", function() {
			it( title="invalid escapes are kept", body=function( currentSpec ) {
				expect( variables.Dec.decodeLax( "a%zzb+c", "UTF-8", true ) ).toBe( "a%zzb c" );
				expect( variables.Dec.decodeLax( "%C3%BC", "UTF-8", true ) ).toBe( chr( 252 ) );
			});
		});
	}
}
