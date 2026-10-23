component extends="org.lucee.cfml.test.LuceeTestCase" labels="base64" {

	function beforeAll() {
		variables.B64 = createObject( "java", "lucee.commons.digest.Base64Encoder" );
	}

	private string function lenient( required string input ) {
		try {
			return binaryEncode( variables.B64.decode( arguments.input, false ), "hex" );
		}
		catch ( any e ) {
			return "ERROR:" & e.message;
		}
	}

	private string function hx( required string plain ) {
		return binaryEncode( charsetDecode( arguments.plain, "utf-8" ), "hex" );
	}

	function run( testResults, testBox ) {
		describe( "lucee.commons.digest.Base64Encoder.decode( data, precise=false ), the lenient decoder", function() {

			it( title="valid input with and without padding", body=function( currentSpec ) {
				expect( lenient( "aGVsbG8=" ) ).toBe( hx( "hello" ) );
				expect( lenient( "aGVsbG8" ) ).toBe( hx( "hello" ) );
				expect( lenient( "aGVsbA==" ) ).toBe( hx( "hell" ) );
				expect( lenient( "aGVsbA" ) ).toBe( hx( "hell" ) );
				expect( lenient( "aGVs" ) ).toBe( hx( "hel" ) );
				expect( lenient( "YQ==" ) ).toBe( hx( "a" ) );
				expect( lenient( "YQ" ) ).toBe( hx( "a" ) );
			});

			it( title="too much or misplaced padding", body=function( currentSpec ) {
				expect( lenient( "aGVsbG8==" ) ).toBe( hx( "hello" ) );
				expect( lenient( "aGVsbG8===" ) ).toBe( hx( "hello" ) );
				expect( lenient( "YQ=" ) ).toBe( hx( "a" ) );
				expect( lenient( "YQ====" ) ).toBe( hx( "a" ) );
			});

			it( title="whitespace and line breaks are ignored", body=function( currentSpec ) {
				expect( lenient( "aGVs bG8=" ) ).toBe( hx( "hello" ) );
				expect( lenient( "aGVs#chr( 13 )##chr( 10 )#bG8=" ) ).toBe( hx( "hello" ) );
				expect( lenient( "  aGVsbG8=  " ) ).toBe( hx( "hello" ) );
				expect( lenient( "aG#chr( 9 )#VsbG8=" ) ).toBe( hx( "hello" ) );
			});

			it( title="characters outside of the alphabet are ignored", body=function( currentSpec ) {
				expect( lenient( "aGVs!bG8=" ) ).toBe( hx( "hello" ) );
				expect( lenient( "aGVs.bG8=" ) ).toBe( hx( "hello" ) );
				expect( lenient( "aGV#chr( 252 )#sbG8=" ) ).toBe( hx( "hello" ) );
				expect( lenient( "<aGVsbG8=>" ) ).toBe( hx( "hello" ) );
			});

			it( title="the URL safe alphabet is accepted", body=function( currentSpec ) {
				expect( lenient( "-_-_" ) ).toBe( lenient( "+/+/" ) );
				expect( lenient( "+/+/" ) ).toBe( "fbffbf" );
				expect( lenient( "_-8" ) ).toBe( lenient( "/+8" ) );
			});

			it( title="data after the padding is ignored", body=function( currentSpec ) {
				expect( lenient( "YQ==YQ==" ) ).toBe( hx( "a" ) );
				expect( lenient( "aGVsbG8=aGVsbG8=" ) ).toBe( hx( "hello" ) );
			});

			it( title="binary content", body=function( currentSpec ) {
				expect( lenient( "AAECAwQFBgcICQ==" ) ).toBe( "00010203040506070809" );
				expect( lenient( "//79/A==" ) ).toBe( "fffefdfc" );
				expect( lenient( "AA==" ) ).toBe( "00" );
			});

			it( title="a longer text with line breaks every 76 characters", body=function( currentSpec ) {
				var plain = repeatString( "The quick brown fox jumps over the lazy dog. ", 20 );
				var enc = toBase64( plain );
				var wrapped = "";
				for ( var i = 1; i <= len( enc ); i += 76 ) wrapped &= mid( enc, i, 76 ) & chr( 13 ) & chr( 10 );
				expect( lenient( wrapped ) ).toBe( hx( plain ) );
			});

			it( title="trailing bits that are not zero are accepted", body=function( currentSpec ) {
				expect( lenient( "YR==" ) ).toBe( hx( "a" ) );
				expect( lenient( "aGVsbG9=" ) ).toBe( hx( "hello" ) );
			});

			it( title="input without a usable result fails", body=function( currentSpec ) {
				var msg = "ERROR:cannot convert the input to a binary";
				expect( lenient( "=" ) ).toBe( msg );
				expect( lenient( "====" ) ).toBe( msg );
				expect( lenient( "!!!!" ) ).toBe( msg );
				expect( lenient( "   " ) ).toBe( msg );
				expect( lenient( "A" ) ).toBe( msg );
				expect( lenient( "A===" ) ).toBe( msg );
				expect( lenient( "=YQ==" ) ).toBe( msg );
			});

			it( title="empty input gives an empty result", body=function( currentSpec ) {
				expect( lenient( "" ) ).toBe( "" );
			});

			it( title="a dangling character after full groups is dropped", body=function( currentSpec ) {
				expect( lenient( "aGVsA" ) ).toBe( hx( "hel" ) );
				expect( lenient( "aGVsbG8=A" ) ).toBe( hx( "hello" ) );
			});

			it( title="the precise mode is not changed", body=function( currentSpec ) {
				expect( binaryEncode( variables.B64.decode( "aGVsbG8=", true ), "hex" ) ).toBe( hx( "hello" ) );
				var failed = false;
				try {
					variables.B64.decode( "aGVsbG8", true );
				}
				catch ( any e ) {
					failed = true;
				}
				expect( failed ).toBeTrue();
			});
		});
	}
}
