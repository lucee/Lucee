component extends="org.lucee.cfml.test.LuceeTestCase" labels="multipart" {

	function beforeAll() {
		variables.IOUtil = createObject( "java", "lucee.commons.io.IOUtil" );
		variables.Charset = createObject( "java", "java.nio.charset.Charset" );
		variables.ParserClass = createObject( "java", "lucee.runtime.net.http.MultipartFormParser" );
		variables.nul = charsetEncode( binaryDecode( "00", "hex" ), "iso-8859-1" );
		variables.fixtures = getDirectoryFromPath( getCurrentTemplatePath() ) & "MultipartParser/";
	}

	private any function parser( required any body, string ct="multipart/form-data; boundary=BB", string cs="UTF-8" ) {
		var is = createObject( "java", "java.io.ByteArrayInputStream" ).init( arguments.body );
		return variables.ParserClass.init( is, arguments.ct, variables.Charset.forName( arguments.cs ) );
	}

	// one line per item: field|isFormField|fileName|contentType|content as hex, like the recorded results
	private array function readAll( required any parser ) {
		var lines = [];
		while ( true ) {
			var item = arguments.parser.next();
			if ( isNull( item ) ) break;
			var name = item.getName();
			var ct = item.getContentType();
			var content = variables.IOUtil.toBytes( item.getInputStream() );
			lines.append( item.getFieldName() & "|" & item.isFormField() & "|" & ( isNull( name ) ? "null" : name ) & "|" & ( isNull( ct ) ? "null" : ct ) & "|" & lCase( binaryEncode( content, "hex" ) ) );
		}
		return lines;
	}

	private binary function bytes( required string isoString ) {
		return charsetDecode( arguments.isoString, "iso-8859-1" );
	}

	private string function allBytes( required numeric count ) {
		var s = createObject( "java", "java.lang.StringBuilder" ).init();
		for ( var i = 0; i < arguments.count; i++ ) s.append( ( i mod 256 ) == 0 ? variables.nul : chr( i mod 256 ) );
		return s.toString();
	}

	private string function disp( required string name, string filename, string ct ) {
		var h = 'Content-Disposition: form-data; name="#arguments.name#"';
		if ( !isNull( arguments.filename ) ) h &= '; filename="#arguments.filename#"';
		if ( !isNull( arguments.ct ) ) h &= chr( 13 ) & chr( 10 ) & "Content-Type: " & arguments.ct;
		return h;
	}

	private string function part( required string headers, required string content, string boundary="BB" ) {
		return "--" & arguments.boundary & chr( 13 ) & chr( 10 ) & arguments.headers & chr( 13 ) & chr( 10 ) & chr( 13 ) & chr( 10 ) & arguments.content & chr( 13 ) & chr( 10 );
	}

	private string function close( string boundary="BB" ) {
		return "--" & arguments.boundary & "--" & chr( 13 ) & chr( 10 );
	}

	private string function failureOf( required any body, string ct="multipart/form-data; boundary=BB" ) {
		try {
			readAll( parser( arguments.body, arguments.ct ) );
		}
		catch ( any e ) {
			return e.type & ": " & e.message;
		}
		return "";
	}

	function run( testResults, testBox ) {
		describe( "lucee.runtime.net.http.MultipartFormParser, recorded requests", function() {

			it( title="gives the same items as commons-fileupload2 did for the recorded requests", body=function( currentSpec ) {
				// caseNN.bin is the request, caseNN.txt the items commons-fileupload2 returned for it.
				// 07, 11 and 12 are not part of the recorded set, the numbering has gaps.
				// commons-fileupload2 is no longer a dependency, so new behaviour goes into the specs below and not here.
				var cases = directoryList( variables.fixtures, false, "path", "*.bin", "name" );
				expect( arrayLen( cases ) ).toBe( 11 );
				for ( var f in cases ) {
					var expected = listToArray( fileRead( replace( f, ".bin", ".txt" ), "utf-8" ), chr( 10 ), false, false );
					var ct = expected[ 1 ];
					var cs = expected[ 2 ];
					arrayDeleteAt( expected, 1 );
					arrayDeleteAt( expected, 1 );
					var actual = readAll( parser( fileReadBinary( f ), ct, cs ) );
					expect( actual.len() ).toBe( expected.len(), listLast( f, "/" ) );
					for ( var i = 1; i <= expected.len(); i++ ) {
						expect( actual[ i ] ).toBe( expected[ i ], listLast( f, "/" ) & " item " & i );
					}
				}
			});
		});

		describe( "lucee.runtime.net.http.MultipartFormParser, content", function() {

			it( title="fields and a file with every byte value", body=function( currentSpec ) {
				var all = allBytes( 1024 );
				var body = bytes( part( disp( "a" ), "one" ) & part( disp( "f", "all.bin", "application/octet-stream" ), all ) & part( disp( "b" ), "two" ) & close() );
				var items = readAll( parser( body ) );
				expect( items ).toHaveLength( 3 );
				expect( items[ 1 ] ).toBe( "a|true|null|null|" & lCase( binaryEncode( bytes( "one" ), "hex" ) ) );
				expect( items[ 2 ] ).toBe( "f|false|all.bin|application/octet-stream|" & lCase( binaryEncode( bytes( all ), "hex" ) ) );
				expect( items[ 3 ] ).toBe( "b|true|null|null|" & lCase( binaryEncode( bytes( "two" ), "hex" ) ) );
			});

			it( title="content that looks like a boundary is kept", body=function( currentSpec ) {
				var content = "x" & chr( 13 ) & chr( 10 ) & "--B" & chr( 13 ) & chr( 10 ) & "--Bx" & chr( 13 ) & "--BB" & chr( 10 ) & "-" & chr( 13 ) & chr( 10 ) & "--";
				var items = readAll( parser( bytes( part( disp( "f", "x.txt" ), content ) & close() ) ) );
				expect( items ).toHaveLength( 1 );
				expect( items[ 1 ] ).toBe( "f|false|x.txt|null|" & lCase( binaryEncode( bytes( content ), "hex" ) ) );
			});

			it( title="content of every size around the size of the internal buffer", body=function( currentSpec ) {
				for ( var size in [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 16370, 16380, 16383, 16384, 16385, 16390, 16400, 32768, 32770, 65536, 65540 ] ) {
					var content = allBytes( size );
					var items = readAll( parser( bytes( part( disp( "f", "x.bin" ), content ) & part( disp( "g" ), "after" ) & close() ) ) );
					expect( items ).toHaveLength( 2, "size #size#" );
					expect( hash( listLast( items[ 1 ], "|", true ) ) ).toBe( hash( lCase( binaryEncode( bytes( content ), "hex" ) ) ), "size #size#" );
					expect( items[ 2 ] ).toBe( "g|true|null|null|" & lCase( binaryEncode( bytes( "after" ), "hex" ) ), "size #size#" );
				}
			});

			it( title="a big file is streamed", body=function( currentSpec ) {
				var chunk = allBytes( 4096 );
				var content = repeatString( chunk, 1024 ); // 4 MB
				var p = parser( bytes( part( disp( "f", "big.bin" ), content ) & close() ) );
				var item = p.next();
				var size = 0;
				var buf = createObject( "java", "java.lang.reflect.Array" ).newInstance( createObject( "java", "java.lang.Byte" ).TYPE, 8192 );
				var is = item.getInputStream();
				while ( true ) {
					var r = is.read( buf );
					if ( r == -1 ) break;
					size += r;
				}
				expect( size ).toBe( len( content ) );
				expect( size ).toBe( 4 * 1024 * 1024 );
				expect( isNull( p.next() ) ).toBeTrue();
			});

			it( title="a preamble and an epilogue are ignored, the closing line break is optional", body=function( currentSpec ) {
				var body = bytes( "preamble" & chr( 13 ) & chr( 10 ) & part( disp( "a" ), "one" ) & "--BB--" & chr( 13 ) & chr( 10 ) & "epilogue" );
				expect( readAll( parser( body ) ) ).toHaveLength( 1 );
				expect( readAll( parser( bytes( part( disp( "a" ), "one" ) & "--BB--" ) ) ) ).toHaveLength( 1 );
				expect( readAll( parser( bytes( part( disp( "a" ), "one" ) & "--BB" ) ) ) ).toHaveLength( 1 );
			});

			it( title="parts without a name are skipped", body=function( currentSpec ) {
				var body = bytes( part( "X-Other: 1", "no name" ) & part( 'Content-Disposition: form-data; name=""', "empty name" ) & part( disp( "a" ), "one" ) & close() );
				var items = readAll( parser( body ) );
				expect( items ).toHaveLength( 1 );
				expect( items[ 1 ] ).toBe( "a|true|null|null|" & lCase( binaryEncode( bytes( "one" ), "hex" ) ) );
			});

			it( title="an attachment is only taken into account inside of a multipart/mixed section", body=function( currentSpec ) {
				var body = bytes( part( 'Content-Disposition: attachment; name="x"', "no" ) & close() );
				expect( readAll( parser( body ) ) ).toHaveLength( 0 );
			});

			it( title="multipart/mixed sections with several files for one field", body=function( currentSpec ) {
				var inner = part( 'Content-Disposition: attachment; filename="one.txt"' & chr( 13 ) & chr( 10 ) & "Content-Type: text/plain", "file one", "SUB" )
					& part( 'Content-Disposition: attachment; filename="two.gif"' & chr( 13 ) & chr( 10 ) & "Content-Type: image/gif", "GIF89a", "SUB" )
					& close( "SUB" );
				var body = bytes( part( 'Content-Disposition: form-data; name="files"' & chr( 13 ) & chr( 10 ) & "Content-Type: multipart/mixed; boundary=SUB", inner )
					& part( disp( "after" ), "value" ) & close() );
				var items = readAll( parser( body ) );
				expect( items ).toHaveLength( 3 );
				expect( items[ 1 ] ).toBe( "files|false|one.txt|text/plain|" & lCase( binaryEncode( bytes( "file one" ), "hex" ) ) );
				expect( items[ 2 ] ).toBe( "files|false|two.gif|image/gif|" & lCase( binaryEncode( bytes( "GIF89a" ), "hex" ) ) );
				expect( items[ 3 ] ).toBe( "after|true|null|null|" & lCase( binaryEncode( bytes( "value" ), "hex" ) ) );
			});
		});

		describe( "lucee.runtime.net.http.MultipartFormParser, a multipart/mixed section", function() {

			it( title="a part without a file name inside of the section is skipped", body=function( currentSpec ) {
				var inner = part( 'Content-Disposition: attachment; filename="one.txt"', "file one", "SUB" )
					& part( "X-Other: 1", "no file name", "SUB" )
					& part( 'Content-Disposition: attachment', "no file name either", "SUB" )
					& part( 'Content-Disposition: attachment; filename="two.txt"', "file two", "SUB" )
					& close( "SUB" );
				var body = bytes( part( 'Content-Disposition: form-data; name="files"' & chr( 13 ) & chr( 10 ) & "Content-Type: multipart/mixed; boundary=SUB", inner ) & part( disp( "after" ), "value" ) & close() );
				var items = readAll( parser( body ) );
				expect( items ).toHaveLength( 3 );
				expect( items[ 1 ] ).toInclude( "files|false|one.txt|" );
				expect( items[ 2 ] ).toInclude( "files|false|two.txt|" );
				expect( items[ 3 ] ).toInclude( "after|true|" );
			});
		});

		describe( "lucee.runtime.net.http.MultipartFormParser, encoded file names", function() {

			var ue = chr( 252 ) & ".txt";

			it( title="RFC 2231/5987: filename* with charset and percent encoding", body=function( currentSpec ) {
				var items = readAll( parser( bytes( part( "Content-Disposition: form-data; name=""f""; filename*=UTF-8''%C3%BC.txt", "x" ) & close() ) ) );
				expect( items ).toHaveLength( 1 );
				expect( items[ 1 ] ).toBe( "f|false|" & ue & "|null|78" );
			});

			it( title="filename* with another charset, a language and lower case hex", body=function( currentSpec ) {
				var items = readAll( parser( bytes( part( "Content-Disposition: form-data; name=""f""; filename*=iso-8859-1'de'%fc.txt", "x" ) & close() ) ) );
				expect( items[ 1 ] ).toBe( "f|false|" & ue & "|null|78" );
			});

			it( title="filename* takes precedence over filename, in any order", body=function( currentSpec ) {
				var a = readAll( parser( bytes( part( "Content-Disposition: form-data; name=""f""; filename=""fallback.txt""; filename*=UTF-8''%C3%BC.txt", "x" ) & close() ) ) );
				var b = readAll( parser( bytes( part( "Content-Disposition: form-data; name=""f""; filename*=UTF-8''%C3%BC.txt; filename=""fallback.txt""", "x" ) & close() ) ) );
				expect( a[ 1 ] ).toBe( "f|false|" & ue & "|null|78" );
				expect( b[ 1 ] ).toBe( "f|false|" & ue & "|null|78" );
			});

			it( title="an item with only filename* is a file", body=function( currentSpec ) {
				var items = readAll( parser( bytes( part( "Content-Disposition: form-data; name=""f""; filename*=UTF-8''a%20b.txt", "x" ) & close() ) ) );
				expect( items[ 1 ] ).toInclude( "f|false|a b.txt|" );
			});

			it( title="filename* that cannot be decoded is kept as it is", body=function( currentSpec ) {
				expect( readAll( parser( bytes( part( "Content-Disposition: form-data; name=""f""; filename*=NoSuchCharset''%C3%BC.txt", "x" ) & close() ) ) )[ 1 ] ).toInclude( "|NoSuchCharset''%C3%BC.txt|" );
				expect( readAll( parser( bytes( part( "Content-Disposition: form-data; name=""f""; filename*=plain.txt", "x" ) & close() ) ) )[ 1 ] ).toInclude( "|plain.txt|" );
			});

			it( title="RFC 2047 encoded words, base64 and quoted printable", body=function( currentSpec ) {
				var b64 = readAll( parser( bytes( part( 'Content-Disposition: form-data; name="f"; filename="=?UTF-8?B?w7wudHh0?="', "x" ) & close() ) ) );
				var q = readAll( parser( bytes( part( 'Content-Disposition: form-data; name="f"; filename="=?UTF-8?Q?=C3=BC.txt?="', "x" ) & close() ) ) );
				var lq = readAll( parser( bytes( part( 'Content-Disposition: form-data; name="f"; filename="=?ISO-8859-1?q?=FC_a.txt?="', "x" ) & close() ) ) );
				expect( b64[ 1 ] ).toBe( "f|false|" & ue & "|null|78" );
				expect( q[ 1 ] ).toBe( "f|false|" & ue & "|null|78" );
				expect( lq[ 1 ] ).toBe( "f|false|" & chr( 252 ) & " a.txt|null|78" );
			});

			it( title="adjacent encoded words are joined, text around them is kept", body=function( currentSpec ) {
				var items = readAll( parser( bytes( part( 'Content-Disposition: form-data; name="f"; filename="=?UTF-8?B?w7w=?= =?UTF-8?Q?=C3=BC?= x.txt"', "x" ) & close() ) ) );
				expect( items[ 1 ] ).toBe( "f|false|" & chr( 252 ) & chr( 252 ) & " x.txt|null|78" );
			});

			it( title="an encoded word that cannot be decoded is kept as it is", body=function( currentSpec ) {
				var items = readAll( parser( bytes( part( 'Content-Disposition: form-data; name="f"; filename="=?NoSuchCharset?B?w7w=?=.txt"', "x" ) & close() ) ) );
				expect( items[ 1 ] ).toInclude( "|=?NoSuchCharset?B?w7w=?=.txt|" );
			});

			it( title="the field name can be encoded as well", body=function( currentSpec ) {
				var items = readAll( parser( bytes( part( "Content-Disposition: form-data; name*=UTF-8''%C3%BC", "x" ) & close() ) ) );
				expect( items[ 1 ] ).toBe( chr( 252 ) & "|true|null|null|78" );
			});
		});

		describe( "lucee.runtime.net.http.MultipartFormParser, headers", function() {

			it( title="names and file names are decoded with the given charset", body=function( currentSpec ) {
				var name = "n" & chr( 252 ) & chr( 228 );
				var utf = charsetEncode( charsetDecode( part( disp( name, name & ".txt" ), "x" ) & close(), "utf-8" ), "iso-8859-1" );
				var items = readAll( parser( charsetDecode( part( disp( name, name & ".txt" ), "x" ) & close(), "utf-8" ), "multipart/form-data; boundary=BB", "UTF-8" ) );
				expect( items[ 1 ] ).toBe( name & "|false|" & name & ".txt|null|78" );
				items = readAll( parser( charsetDecode( part( disp( name, name & ".txt" ), "x" ) & close(), "iso-8859-1" ), "multipart/form-data; boundary=BB", "ISO-8859-1" ) );
				expect( items[ 1 ] ).toBe( name & "|false|" & name & ".txt|null|78" );
			});

			it( title="header names and the disposition type are not case sensitive", body=function( currentSpec ) {
				var body = bytes( part( 'content-disposition: Form-Data; NAME="a"; FileName="b.txt"' & chr( 13 ) & chr( 10 ) & "CONTENT-TYPE: text/plain", "x" ) & close() );
				expect( readAll( parser( body ) )[ 1 ] ).toBe( "a|false|b.txt|text/plain|78" );
			});

			it( title="a space in front of the colon, a folded line and a quoted semicolon", body=function( currentSpec ) {
				var body = bytes( part( 'Content-Disposition : form-data;' & chr( 13 ) & chr( 10 ) & chr( 9 ) & 'name="a;b"; filename="c;d.txt"', "x" ) & close() );
				expect( readAll( parser( body ) )[ 1 ] ).toBe( "a;b|false|c;d.txt|null|78" );
			});

			it( title="an empty file name is a file, with an empty name", body=function( currentSpec ) {
				var body = bytes( part( disp( "a", "" ), "" ) & close() );
				expect( readAll( parser( body ) )[ 1 ] ).toBe( "a|false||null|" );
			});

			it( title="the boundary can be quoted, the type is not case sensitive", body=function( currentSpec ) {
				var body = bytes( part( disp( "a" ), "x", "b-1" ) & close( "b-1" ) );
				expect( readAll( parser( body, 'Multipart/Form-Data; Boundary="b-1"' ) ) ).toHaveLength( 1 );
				expect( readAll( parser( body, "multipart/form-data; charset=utf-8; boundary=b-1" ) ) ).toHaveLength( 1 );
			});

			it( title="a long file name works", body=function( currentSpec ) {
				var name = repeatString( "a", 2000 ) & ".txt";
				var body = bytes( part( disp( "a", name ), "x" ) & close() );
				expect( readAll( parser( body ) )[ 1 ] ).toBe( "a|false|" & name & "|null|78" );
			});

			it( title="the charset of the content type of a field is used to read the value", body=function( currentSpec ) {
				var body = bytes( part( disp( "a", javaCast( "null", "" ), "text/plain; charset=utf-8" ), chr( 195 ) & chr( 188 ) ) & close() );
				var item = parser( body, "multipart/form-data; boundary=BB", "UTF-8" ).next();
				expect( item.getString( javaCast( "null", "" ) ) ).toBe( chr( 252 ) );
				item = parser( bytes( part( disp( "a" ), chr( 252 ) ) & close() ) ).next();
				expect( item.getString( javaCast( "null", "" ) ) ).toBe( chr( 252 ) );
			});
		});

		describe( "lucee.runtime.net.http.MultipartFormParser, invalid requests", function() {

			it( title="a request that is not multipart", body=function( currentSpec ) {
				expect( failureOf( bytes( "a=b" ), "application/x-www-form-urlencoded" ) ).toInclude( "MalformedMultipartException" );
				expect( failureOf( bytes( "a=b" ), "" ) ).toInclude( "MalformedMultipartException" );
			});

			it( title="no boundary", body=function( currentSpec ) {
				expect( failureOf( bytes( part( disp( "a" ), "x" ) & close() ), "multipart/form-data" ) ).toInclude( "MalformedMultipartException" );
				expect( failureOf( bytes( part( disp( "a" ), "x" ) & close() ), "multipart/form-data; boundary=" ) ).toInclude( "MalformedMultipartException" );
			});

			it( title="a stream that ends in a part", body=function( currentSpec ) {
				var full = part( disp( "f", "x.bin" ), repeatString( "0123456789", 100 ) ) & close();
				for ( var cut in [ 10, 40, 100, 200, 500, len( full ) - 20 ] ) {
					expect( failureOf( bytes( left( full, cut ) ) ) ).toInclude( "MalformedMultipartException", "cut at #cut#" );
				}
			});

			it( title="characters after a boundary", body=function( currentSpec ) {
				expect( failureOf( bytes( part( disp( "a" ), "x" ) & "--BBx" & chr( 13 ) & chr( 10 ) ) ) ).toInclude( "MalformedMultipartException" );
			});

			it( title="a header section that is too big", body=function( currentSpec ) {
				var body = bytes( part( disp( "a" ) & chr( 13 ) & chr( 10 ) & "X-Big: " & repeatString( "x", 11000 ), "x" ) & close() );
				expect( failureOf( body ) ).toInclude( "MalformedMultipartException" );
				body = bytes( part( disp( "a" ) & chr( 13 ) & chr( 10 ) & "X-Big: " & repeatString( "x", 9000 ), "x" ) & close() );
				expect( failureOf( body ) ).toBe( "" );
			});

			it( title="a file name with a zero byte", body=function( currentSpec ) {
				var body = bytes( part( disp( "a", "evil" & variables.nul & ".txt" ), "x" ) & close() );
				expect( failureOf( body ) ).toInclude( "MalformedMultipartException" );
			});
		});

		describe( "lucee.runtime.net.http.MultiPartResponseUtils.getParts", function() {

			it( title="splits the body of a multipart response", body=function( currentSpec ) {
				var Utils = createObject( "java", "lucee.runtime.net.http.MultiPartResponseUtils" );
				var body = bytes( "--XX" & chr( 13 ) & chr( 10 ) & "Content-Type: text/plain" & chr( 13 ) & chr( 10 ) & "X-Id: 1" & chr( 13 ) & chr( 10 ) & chr( 13 ) & chr( 10 ) & "first" & chr( 13 ) & chr( 10 )
					& "--XX" & chr( 13 ) & chr( 10 ) & "Content-Type: application/octet-stream" & chr( 13 ) & chr( 10 ) & chr( 13 ) & chr( 10 ) & allBytes( 600 ) & chr( 13 ) & chr( 10 ) & "--XX--" & chr( 13 ) & chr( 10 ) );
				var parts = Utils.getParts( body, "multipart/mixed; boundary=XX" );
				expect( arrayLen( parts ) ).toBe( 2 );
				expect( parts[ 1 ].headers[ "Content-Type" ] ).toBe( "text/plain" );
				expect( parts[ 1 ].headers[ "X-Id" ] ).toBe( "1" );
				expect( toString( parts[ 1 ].content ) ).toBe( "first" );
				expect( binaryEncode( parts[ 2 ].content, "hex" ) ).toBe( binaryEncode( bytes( allBytes( 600 ) ), "hex" ) );
			});
		});
	}
}
