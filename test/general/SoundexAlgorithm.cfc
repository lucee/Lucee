component extends="org.lucee.cfml.test.LuceeTestCase" labels="soundex" {

	function run( testResults, testBox ) {
		describe( "soundex()", function() {

			it( title="known values", body=function( currentSpec ) {
				expect( soundex( "Robert" ) ).toBe( "R163" );
				expect( soundex( "Rupert" ) ).toBe( "R163" );
				expect( soundex( "Rubin" ) ).toBe( "R150" );
				expect( soundex( "Ashcraft" ) ).toBe( "A261" );
				expect( soundex( "Tymczak" ) ).toBe( "T522" );
				expect( soundex( "Pfister" ) ).toBe( "P236" );
				expect( soundex( "Honeyman" ) ).toBe( "H555" );
				expect( soundex( "lucee" ) ).toBe( "L200" );
				expect( soundex( "coldfusion" ) ).toBe( "C431" );
			});

			it( title="case, spaces and non letters", body=function( currentSpec ) {
				expect( soundex( "robert" ) ).toBe( "R163" );
				expect( soundex( "ROBERT" ) ).toBe( "R163" );
				expect( soundex( "  r o-b'e r t 123" ) ).toBe( "R163" );
				expect( soundex( "A" ) ).toBe( "A000" );
				expect( soundex( "" ) ).toBe( "" );
				expect( soundex( "123" ) ).toBe( "" );
				expect( soundex( " - " ) ).toBe( "" );
			});

			it( title="the whole string is uppercased, so sharp s and ligatures map like commons-codec", body=function( currentSpec ) {
				// chr( 223 ) is the german sharp s, uppercased it becomes "SS"
				expect( soundex( "Stra" & chr( 223 ) & "e" ) ).toBe( "S362" );
				expect( soundex( chr( 223 ) ) ).toBe( "S000" );
				// chr( 64257 ) is the ligature "fi", uppercased it becomes "FI"
				expect( soundex( chr( 64257 ) & "sher" ) ).toBe( "F260" );
			});

			it( title="a character that is not mapped fails", body=function( currentSpec ) {
				var msg = "";
				try {
					soundex( "M" & chr( 252 ) & "ller" );
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toBe( "The character is not mapped: #chr( 220 )# (index=#asc( chr( 220 ) ) - 65#)" );
			});

			it( title="the same result as before for 4000 generated words", body=function( currentSpec ) {
				var letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
				var seed = 12345;
				var all = [];
				for ( var i = 1; i <= 4000; i++ ) {
					var word = "";
					seed = ( seed * 1103515245 + 12345 ) mod 2147483648;
					var wl = 1 + ( seed mod 12 );
					for ( var j = 1; j <= wl; j++ ) {
						seed = ( seed * 1103515245 + 12345 ) mod 2147483648;
						word &= mid( letters, 1 + ( int( seed / 65536 ) mod 52 ), 1 );
					}
					arrayAppend( all, word & ":" & soundex( word ) );
				}
				expect( hash( arrayToList( all, "," ), "MD5" ) ).toBe( "8A77C1406406855D1C88122B3E45567B" );
			});
		});

		describe( "metaphone()", function() {
			it( title="is an alias of soundex()", body=function( currentSpec ) {
				expect( metaphone( "lucee" ) ).toBe( soundex( "lucee" ) );
				expect( metaphone( "coldfusion" ) ).toBe( soundex( "coldfusion" ) );
			});
		});
	}
}
