component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.base = GetTempDirectory() & "testImageFormats/";
		if( directoryExists( base ) ){
			directoryDelete (base, true );
		}
		directoryCreate( base );
		// copy the sample images  to a temp dir to detect any file locking
		directoryCopy( source="/test/artifacts/images", destination=base, recurse=true, createPath=true );

		variables.images = directoryList( base, true, "path" );
		//for (var image in images) systemOutput( image, true);
		variables.imageFormats = ArrayToStruct(array=listToArray( getWriteableImageFormats() ), valueAsKey=true);
		// for (var format in imageFormats) systemOutput( format, true );
	}

	function testImageInfo(){
		for (var imagePath in images){
			if ( !fileExists( imagePath ) ) {
				// directory!
			} else if ( structKeyExists( imageFormats, listLast( imagePath, "." ) ) ) {
				// systemOutput("ImageInfo  - #imagePath# ", true);
				expect ( function(){
					expect( imageInfo( imagePath ) ).toBeStruct( imagePath );
				}).notToThrow( message=imagePath );
			} else {
				systemOutput("Image format not supported - #imagePath# ", true);
			}
		}
	}

	function testImageRead(){
		for ( var imagePath in images ){
			if ( !fileExists( imagePath ) ) {
				// directory!
			} else if ( structKeyExists( imageFormats, listLast( imagePath, "." ) ) ) {
				// systemOutput("ImageRead - #imagePath# ", true);
				expect ( function(){
					expect( isImage( imageRead( imagePath ) ) ).toBeTrue( imagePath );
				}).notToThrow(message=imagePath );
			} else {
				systemOutput("Image format not supported - #imagePath# ", true);
			}
		}
	}

	function afterAll(){
		// make sure no files are locked
		for ( var imagePath in images ){
			if ( fileExists( imagePath ) ) 
				fileDelete( imagePath );
		}

		if( directoryExists( base ) ){
			directoryDelete( base , true );
		}
	}

}