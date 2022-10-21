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
		var excludedFormats = getExcludedFormats();
		if ( isArray( imgFormats.decoder ) ){ // imageFormats(true) returns with 2.0 by codec
			// v1
			for ( local.format in ImageFormats().encoder ){
				if ( !structKeyExists( excludedFormats, format ) )
					variables.writeImageFormats[ format ] = format;
			}
			for ( local.format in ImageFormats().decoder ){
				if ( !structKeyExists( excludedFormats, format ) )
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
					variables.readImageFormatsCodec[ codec & "-" & format ] = format;
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

	private struct function getExcludedFormats(){
		var qry = extensionlist(false);
		loop query=qry {
			if(qry.id=="B737ABC4-D43F-4D91-8E8E973E37C40D1B") {
				if(left(qry.version,1)>=2) return {};
			}
		}
		return {
			xbm: true,
			pcx: true,
			xpm: true
		};
	}

	function run( testResults, testBox ) localmode=true{
		doDynamicSuiteConfig();
		variables.testImage = imageNew( "", 256, 256, "rgb", "yellow" );

		loop collection=variables.writeImageFormats key="name" value="_imageFormat" {
			//systemOutput(name & " " & _imageFormat, true);
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
						var testFile = getTempFile( getTempDirectory(), "image-write-test", imageFormat );
						if ( structKeyExists( variables.readImageFormats, imageFormat ) ){
							imageWrite ( variables.testImage, testFile );

							expect( isImageFile( testFile ) ).toBeTrue( "isImageFile with the format [#imageFormat#]" );
						}
					} finally {
						if ( fileExists( testFile ) )
							fileDelete( testFile );
					}
				});

				it( title="test imageInfo() with format: [#name#]",
						data={ imageFormat=_imageFormat },
						body=function( data ) {
					try {
						var imageFormat = data.imageFormat;
						var testFile = getTempFile( getTempDirectory(), "image-info-test", imageFormat );
						if ( structKeyExists( variables.readImageFormats, imageFormat ) ){
							imageWrite ( variables.testImage, testFile );

							expect( imageInfo( testFile ) ).toBeStruct( "Can read ImageInfo with the format [#imageFormat#]" );
						}
					} finally {
						if ( fileExists( testFile ) )
							fileDelete( testFile );
					}
				});

				it( title="test ImageGetExifMetaData() with format: [#name#]",
						data={ imageFormat=_imageFormat },
						body=function( data ) {
					try {
						var imageFormat = data.imageFormat;
						var testFile = getTempFile( getTempDirectory(), "image-exif-test", imageFormat );
						if ( structKeyExists( variables.readImageFormats, imageFormat ) ){
							imageWrite ( variables.testImage, testFile );

							expect( ImageGetExifMetaData( testFile ) ).toBeStruct( "Can read ImageGetExifMetaData() with the format [#imageFormat#]" );
						}
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
						var testFile = getTempFile( getTempDirectory(), "image-write-test", imageFormat );
						if ( structKeyExists( variables.readImageFormats, imageFormat ) ){
							imageWrite ( variables.testImage, testFile );
							// test if we can read a file in this image format (image format is based on file extension )
							var img = imageRead ( testFile );
							expect( isImage( img ) ).toBeTrue( "Can read an image with the format [#imageFormat#]" );
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