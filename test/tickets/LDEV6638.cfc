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

			it( title="uses the extension to tell zip based formats apart", body=function( currentSpec ) {
				var file = variables.dir & "report.docx";
				fileWrite( file, binaryDecode( "504B03041400000008000000", "hex" ) );
				// the built-in detector uses its extension map (application/msword), tika knows the exact type
				expect( [ "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ] ).toInclude( fileGetMimeType( file ) );
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
