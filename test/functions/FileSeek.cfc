component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title = "Testcase for fileSeek() function", body = function() {
			it( title = "checking fileSeek() function", body = function( currentSpec ) {
				var file = getTempFile( getTempDirectory(), "fileSeek", "txt" );
				fileWrite( file, "123" );
				var file = fileOpen( file=file, mode="write", seekable=true );

				fileSeek( file, 3 );
				fileWrite( file, 45 );
				expect( fileRead( file ) ).toBe( "12345" );

				fileSeek( file, 2 );
				fileWrite( file, 45 );
				expect( fileRead( file ) ).toBe( "12455" );

				fileclose( file );
				if( fileExists( file ) ) fileDelete( file );
			});
		});
	}
}