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

	private function doDynamicSuiteConfig() localmode=true {
		try {
			var imgFormats = evaluate("imageFormats( true )"); // v2, evaluate to bypass compile error with v1
		} catch (e){
			var imgFormats = imageFormats(); // v1
		}
		variables.writeImageFormats = {};
		variables.readImageFormats = {};
		if ( isArray( imgFormats.decoder ) ){ // imageFormats(true) returns with 2.0 by codec
			// v1
			for ( local.format in ImageFormats().encoder ){
				variables.writeImageFormats[ format ] = format;
			}
			for ( local.format in ImageFormats().decoder ){
				variables.readImageFormats[ format ] = format;
			}
		} else {
			// v2
			// remember, the extension still picks which codec itself, but we try every combination
			loop collection=imgFormats.encoder key="local.codec" value="local.formats" {
				for ( local.format in formats ){
					variables.writeImageFormats[ codec & "-" & format ] = format;
				}
			}
			loop collection=imgFormats.decoder key="local.codec" value="local.formats" {
				for ( local.format in formats ){
					variables.readImageFormatsCode[ codec & "-" & format ] = format;
					variables.readImageFormats[ format ] = format;
				}
			}
		}
		/*
		systemOutput("--------------", true);
		systemOutput( imgFormats, true);
		systemOutput( variables.readImageFormats, true);
		systemOutput( variables.writeImageFormats, true);
		systemOutput("--------------", true);
		*/
	}

	function run( testResults, testBox ) localmode=true{
		doDynamicSuiteConfig();
		variables.testImage = imageNew( "", 100, 100, "rgb", "yellow" );

		loop collection=variables.writeImageFormats key="name" value="_imageFormat" {
			systemOutput(name & " " & _imageFormat, true);
			describe("test image format: [#name#]", function(){
				it( title="test imageWrite() with format: [#name#], ",
						data={ imageFormat=_imageFormat },
						body=function( data ) {
					try {
						var imageFormat = data.imageFormat;
						// test if we can write a file in this image format (image format is based on file extension )
						var testFile = getTempFile( getTempDirectory(), "image-write-test", imageFormat );

						expect(function(){
							imageWrite ( variables.testImage, testFile );
						}).notToThrow();
					} finally {
						if ( fileExists( testFile ) )
							fileDelete( testFile );
					}
				});

				it( title="test isImageFile() with format: [#name#]",
						data={ imageFormat=_imageFormat },
						body=function( data ) {
					try {
						var imageFormat = data.imageFormat;
						// test if we can write a file in this image format (image format is based on file extension )
						var testFile = getTempFile( getTempDirectory(), "image-write-test", imageFormat );
						imageWrite ( variables.testImage, testFile );

						expect( isImageFile( testFile ) ).toBeTrue( "Can write an image with the format [#imageFormat#]" );
					} finally {
						if ( fileExists( testFile ) )
							fileDelete( testFile );
					}
				});

				it( title="test imageRead() with format: [#name#]",
						data={ imageFormat=_imageFormat },
						body=function( data ) {
					try {
						var imageFormat = data.imageFormat;
						// test if we can write a file in this image format (image format is based on file extension )
						var testFile = getTempFile( getTempDirectory(), "image-write-test", imageFormat );

						imageWrite ( variables.testImage, testFile );
						expect( isImageFile( testFile ) ).toBeTrue( "Can write an image with the format [#imageFormat#]" );

						if ( structKeyExists( variables.readImageFormats, imageFormat ) GT 0 ){
							// test if we can read a file in this image format (image format is based on file extension )
							imageRead ( testFile );
							expect( isImageFile( testFile ) ).toBeTrue( "Can write an image with the format [#imageFormat#]" );
						}

					} finally {
						if ( fileExists( testFile ) )
							fileDelete( testFile );
					}
				});
			});
		}
	}

}
</cfscript>