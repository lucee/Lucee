component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	public void function testFileWriteLine() {
		var f = getTempFile( getTempDirectory(), "fileWriteLine", "txt" );
		fileWriteLine( f, "123" );
		expect( trim( fileRead( f ) ) ).toBe( "123" );
		fileWriteLine( f, "456" );
		expect( fileRead( f ) ).ToBe( "456" ); // file was overwritten 
		fileDelete( f );
	}

	public void function testFileWriteLineObject() {
		var f = getTempFile( getTempDirectory(), "fileWriteLine-object", "txt" );

		var handle = fileOpen( f, "append" ); 
		fileWriteLine( handle, "a" ); 
		fileWriteLine( handle, "b" );
		fileWriteLine( handle, "c" );
		expect( fileRead( f ) ).toBe( "" );  // file wasn't closed
		fileClose( handle );

		var ff = fileRead( f );

		var arr = []
		for ( var a =1; a < len( ff ); a++ ){
			//systemOutput( a & ") [" & ff[ a ] & "] is ascii " & asc( ff[ a ] ), true );
			arrayAppend( arr, ff[ a ] );
		}
		var idx = 1;
		expect( asc( arr[ idx ] ) ).toBe( 97 ); // a
		idx ++;
		for (var s = 1; s lte len( server.separator.line ); s++ ){
			expect( asc( arr[ idx ] ) ).toBe( asc( server.separator.line[ s ] ) );
			idx++;
		}
		expect( asc( arr[ idx ] ) ).toBe( 98 ); // b
		idx++;
		for (var s = 1; s lte len( server.separator.line ); s++ ){
			expect( asc( arr[ idx ] ) ).toBe( asc( server.separator.line[ s ] ) );
			idx++;
		}
		expect( asc( arr[ idx ] ) ).toBe( 99 ); // c 

		fileDelete( f );
	}

}
