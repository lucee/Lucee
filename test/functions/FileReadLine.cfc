component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	public void function testFileReadLine () {
		var f = getTempFile( getTempDirectory(), "fileWriteLine-object", "txt" );

		var handle = fileOpen( f, "append" ); 
		fileWriteLine( handle, "a" ); 
		fileWriteLine( handle, "b" );
		fileWriteLine( handle, "c" );
		expect( fileRead( f ) ).toBe( "" );  // file wasn't closed
		fileClose( handle );

		var handle = fileOpen( f, "read" );
		var arr = [];
		while( !fileIsEoF( handle ) ) {
			arrayAppend( arr, fileReadLine( handle ) );
		}
		fileClose( handle );

		var idx = 1;
		expect( asc( arr[ idx ] ) ).toBe( 97 ); // a
		idx ++;
		expect( asc( arr[ idx ] ) ).toBe( 98 ); // b
		idx++;
		expect( asc( arr[ idx ] ) ).toBe( 99 ); // c 

		fileDelete( f );
	}

}
