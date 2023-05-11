component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		
		describe( "test case for LDEV-2395 fileGetMimeType (strict) ", function() {
			it(title = "fileGetMimeType( strict=false ) to return image/png on real png file", body = function( currentSpec ) {
				var pngFile = expandPath("/test/artifacts/images/1.sm.png");
				expect( fileGetMimeType( file=pngFile, strict=false ) ).toBe ('image/png' );
				expect( fileGetMimeType( file=pngFile, strict=true ) ).toBe ('image/png' );
				expect( fileGetMimeType( file=pngFile) ).toBe ('image/png' );
			});

			it(title = "fileGetMimeType( strict=false ) to return image/png for empty png file", body = function( currentSpec ) {
				var pngFile = getTempFile( getTempDirectory(), "LDEV2395", "png" );
				expect( fileGetMimeType( file=pngFile, strict=false ) ).toBe ('image/png' );
				fileDelete( pngFile );
			});

			it(title = "fileGetMimeType( strict=true ) to throw on non existent png", body = function( currentSpec ) {
				expect( function(){
					fileGetMimeType( file=getTempDirectory() & createGuid() & "\LDEV2395.png", strict=true );
				} ).toThrow();
			});

			it(title = "fileGetMimeType( strict=true ) to throw on empty png file", body = function( currentSpec ) {
				var pngFile = getTempFile( getTempDirectory(), "LDEV2395", "png" );
				expect( function(){
					fileGetMimeType( file=pngFile, strict=true );
				} ).toThrow();
				fileDelete( pngFile );
			});

		});

	}
}