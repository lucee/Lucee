component extends="org.lucee.cfml.test.LuceeTestCase" labels="javacast" {

	function run( testResults, testBox ) {
		describe( "LDEV-6915 javaCast byte[] accepts unsigned byte values", function() {

			it( "converts values 128 to 255 to their 8-bit representation", function() {
				var bytes = javaCast( "byte[]", [ 255, 0, 0 ] );
				expect( isBinary( bytes ) ).toBeTrue();
				expect( binaryEncode( bytes, "hex" ) ).toBe( "FF0000" );
			});

			it( "covers the whole signed and unsigned byte range", function() {
				var bytes = javaCast( "byte[]", [ -128, -1, 0, 127, 128, 200, 255 ] );
				expect( binaryEncode( bytes, "hex" ) ).toBe( "80FF007F80C8FF" );
			});

			it( "accepts numeric strings", function() {
				var bytes = javaCast( "byte[]", [ "255", "128", "-128" ] );
				expect( binaryEncode( bytes, "hex" ) ).toBe( "FF8080" );
			});

			it( "still throws for values outside -128 to 255", function() {
				expect( function() { javaCast( "byte[]", [ 256 ] ); } ).toThrow();
				expect( function() { javaCast( "byte[]", [ -129 ] ); } ).toThrow();
				expect( function() { javaCast( "byte[]", [ "300" ] ); } ).toThrow();
			});

			it( "a single byte keeps the signed range check", function() {
				expect( function() { javaCast( "byte", 255 ); } ).toThrow();
				expect( javaCast( "byte", 127 ) ).toBe( 127 );
			});

		});
	}
}
