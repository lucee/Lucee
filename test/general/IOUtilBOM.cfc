component extends="org.lucee.cfml.test.LuceeTestCase" labels="iobom" {

	function beforeAll() {
		variables.IOUtil = createObject( "java", "lucee.commons.io.IOUtil" );
		variables.Charset = createObject( "java", "java.nio.charset.Charset" );
	}

	private string function read( required string hex, string charset="UTF-8" ) {
		var is = createObject( "java", "java.io.ByteArrayInputStream" ).init( binaryDecode( arguments.hex, "hex" ) );
		return variables.IOUtil.toString( is, variables.Charset.forName( arguments.charset ) );
	}

	private string function enc( required string str, required string charset ) {
		return binaryEncode( charsetDecode( arguments.str, arguments.charset ), "hex" );
	}

	function run( testResults, testBox ) {
		describe( "lucee.commons.io.IOUtil.toString( InputStream, Charset ), byte order marks", function() {

			var text = "H" & chr( 228 ) & "llo " & chr( 8364 ) & " w" & chr( 246 ) & "rld";

			it( title="no byte order mark", body=function( currentSpec ) {
				expect( read( enc( text, "utf-8" ) ) ).toBe( text );
				expect( read( enc( text, "iso-8859-1" ), "iso-8859-1" ) ).toBe( "H" & chr( 228 ) & "llo ? w" & chr( 246 ) & "rld" );
			});

			it( title="UTF-8 mark is removed", body=function( currentSpec ) {
				expect( read( "EFBBBF" & enc( text, "utf-8" ) ) ).toBe( text );
			});

			it( title="UTF-16 big endian mark is removed and the charset is taken from it", body=function( currentSpec ) {
				expect( read( "FEFF" & enc( text, "utf-16be" ) ) ).toBe( text );
			});

			it( title="UTF-16 little endian mark is removed and the charset is taken from it", body=function( currentSpec ) {
				expect( read( "FFFE" & enc( text, "utf-16le" ) ) ).toBe( text );
			});

			it( title="UTF-32 big endian mark is removed and the charset is taken from it", body=function( currentSpec ) {
				expect( read( "0000FEFF" & enc( text, "utf-32be" ) ) ).toBe( text );
			});

			it( title="UTF-32 little endian mark is not known, it is taken as UTF-16 little endian", body=function( currentSpec ) {
				var result = read( "FFFE0000" & "41000000" );
				expect( len( result ) ).toBe( 3 );
				expect( mid( result, 2, 1 ) ).toBe( "A" );
			});

			it( title="the mark overrules the given charset", body=function( currentSpec ) {
				expect( read( "EFBBBF" & enc( text, "utf-8" ), "iso-8859-1" ) ).toBe( text );
				expect( read( "FEFF" & enc( text, "utf-16be" ), "utf-8" ) ).toBe( text );
			});

			it( title="very short streams", body=function( currentSpec ) {
				expect( read( "" ) ).toBe( "" );
				expect( read( "41" ) ).toBe( "A" );
				expect( read( "4142" ) ).toBe( "AB" );
				expect( read( "414243" ) ).toBe( "ABC" );
				expect( read( "41424344" ) ).toBe( "ABCD" );
				expect( read( "EFBBBF" ) ).toBe( "" );
				expect( read( "FEFF" ) ).toBe( "" );
				expect( read( "FFFE" ) ).toBe( "" );
				expect( read( "0000FEFF" ) ).toBe( "" );
				expect( read( "EFBBBF41" ) ).toBe( "A" );
				expect( read( "FEFF0041" ) ).toBe( "A" );
			});

			it( title="bytes that only start like a mark are kept", body=function( currentSpec ) {
				var result = read( "00004142" );
				expect( len( result ) ).toBe( 4 );
				expect( asc( left( result, 1 ) ) ).toBe( 0 );
				expect( right( result, 2 ) ).toBe( "AB" );
				expect( read( "EFBB41", "iso-8859-1" ) ).toBe( chr( 239 ) & chr( 187 ) & "A" );
				expect( read( "4142FEFF", "iso-8859-1" ) ).toBe( "AB" & chr( 254 ) & chr( 255 ) );
			});

			it( title="a mark in front of a long content", body=function( currentSpec ) {
				var big = repeatString( text & chr( 10 ), 20000 );
				var result = read( "EFBBBF" & enc( big, "utf-8" ) );
				expect( len( result ) ).toBe( len( big ) );
				expect( hash( result ) ).toBe( hash( big ) );
			});
		});
	}
}
