component extends="org.lucee.cfml.test.LuceeTestCase" labels="urlencoder" {

	function beforeAll() {
		variables.Enc = createObject( "java", "lucee.commons.lang.URLEncoder" );
	}

	function run( testResults, testBox ) {
		describe( "lucee.commons.lang.URLEncoder", function() {

			it( title="alphanumerics stay, space becomes %20", body=function( currentSpec ) {
				expect( variables.Enc.encode( "abcXYZ019 x" ) ).toBe( "abcXYZ019%20x" );
			});

			it( title="the characters . - * _ are escaped", body=function( currentSpec ) {
				expect( variables.Enc.encode( ".-*_" ) ).toBe( "%2E%2D%2A%5F" );
			});

			it( title="reserved characters are escaped with uppercase hex", body=function( currentSpec ) {
				expect( variables.Enc.encode( "a@b/c?d=e&f%g~h" ) ).toBe( "a%40b%2Fc%3Fd%3De%26f%25g%7Eh" );
			});

			it( title="non ascii characters are escaped per byte in UTF-8 by default", body=function( currentSpec ) {
				expect( variables.Enc.encode( "#chr( 252 )#" ) ).toBe( "%C3%BC" );
				expect( variables.Enc.encode( "#chr( 8364 )#" ) ).toBe( "%E2%82%AC" );
			});

			it( title="umlauts and accents", body=function( currentSpec ) {
				var str = chr( 246 ) & chr( 228 ) & chr( 252 ) & chr( 233 ) & chr( 224 ) & chr( 232 ); // öäüéàè
				expect( variables.Enc.encode( str ) ).toBe( "%C3%B6%C3%A4%C3%BC%C3%A9%C3%A0%C3%A8" );
				expect( variables.Enc.encode( str, "UTF-8" ) ).toBe( "%C3%B6%C3%A4%C3%BC%C3%A9%C3%A0%C3%A8" );
				expect( variables.Enc.encode( str, "ISO-8859-1" ) ).toBe( "%F6%E4%FC%E9%E0%E8" );
				expect( variables.Enc.encode( "M" & chr( 252 ) & "ller " & chr( 233 ) & "t" & chr( 233 ) ) ).toBe( "M%C3%BCller%20%C3%A9t%C3%A9" );
			});

			it( title="the encoding can be given as a name", body=function( currentSpec ) {
				expect( variables.Enc.encode( "#chr( 252 )# x", "ISO-8859-1" ) ).toBe( "%FC%20x" );
				expect( variables.Enc.encode( "#chr( 252 )#", "UTF-16BE" ) ).toBe( "%00%FC" );
			});

			it( title="empty string", body=function( currentSpec ) {
				expect( variables.Enc.encode( "" ) ).toBe( "" );
			});

			it( title="every ascii character", body=function( currentSpec ) {
				var all = "";
				for ( var i = 1; i <= 127; i++ ) all &= chr( i );
				expect( hash( variables.Enc.encode( all ) ) ).toBe( "9A1CE1D3427F738D5AF25415D29B4B2A" );
			});
		});
	}
}
