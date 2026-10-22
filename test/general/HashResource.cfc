component extends="org.lucee.cfml.test.LuceeTestCase" labels="hash" {

	function beforeAll() {
		variables.dir = getTempDirectory() & "hashresource-" & createUUID() & "/";
		directoryCreate( variables.dir );
		variables.Hash = createObject( "java", "lucee.commons.digest.Hash" );
	}

	function afterAll() {
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( "lucee.commons.digest.Hash, the methods that hash a resource", function() {

			it( title="known values of the content 'hello'", body=function( currentSpec ) {
				var res = resource( "hello", "hello.txt" );
				expect( variables.Hash.md5( res ) ).toBe( "5d41402abc4b2a76b9719d911017c592" );
				expect( variables.Hash.sha1( res ) ).toBe( "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d" );
				expect( variables.Hash.sha256( res ) ).toBe( "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824" );
				expect( variables.Hash.sha512( res ) ).toBe( "9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca72323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043" );
			});

			it( title="an empty file", body=function( currentSpec ) {
				var res = resource( "", "empty.txt" );
				expect( variables.Hash.md5( res ) ).toBe( "d41d8cd98f00b204e9800998ecf8427e" );
				expect( variables.Hash.sha1( res ) ).toBe( "da39a3ee5e6b4b0d3255bfef95601890afd80709" );
				expect( variables.Hash.sha256( res ) ).toBe( "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" );
			});

			it( title="a file bigger than one read buffer gives the same result as hash()", body=function( currentSpec ) {
				var content = repeatString( "0123456789abcdef", 20000 ); // 320000 bytes
				var res = resource( content, "big.txt" );
				expect( variables.Hash.md5( res ) ).toBe( lCase( hash( content, "MD5", "utf-8" ) ) );
				expect( variables.Hash.sha1( res ) ).toBe( lCase( hash( content, "SHA-1", "utf-8" ) ) );
				expect( variables.Hash.sha256( res ) ).toBe( lCase( hash( content, "SHA-256", "utf-8" ) ) );
				expect( variables.Hash.sha512( res ) ).toBe( lCase( hash( content, "SHA-512", "utf-8" ) ) );
			});

			it( title="binary content", body=function( currentSpec ) {
				var bin = binaryDecode( "000102030405FEFF80", "hex" );
				var f = variables.dir & "binary.bin";
				fileWrite( f, bin );
				var res = toResource( f );
				expect( variables.Hash.md5( res ) ).toBe( lCase( hash( bin, "MD5" ) ) );
				expect( variables.Hash.sha256( res ) ).toBe( lCase( hash( bin, "SHA-256" ) ) );
			});

			it( title="the result is lower case hex", body=function( currentSpec ) {
				var res = resource( "hello", "case.txt" );
				expect( variables.Hash.md5( res ) ).toMatch( "^[0-9a-f]{32}$" );
				expect( variables.Hash.sha1( res ) ).toMatch( "^[0-9a-f]{40}$" );
				expect( variables.Hash.sha256( res ) ).toMatch( "^[0-9a-f]{64}$" );
				expect( variables.Hash.sha512( res ) ).toMatch( "^[0-9a-f]{128}$" );
			});

			it( title="a missing file fails", body=function( currentSpec ) {
				var res = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" ).toResourceNotExisting( getPageContext(), variables.dir & "missing.txt" );
				var failed = false;
				try {
					variables.Hash.md5( res );
				}
				catch ( any e ) {
					failed = true;
				}
				expect( failed ).toBeTrue();
			});
		});

		describe( "lucee.commons.digest.Hash.md5( resource, maxlength ), the hash of the beginning of a file", function() {

			it( title="a file smaller than 100 bytes is hashed as it is", body=function( currentSpec ) {
				var res = resource( "hello", "max-small.txt" );
				expect( variables.Hash.md5( res, 1024 ) ).toBe( "5d41402abc4b2a76b9719d911017c592" );
			});

			it( title="a file bigger than the maximum is cut", body=function( currentSpec ) {
				var content = repeatString( "0123456789", 500 ); // 5000 bytes
				var res = resource( content, "max-big.txt" );
				expect( variables.Hash.md5( res, 1024 ) ).toBe( lCase( hash( left( content, 1024 ), "MD5", "utf-8" ) ) );
				// the whole file fits in the buffer, which is padded with zeros up to the maximum
				expect( variables.Hash.md5( res, 8192 ) ).toBe( lCase( hash( zeroPadded( content, 8192 ), "MD5" ) ) );
			});

			it( title="a file of 100 bytes or more that is smaller than the maximum is hashed with the whole buffer (padded with zeros), this is what the method always did", body=function( currentSpec ) {
				var content = repeatString( "x", 150 );
				var res = resource( content, "max-mid.txt" );
				expect( variables.Hash.md5( res, 1024 ) ).toBe( lCase( hash( zeroPadded( content, 1024 ), "MD5" ) ) );
			});

			it( title="an empty file fails", body=function( currentSpec ) {
				var res = resource( "", "max-empty.txt" );
				var failed = false;
				try {
					variables.Hash.md5( res, 1024 );
				}
				catch ( any e ) {
					failed = true;
				}
				expect( failed ).toBeTrue();
			});
		});
	}

	private binary function zeroPadded( required string content, required numeric size ) {
		var hex = binaryEncode( charsetDecode( arguments.content, "utf-8" ), "hex" );
		return binaryDecode( hex & repeatString( "00", arguments.size - len( arguments.content ) ), "hex" );
	}

	private any function resource( required string content, required string name ) {
		var f = variables.dir & arguments.name;
		fileWrite( f, arguments.content );
		return toResource( f );
	}

	private any function toResource( required string path ) {
		return createObject( "java", "lucee.commons.io.res.util.ResourceUtil" ).toResourceExisting( getPageContext(), arguments.path );
	}
}
