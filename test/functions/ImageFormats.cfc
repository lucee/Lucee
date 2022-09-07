<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	public void function testImageFormats() localmode="true" {
		var imgf = ImageFormats();
		
		expect( imgf ).toBeStruct();
		expect( imgf ).toHaveKey( "decoder" );
		expect( imgf ).toHaveKey( "encoder" );

		expect( imgf.encoder ).notToBeEmpty();
		expect( imgf.decoder ).notToBeEmpty();
		
		expect( imgf.decoder ).toInclude( "JPG" );
		expect( imgf.encoder ).toInclude( "PNG" );

		// LDEV-4080
		expect( imgf.decoder ).toBeTypeOf( "array" );
		expect( imgf.encoder ).toBeTypeOf( "array" );

		// expect( isObject( imgf.decoder ) ).toBeTrue(); // i.e. should be a cfml array
		// expect( isObject( imgf.encoder ) ).toBeTrue(); // i.e. should be a cfml array
	}

	public void function testImageFormatSupport() localmode="true" {
		var writeImageFormats = ImageFormats().encoder;
		var readImageFormats = ImageFormats().decoder;
		var testFile = "";
		var testImage = imageNew( "", 100, 100, "rgb", "yellow" );
		for ( var imageFormat in writeImageFormats ){
			try {
				// test if we can write a file in this image format (image format is based on file extension )
				testFile = getTempFile( getTempDirectory(), "image-write-test", imageFormat );
				imageWrite ( testImage, testFile );
				expect( isImageFile( testFile ) ).toBeTrue( "Can write an image with the format [#imageFormat#]" );

				if ( arrayContainsNoCase( readImageFormats, imageFormat ) GT 0 ){
					// test if we can read a file in this image format (image format is based on file extension )
					imageRead ( testFile );
					expect( isImageFile( testFile ) ).toBeTrue( "Can write an image with the format [#imageFormat#]" );
				}

			} finally {
				if ( fileExists( testFile ) )
					fileDelete( testFile );
			}
		}
	}

} 
</cfscript>