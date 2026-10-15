component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title="Test suite for LDEV-6052 (java.util.Base64 encoder/decoder)", body=function() {

			// The encoder was rewritten wholesale, but the only literal base64 encode
			// assertions in the suite before this were a single 13-byte ASCII string in
			// BinaryEncode.cfc and the round-trips in LDEV0555. These pin the cases a
			// rewrite actually breaks.

			it( title="padding: input length mod 3 of 0, 1 and 2", body=function( currentSpec ) {
				// The classic base64 break. Three bytes in produce four chars with no
				// padding; the two remainder cases produce one or two "=".
				expect( toBase64( "abc" ) ).toBe( "YWJj" );
				expect( toBase64( "abcd" ) ).toBe( "YWJjZA==" );
				expect( toBase64( "abcde" ) ).toBe( "YWJjZGU=" );

				expect( binaryEncode( toBinary( toBase64( "abc" ) ), "base64" ) ).toBe( "YWJj" );
				expect( binaryEncode( toBinary( toBase64( "abcd" ) ), "base64" ) ).toBe( "YWJjZA==" );
				expect( binaryEncode( toBinary( toBase64( "abcde" ) ), "base64" ) ).toBe( "YWJjZGU=" );
			});

			it( title="every byte value 0-255 encodes correctly", body=function( currentSpec ) {
				// Guards sign extension on bytes >= 0x80 — the other classic encoder bug,
				// and previously untested because every literal assertion used ASCII input.
				var bytes = [];
				for ( var i = 0; i <= 255; i++ ) {
					arrayAppend( bytes, i < 128 ? i : i - 256 );
				}
				var bin = javacast( "byte[]", bytes );

				var expected = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4"
					& "OTo7PD0+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3Bx"
					& "cnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmq"
					& "q6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj"
					& "5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+/w==";

				expect( binaryEncode( bin, "base64" ) ).toBe( expected );
				expect( len( binaryEncode( bin, "base64" ) ) ).toBe( 344 );

				// round-trip must be byte-identical
				var back = binaryDecode( expected, "base64" );
				expect( arrayLen( back ) ).toBe( 256 );
				expect( binaryEncode( back, "hex" ) ).toBe( binaryEncode( bin, "hex" ) );
			});

			it( title="empty input", body=function( currentSpec ) {
				expect( toBase64( "" ) ).toBe( "" );
				expect( binaryEncode( javacast( "byte[]", [] ), "base64" ) ).toBe( "" );
			});

			it( title="64KB payload encodes unwrapped and round-trips", body=function( currentSpec ) {
				// Nothing in the suite asserted correctness above 88 chars, and LDEV-6052's
				// own benchmark used 64KB.
				var bytes = [];
				for ( var i = 0; i < 65536; i++ ) {
					var v = i % 256;
					arrayAppend( bytes, v < 128 ? v : v - 256 );
				}
				var bin = javacast( "byte[]", bytes );
				var encoded = binaryEncode( bin, "base64" );

				expect( len( encoded ) ).toBe( 87384 );

				// Explicit guard against a getMimeEncoder() swap, which would insert CRLF
				// every 76 chars and silently corrupt every payload over that length.
				// LDEV0555 catches this by accident at 88 chars; this makes it deliberate.
				expect( find( chr( 10 ), encoded ) ).toBe( 0 );
				expect( find( chr( 13 ), encoded ) ).toBe( 0 );

				var back = binaryDecode( encoded, "base64" );
				expect( arrayLen( back ) ).toBe( 65536 );
				expect( binaryEncode( back, "hex" ) ).toBe( binaryEncode( bin, "hex" ) );
			});

			it( title="lenient decode tolerates truncated padding, precise does not", body=function( currentSpec ) {
				// Pins the lenient decoder to an implementation that accepts malformed
				// padding. "YWJjZA=" is 7 chars with a single "=" where "==" is required:
				// Apache Commons decodes it to "abcd", JDK getMimeDecoder() throws.
				// Verified by probing both decoders directly.
				//
				// Without this assertion, swapping the lenient path to getMimeDecoder()
				// leaves the whole suite green. Note the difference is PADDING, not
				// whitespace — getMimeDecoder() handles embedded newlines fine.
				expect( toString( binaryDecode( "YWJjZA=", "base64" ) ) ).toBe( "abcd" );
				expect( toString( binaryDecode( "YWJjZA=", "base64", false ) ) ).toBe( "abcd" );

				// precise mode rejects it on the length check before any decoder runs
				expect( function(){ binaryDecode( "YWJjZA=", "base64", true ) } ).toThrow();
			});

		});

		describe( title="precise decode error surface (pinned: the alphabet walk only runs after the JDK decoder rejects)", body=function() {

			it( title="wrong length reports the length", body=function( currentSpec ) {
				expect( preciseError( "YWJjZA=" ) ).toInclude( "invalid length (7)" );
			});

			it( title="more than three padding chars reports the padding count", body=function( currentSpec ) {
				expect( preciseError( "QUJD====" ) ).toInclude( "invalid padding length [4]" );
			});

			it( title="illegal char reports the char and its 1-based position", body=function( currentSpec ) {
				expect( preciseError( "QUJ-" ) ).toInclude( "invalid character [-] in base64 string at position [4]" );
				expect( preciseError( "QUJ_" ) ).toInclude( "invalid character [_] in base64 string at position [4]" );
			});

			it( title="with several illegal chars the LAST one is reported (walk runs from the end)", body=function( currentSpec ) {
				expect( preciseError( "Q-J-" ) ).toInclude( "position [4]" );
			});

			it( title="a padding char in the middle is an illegal char", body=function( currentSpec ) {
				expect( preciseError( "QU=D" ) ).toInclude( "invalid character [=] in base64 string at position [3]" );
			});

			it( title="embedded newline and non-Latin1 chars are reported with their position", body=function( currentSpec ) {
				expect( preciseError( "QUJ" & chr( 10 ) ) ).toInclude( "position [4]" );
				expect( preciseError( "QUJ" & chr( 8364 ) ) ).toInclude( "invalid character [" & chr( 8364 ) & "] in base64 string at position [4]" );
			});

			it( title="three padding chars pass the pre-checks but the decoder rejects: still a CoderException", body=function( currentSpec ) {
				// the pre-checks allow up to three pads; only the decoder knows three is wrong.
				// must surface as CoderException, never as a raw java.lang.IllegalArgumentException
				var type = "";
				try {
					binaryDecode( "Q===", "base64", true );
				}
				catch ( any e ) {
					type = e.type;
				}
				expect( type ).toBe( "lucee.runtime.coder.CoderException" );
			});

			it( title="valid input decodes with zero, one and two pads", body=function( currentSpec ) {
				expect( toString( binaryDecode( "YWJj", "base64", true ) ) ).toBe( "abc" );
				expect( toString( binaryDecode( "YWJjZGU=", "base64", true ) ) ).toBe( "abcde" );
				expect( toString( binaryDecode( "YWJjZA==", "base64", true ) ) ).toBe( "abcd" );
				expect( arrayLen( binaryDecode( "", "base64", true ) ) ).toBe( 0 );
			});

			it( title="precise round trip at every padding length and on a 64KB payload", body=function( currentSpec ) {
				for ( var n = 0; n <= 5; n++ ) {
					var s = n == 0 ? "" : left( "abcde", n );
					expect( toString( binaryDecode( toBase64( s ), "base64", true ) ) ).toBe( s );
				}
				var big = [];
				for ( var i = 0; i < 65536; i++ ) {
					big[ i + 1 ] = ( i mod 256 ) - 128;
				}
				var bin = javacast( "byte[]", big );
				var back = binaryDecode( binaryEncode( bin, "base64" ), "base64", true );
				expect( arrayLen( back ) ).toBe( 65536 );
				expect( binaryEncode( back, "hex" ) ).toBe( binaryEncode( bin, "hex" ) );
			});

		});

		describe( title="encrypt key derivation through the base64 decoder (pinned against 7.0.5.41)", body=function() {

			// Cryptor base64-decodes AES / BLOWFISH keys of 8+ chars with the caller's precise,
			// falling back to raw UTF-8 bytes when the decode fails or gives a bad length under
			// precise=false. Any change in what the decoder accepts or produces silently changes
			// the key, and existing ciphertext no longer decrypts (LDEV-4101, LDEV-5766).
			// Expected values captured on 7.0.5.41, before LDEV-6052. "ERR:" is a fragment of
			// the expected error message.
			var cases = [
				[ "my secret key!", "BLOWFISH", true, "ERR:invalid length (14)" ],
				[ "my secret key!", "BLOWFISH", false, "WRPaIn4IefU=" ],
				[ "my secret key!", "AES", true, "ERR:invalid length (14)" ],
				[ "my secret key!", "AES", false, "ixIDbfyIZvgJcwBX7/NlCQ==" ],
				// lenient decode drops non-alphabet chars, so this collapses to the same BLOWFISH key as above
				[ "my secret key!!!", "BLOWFISH", true, "ERR:invalid character [!] in base64 string at position [16]" ],
				[ "my secret key!!!", "BLOWFISH", false, "WRPaIn4IefU=" ],
				[ "my secret key!!!", "AES", true, "ERR:invalid character [!] in base64 string at position [16]" ],
				[ "my secret key!!!", "AES", false, "tqHa3XftQK7daM8la5Zghw==" ],
				[ "key12345", "BLOWFISH", true, "LFAfnDbOWhg=" ],
				[ "key12345", "BLOWFISH", false, "LFAfnDbOWhg=" ],
				[ "key12345", "AES", true, "ERR:Invalid key length for AES: 6 bytes" ],
				[ "key12345", "AES", false, "0n6zXEKRJEdveInwqPLFmw==" ],
				[ "abcdefgh", "BLOWFISH", true, "7NSdayl9AU0=" ],
				[ "abcdefgh", "BLOWFISH", false, "7NSdayl9AU0=" ],
				[ "abcdefgh", "AES", true, "ERR:Invalid key length for AES: 6 bytes" ],
				[ "abcdefgh", "AES", false, "ON+y+Sksq19y0hykMQgZjA==" ],
				[ "abc def ghi jkl", "BLOWFISH", true, "ERR:invalid length (15)" ],
				[ "abc def ghi jkl", "BLOWFISH", false, "1WNxURuHgg4=" ],
				[ "abc def ghi jkl", "AES", true, "ERR:invalid length (15)" ],
				[ "abc def ghi jkl", "AES", false, "wY6se8J+RHOfMQCj3HKs/g==" ],
				[ "a-b_c.d~e", "BLOWFISH", true, "ERR:invalid length (9)" ],
				[ "a-b_c.d~e", "BLOWFISH", false, "x3lQ2QMYTJI=" ],
				[ "a-b_c.d~e", "AES", true, "ERR:invalid length (9)" ],
				[ "a-b_c.d~e", "AES", false, "E6QxuuZ4D7AF/DNRV+4PwQ==" ],
				[ "key12345=", "BLOWFISH", true, "ERR:invalid length (9)" ],
				[ "key12345=", "BLOWFISH", false, "LFAfnDbOWhg=" ],
				[ "key12345=", "AES", true, "ERR:invalid length (9)" ],
				[ "key12345=", "AES", false, "0Oyl62POxn3rSUqcs+vzvA==" ],
				[ "ab==cdef", "BLOWFISH", true, "ERR:invalid character [=] in base64 string at position [4]" ],
				[ "ab==cdef", "BLOWFISH", false, "GtkYFHHwsGs=" ],
				[ "ab==cdef", "AES", true, "ERR:invalid character [=] in base64 string at position [4]" ],
				[ "ab==cdef", "AES", false, "DoCFSGhJPXvbaIzGLc1wkw==" ],
				[ "ab=cdefg", "BLOWFISH", true, "ERR:invalid character [=] in base64 string at position [3]" ],
				[ "ab=cdefg", "BLOWFISH", false, "xRjNWMZYjdU=" ],
				[ "ab=cdefg", "AES", true, "ERR:invalid character [=] in base64 string at position [3]" ],
				[ "ab=cdefg", "AES", false, "bhXznoYztpumoSM+XgAzqA==" ],
				[ "=abcdefg", "BLOWFISH", true, "ERR:invalid character [=] in base64 string at position [1]" ],
				[ "=abcdefg", "BLOWFISH", false, "AhztpE6c4SM=" ],
				[ "=abcdefg", "AES", true, "ERR:invalid character [=] in base64 string at position [1]" ],
				[ "=abcdefg", "AES", false, "9QtjLtT7T3Zvt8PGM55ajA==" ],
				[ "key12345==", "BLOWFISH", true, "ERR:invalid length (10)" ],
				[ "key12345==", "BLOWFISH", false, "LFAfnDbOWhg=" ],
				[ "key12345==", "AES", true, "ERR:invalid length (10)" ],
				[ "key12345==", "AES", false, "gqeqY2yzWdMq21WYA1jTRg==" ],
				[ "key1 2345", "BLOWFISH", true, "ERR:invalid length (9)" ],
				[ "key1 2345", "BLOWFISH", false, "LFAfnDbOWhg=" ],
				[ "key1 2345", "AES", true, "ERR:invalid length (9)" ],
				[ "key1 2345", "AES", false, "0fDfm46zOtnRyvtggt/GRA==" ],
				[ "key-12345", "BLOWFISH", true, "ERR:invalid length (9)" ],
				[ "key-12345", "BLOWFISH", false, "gnkIQYaITnY=" ],
				[ "key-12345", "AES", true, "ERR:invalid length (9)" ],
				[ "key-12345", "AES", false, "0sww2ajH2RDFqASwR2Q++g==" ],
				[ repeatString( chr( 233 ), 8 ), "BLOWFISH", true, "ERR:in base64 string at position [8]" ],
				[ repeatString( chr( 233 ), 8 ), "BLOWFISH", false, "fADcmYKiVAg=" ],
				[ repeatString( chr( 233 ), 8 ), "AES", true, "ERR:in base64 string at position [8]" ],
				[ repeatString( chr( 233 ), 8 ), "AES", false, "mJQecvWkEZVy9zqi6OV6tw==" ],
				[ "this is a 32 char passphrase!!!!", "BLOWFISH", true, "ERR:invalid character [!] in base64 string at position [32]" ],
				[ "this is a 32 char passphrase!!!!", "BLOWFISH", false, "mS3tCwINid4=" ],
				[ "this is a 32 char passphrase!!!!", "AES", true, "ERR:invalid character [!] in base64 string at position [32]" ],
				[ "this is a 32 char passphrase!!!!", "AES", false, "ewnHuAW5M0VPRjsEqb13yg==" ],
				// a real 16 byte base64 key, then the same key unpadded, short-padded and space separated
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng==", "BLOWFISH", true, "3Aakf/H0Sz4=" ],
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng==", "BLOWFISH", false, "3Aakf/H0Sz4=" ],
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng==", "AES", true, "zvMyCrBG6v7Jtdo7Ptkgww==" ],
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng==", "AES", false, "zvMyCrBG6v7Jtdo7Ptkgww==" ],
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng", "BLOWFISH", true, "ERR:invalid length (22)" ],
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng", "BLOWFISH", false, "3Aakf/H0Sz4=" ],
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng", "AES", true, "ERR:invalid length (22)" ],
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng", "AES", false, "zvMyCrBG6v7Jtdo7Ptkgww==" ],
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng=", "BLOWFISH", true, "ERR:invalid length (23)" ],
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng=", "BLOWFISH", false, "3Aakf/H0Sz4=" ],
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng=", "AES", true, "ERR:invalid length (23)" ],
				[ "MTIzNDU2Nzg5MDEyMzQ1Ng=", "AES", false, "zvMyCrBG6v7Jtdo7Ptkgww==" ],
				[ "MTIz NDU2 Nzg5 MDEy MzQ1 Ng==", "BLOWFISH", true, "ERR:invalid length (29)" ],
				[ "MTIz NDU2 Nzg5 MDEy MzQ1 Ng==", "BLOWFISH", false, "3Aakf/H0Sz4=" ],
				[ "MTIz NDU2 Nzg5 MDEy MzQ1 Ng==", "AES", true, "ERR:invalid length (29)" ],
				[ "MTIz NDU2 Nzg5 MDEy MzQ1 Ng==", "AES", false, "zvMyCrBG6v7Jtdo7Ptkgww==" ]
			];

			loop array=cases item="local.c" {
				it( title="[#c[ 1 ]#] #c[ 2 ]# precise=#c[ 3 ]#", data={ key=c[ 1 ], algo=c[ 2 ], precise=c[ 3 ], expected=c[ 4 ] }, body=function( data ) {
					var result = encryptKeyProbe( arguments.data.key, arguments.data.algo, arguments.data.precise );
					if ( left( arguments.data.expected, 4 ) == "ERR:" ) {
						expect( result ).toInclude( mid( arguments.data.expected, 5 ) );
						expect( left( result, 4 ) ).toBe( "ERR:" );
					}
					else {
						expect( result ).toBe( arguments.data.expected );
						expect( toString( decrypt( result, arguments.data.key, arguments.data.algo, "base64", "", 0, arguments.data.precise ) ) ).toBe( "lucee" );
					}
				});
			}

		});
	}

	private string function encryptKeyProbe( required string key, required string algo, required boolean precise ) {
		try {
			return encrypt( "lucee", arguments.key, arguments.algo, "base64", "", 0, arguments.precise );
		}
		catch ( any e ) {
			return "ERR:" & e.message;
		}
	}

	private string function preciseError( required string encoded ) {
		try {
			binaryDecode( arguments.encoded, "base64", true );
		}
		catch ( any e ) {
			return e.message;
		}
		return "(no exception)";
	}

}
