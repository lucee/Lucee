component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "LDEV-6638 mime type detection (built-in detector or the one from the MimeType extension)", function() {

			beforeEach( function() {
				variables.dir = getTempDirectory() & "LDEV6638_" & createUUID() & "/";
				directoryCreate( variables.dir );
			} );

			afterEach( function() {
				if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
			} );

			it( title="the engine provides a mime type detector", body=function( currentSpec ) {
				var detector = createObject( "java", "lucee.loader.engine.CFMLEngineFactory" ).getInstance().getMimeTypeDetector();
				expect( isNull( detector ) ).toBeFalse();
			} );

			it( title="detects a png file by its content, whatever the extension", body=function( currentSpec ) {
				var file = variables.dir & "image.dat";
				fileWrite( file, binaryDecode( "89504E470D0A1A0A0000000D49484452", "hex" ) );
				expect( fileGetMimeType( file ) ).toBe( "image/png" );
			} );

			it( title="detects a pdf file by its content", body=function( currentSpec ) {
				var file = variables.dir & "doc.dat";
				fileWrite( file, charsetDecode( "%PDF-1.4 hello", "utf-8" ) );
				expect( fileGetMimeType( file ) ).toBe( "application/pdf" );
			} );

			it( title="detects binary content passed directly", body=function( currentSpec ) {
				expect( fileGetMimeType( binaryDecode( "89504E470D0A1A0A0000000D49484452", "hex" ) ) ).toBe( "image/png" );
				expect( fileGetMimeType( charsetDecode( "%PDF-1.4 hello", "utf-8" ) ) ).toBe( "application/pdf" );
			} );

			it( title="detects plain text", body=function( currentSpec ) {
				var file = variables.dir & "readme";
				fileWrite( file, "just some plain text" );
				expect( fileGetMimeType( file ) ).toBe( "text/plain" );
			} );

			it( title="uses the extension to tell zip based formats apart (exact OOXML types)", body=function( currentSpec ) {
				var types = {
					"docx": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
					"xlsx": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
					"pptx": "application/vnd.openxmlformats-officedocument.presentationml.presentation"
				};
				loop collection=types key="local.ext" value="local.mt" {
					var file = variables.dir & "report." & ext;
					fileWrite( file, binaryDecode( "504B03041400000008000000", "hex" ) );
					expect( fileGetMimeType( file ) ).toBe( mt, ext );
				}
			} );

			it( title="UTF-16 text files are text, not audio/mpeg", body=function( currentSpec ) {
				var le = binaryDecode( "FFFE" & binaryEncode( charsetDecode( "name,city#chr(10)#Susi,Zurich", "utf-16le" ), "hex" ), "hex" );
				var be = binaryDecode( "FEFF" & binaryEncode( charsetDecode( "name,city#chr(10)#Susi,Zurich", "utf-16be" ), "hex" ), "hex" );

				fileWrite( variables.dir & "le.txt", le );
				fileWrite( variables.dir & "be.txt", be );
				fileWrite( variables.dir & "le.csv", le );
				fileWrite( variables.dir & "be.csv", be );

				expect( fileGetMimeType( variables.dir & "le.txt" ) ).toBe( "text/plain" );
				expect( fileGetMimeType( variables.dir & "be.txt" ) ).toBe( "text/plain" );
				expect( fileGetMimeType( variables.dir & "le.csv" ) ).toBe( "text/csv" );
				expect( fileGetMimeType( variables.dir & "be.csv" ) ).toBe( "text/csv" );
			} );

			it( title="text starting with BM or ID3 is not an image or mp3", body=function( currentSpec ) {
				fileWrite( variables.dir & "cars.txt", "BMW and Mercedes are both German car makers, this line is long enough." );
				fileWrite( variables.dir & "tags.txt", "ID3 notes: the tag format used in mp3 files, written down as text." );
				expect( fileGetMimeType( variables.dir & "cars.txt" ) ).toBe( "text/plain" );
				expect( fileGetMimeType( variables.dir & "tags.txt" ) ).toBe( "text/plain" );
			} );

			it( title="detects a real bmp header and a real mp3 (ID3 tag) by content", body=function( currentSpec ) {
				// BITMAPFILEHEADER (size 58, reserved 0, data offset 54) + BITMAPINFOHEADER size 40, 1x1 px
				var bmp = "424D3A000000000000003600000028000000010000000100000001001800000000000400000000000000000000000000000000000000FFFFFF00";
				fileWrite( variables.dir & "pic.dat", binaryDecode( bmp, "hex" ) );
				expect( fileGetMimeType( variables.dir & "pic.dat" ) ).toBe( "image/bmp" );

				// ID3v2.3 header, size 0, followed by an MPEG-1 Layer III frame header
				fileWrite( variables.dir & "song.dat", binaryDecode( "49443303000000000000FFFB9064000000000000", "hex" ) );
				expect( fileGetMimeType( variables.dir & "song.dat" ) ).toBe( "audio/mpeg" );
			} );

			it( title="unknown binary content is an octet-stream", body=function( currentSpec ) {
				var file = variables.dir & "data.unknownext";
				fileWrite( file, binaryDecode( "0001020304FFFE", "hex" ) );
				expect( fileGetMimeType( file ) ).toBe( "application/octet-stream" );
			} );

			it( title="detects the type of a not existing file by its extension when strict is false", body=function( currentSpec ) {
				expect( fileGetMimeType( file=variables.dir & "nope.png", strict=false ) ).toBe( "image/png" );
			} );

		} );

	}

}
