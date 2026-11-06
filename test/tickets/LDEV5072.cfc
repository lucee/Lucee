component extends="org.lucee.cfml.test.LuceeTestCase" labels="encoding" {

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-5072 - base64url encoding for binaryEncode() and binaryDecode()", function() {

			it( title="binaryEncode() with base64url uses the URL safe alphabet", body=function( currentSpec ) {
				var bin = binaryDecode( "FBFFFE", "hex" );
				expect( binaryEncode( bin, "base64" ) ).toBe( "+//+" );
				expect( binaryEncode( bin, "base64url" ) ).toBe( "-__-" );
			});

			it( title="binaryEncode() with base64url does not add padding", body=function( currentSpec ) {
				// the test vectors from RFC 4648 section 10
				expect( binaryEncode( charsetDecode( "", "utf-8" ), "base64url" ) ).toBe( "" );
				expect( binaryEncode( charsetDecode( "f", "utf-8" ), "base64url" ) ).toBe( "Zg" );
				expect( binaryEncode( charsetDecode( "fo", "utf-8" ), "base64url" ) ).toBe( "Zm8" );
				expect( binaryEncode( charsetDecode( "foo", "utf-8" ), "base64url" ) ).toBe( "Zm9v" );
				expect( binaryEncode( charsetDecode( "foob", "utf-8" ), "base64url" ) ).toBe( "Zm9vYg" );
				expect( binaryEncode( charsetDecode( "fooba", "utf-8" ), "base64url" ) ).toBe( "Zm9vYmE" );
				expect( binaryEncode( charsetDecode( "foobar", "utf-8" ), "base64url" ) ).toBe( "Zm9vYmFy" );

				expect( binaryEncode( binaryDecode( "FB", "hex" ), "base64url" ) ).toBe( "-w" );
				expect( binaryEncode( binaryDecode( "FF", "hex" ), "base64url" ) ).toBe( "_w" );
				expect( binaryEncode( binaryDecode( "FBFF", "hex" ), "base64url" ) ).toBe( "-_8" );
			});

			it( title="binaryEncode() with base64url never contains +, / or =", body=function( currentSpec ) {
				var hex = "";
				loop from=0 to=255 index="local.i" {
					hex &= right( "0" & formatBaseN( i, 16 ), 2 );
				}
				loop from=1 to=5 index="local.len" {
					var encoded = binaryEncode( binaryDecode( left( hex, len * 2 ), "hex" ), "base64url" );
					expect( encoded ).notToMatch( "[+/=]" );
				}
				var encoded = binaryEncode( binaryDecode( hex, "hex" ), "base64url" );
				expect( encoded ).notToMatch( "[+/=]" );
				expect( encoded ).toMatch( "^[A-Za-z0-9_-]+$" );
				expect( binaryEncode( binaryDecode( hex, "hex" ), "hex" ) ).toBe( ucase( hex ) );
			});

			it( title="the encoding name is case insensitive", body=function( currentSpec ) {
				var bin = binaryDecode( "FBFFFE", "hex" );
				expect( binaryEncode( bin, "Base64URL" ) ).toBe( "-__-" );
				expect( binaryEncode( bin, "BASE64URL" ) ).toBe( "-__-" );
				expect( binaryEncode( binaryDecode( "-__-", "Base64URL", true ), "hex" ) ).toBe( "FBFFFE" );
			});

			it( title="binaryDecode() with base64url accepts input with and without padding", body=function( currentSpec ) {
				loop array=[ true, false ] item="local.precise" {
					expect( binaryEncode( binaryDecode( "Zg", "base64url", precise ), "hex" ) ).toBe( "66" );
					expect( binaryEncode( binaryDecode( "Zg==", "base64url", precise ), "hex" ) ).toBe( "66" );
					expect( binaryEncode( binaryDecode( "Zm8", "base64url", precise ), "hex" ) ).toBe( "666F" );
					expect( binaryEncode( binaryDecode( "Zm8=", "base64url", precise ), "hex" ) ).toBe( "666F" );
					expect( binaryEncode( binaryDecode( "Zm9v", "base64url", precise ), "hex" ) ).toBe( "666F6F" );
					expect( binaryEncode( binaryDecode( "Zm9vYg", "base64url", precise ), "hex" ) ).toBe( "666F6F62" );
					expect( binaryEncode( binaryDecode( "Zm9vYg==", "base64url", precise ), "hex" ) ).toBe( "666F6F62" );
					expect( binaryEncode( binaryDecode( "-w", "base64url", precise ), "hex" ) ).toBe( "FB" );
					expect( binaryEncode( binaryDecode( "-w==", "base64url", precise ), "hex" ) ).toBe( "FB" );
					expect( binaryEncode( binaryDecode( "_w", "base64url", precise ), "hex" ) ).toBe( "FF" );
					expect( binaryEncode( binaryDecode( "-_8", "base64url", precise ), "hex" ) ).toBe( "FBFF" );
					expect( binaryEncode( binaryDecode( "-_8=", "base64url", precise ), "hex" ) ).toBe( "FBFF" );
					expect( binaryEncode( binaryDecode( "-__-", "base64url", precise ), "hex" ) ).toBe( "FBFFFE" );
				}
			});

			it( title="binaryDecode() with base64url and an empty string returns empty binary", body=function( currentSpec ) {
				var bin = binaryDecode( "", "base64url", true );
				expect( isBinary( bin ) ).toBeTrue();
				expect( arrayLen( bin ) ).toBe( 0 );
			});

			it( title="binaryDecode() with precise=true rejects input that is not base64url", body=function( currentSpec ) {
				// characters of the standard alphabet
				expect( function() {
					binaryDecode( "+//+", "base64url", true );
				}).toThrow( regex="invalid character" );
				expect( function() {
					binaryDecode( "+w==", "base64url", true );
				}).toThrow( regex="invalid character" );
				// characters outside of both alphabets
				expect( function() {
					binaryDecode( "Zm9v!", "base64url", true );
				}).toThrow( regex="invalid character" );
				expect( function() {
					binaryDecode( "Zm 9v", "base64url", true );
				}).toThrow( regex="invalid character" );
				// a single leftover character can not be decoded
				expect( function() {
					binaryDecode( "Z", "base64url", true );
				}).toThrow();
				expect( function() {
					binaryDecode( "Zm9vY", "base64url", true );
				}).toThrow();
				// wrong padding
				expect( function() {
					binaryDecode( "Zg=", "base64url", true );
				}).toThrow();
				expect( function() {
					binaryDecode( "Zg===", "base64url", true );
				}).toThrow();
				expect( function() {
					binaryDecode( "=Zg", "base64url", true );
				}).toThrow();
			});

			it( title="binaryDecode() with precise=false accepts both alphabets", body=function( currentSpec ) {
				expect( binaryEncode( binaryDecode( "-__-", "base64url", false ), "hex" ) ).toBe( "FBFFFE" );
				expect( binaryEncode( binaryDecode( "+//+", "base64url", false ), "hex" ) ).toBe( "FBFFFE" );
			});

			it( title="base64 and base64url round trip random data", body=function( currentSpec ) {
				loop from=0 to=64 index="local.len" {
					var hex = "";
					loop from=1 to=len index="local.j" {
						hex &= right( "0" & formatBaseN( ( len * 37 + j * 91 + j * j ) mod 256, 16 ), 2 );
					}
					var bin = binaryDecode( hex, "hex" );
					var encoded = binaryEncode( bin, "base64url" );
					expect( encoded ).notToMatch( "[+/=]" );
					expect( binaryEncode( binaryDecode( encoded, "base64url", true ), "hex" ) ).toBe( ucase( hex ) );

					// same data as the standard base64, only the alphabet and the padding differ
					var std = binaryEncode( bin, "base64" );
					expect( encoded ).toBe( replace( replace( replace( std, "+", "-", "all" ), "/", "_", "all" ), "=", "", "all" ) );
				}
			});

			it( title="JWT style round trip (RFC 7515 / jwt.io example)", body=function( currentSpec ) {
				var header = '{"alg":"HS256","typ":"JWT"}';
				var payload = '{"sub":"1234567890","name":"John Doe","iat":1516239022}';

				var encHeader = binaryEncode( charsetDecode( header, "utf-8" ), "base64url" );
				var encPayload = binaryEncode( charsetDecode( payload, "utf-8" ), "base64url" );
				expect( encHeader ).toBe( "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" );
				expect( encPayload ).toBe( "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ" );

				var signingInput = encHeader & "." & encPayload;
				var signature = binaryEncode( binaryDecode( hmac( signingInput, "your-256-bit-secret", "HmacSHA256" ), "hex" ), "base64url" );
				expect( signature ).toBe( "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c" );

				var token = signingInput & "." & signature;
				expect( token ).toBe( "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c" );

				// decode it again
				var parts = listToArray( token, "." );
				expect( parts ).toHaveLength( 3 );
				expect( charsetEncode( binaryDecode( parts[ 1 ], "base64url", true ), "utf-8" ) ).toBe( header );
				expect( charsetEncode( binaryDecode( parts[ 2 ], "base64url", true ), "utf-8" ) ).toBe( payload );
				expect( binaryEncode( binaryDecode( parts[ 3 ], "base64url", true ), "hex" ) ).toBe(
					hmac( parts[ 1 ] & "." & parts[ 2 ], "your-256-bit-secret", "HmacSHA256" ) );
			});

			it( title="a base64url string with non ASCII payload round trips", body=function( currentSpec ) {
				var str = "Gr" & chr( 252 ) & chr( 223 ) & "e? >>> ~~~ " & chr( 8364 );
				var encoded = binaryEncode( charsetDecode( str, "utf-8" ), "base64url" );
				expect( encoded ).notToMatch( "[+/=]" );
				expect( charsetEncode( binaryDecode( encoded, "base64url", true ), "utf-8" ) ).toBe( str );
			});

			it( title="encrypt() and decrypt() accept the base64url encoding", body=function( currentSpec ) {
				var key = generateSecretKey( "AES" );
				var encrypted = encrypt( "Lucee", key, "AES", "base64url" );
				expect( encrypted ).notToMatch( "[+/=]" );
				expect( decrypt( encrypted, key, "AES", "base64url" ) ).toBe( "Lucee" );

				// same bytes as with base64
				var encrypted64 = encrypt( "Lucee", key, "AES", "base64" );
				expect( encrypted ).toBe( replace( replace( replace( encrypted64, "+", "-", "all" ), "/", "_", "all" ), "=", "", "all" ) );
			});

			it( title="invalid encoding names still throw and list base64url", body=function( currentSpec ) {
				expect( function() {
					binaryEncode( binaryDecode( "FB", "hex" ), "base64urlx" );
				}).toThrow( regex="base64url" );
				expect( function() {
					binaryDecode( "FB", "base32", true );
				}).toThrow( regex="Invalid encoding" );
			});

			it( title="the existing encodings are unchanged", body=function( currentSpec ) {
				var bin = binaryDecode( "FBFFFE", "hex" );
				expect( binaryEncode( bin, "base64" ) ).toBe( "+//+" );
				expect( binaryEncode( binaryDecode( "66", "hex" ), "base64" ) ).toBe( "Zg==" );
				expect( binaryEncode( bin, "hex" ) ).toBe( "FBFFFE" );
				expect( binaryEncode( binaryDecode( "Zg==", "base64", true ), "hex" ) ).toBe( "66" );
			});
		});
	}
}
