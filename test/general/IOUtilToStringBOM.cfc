component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.testDir = getTempDirectory() & "ioutil-bom-test/";
		if ( directoryExists( testDir ) ) directoryDelete( testDir, true );
		directoryCreate( testDir );
	}

	function run( testResults, testBox ) {

		describe( "fileRead with BOM detection", function() {

			it( "reads plain UTF-8 without BOM", function() {
				var f = testDir & "plain-utf8.txt";
				fileWrite( f, "hello world", "utf-8" );
				var result = fileRead( f, "utf-8" );
				expect( result ).toBe( "hello world" );
			});

			it( "strips UTF-8 BOM (EF BB BF)", function() {
				var f = testDir & "utf8-bom.txt";
				var bom = charNew( 3 );
				bom[ 1 ] = chr( 239 ); // 0xEF
				bom[ 2 ] = chr( 187 ); // 0xBB
				bom[ 3 ] = chr( 191 ); // 0xBF
				// write raw BOM bytes + content as ISO so the BOM isn't re-encoded
				fileWrite( f, bom.toList( "" ) & "hello BOM", "iso-8859-1" );
				var result = fileRead( f, "utf-8" );
				expect( result ).toBe( "hello BOM" );
			});

			it( "reads empty file", function() {
				var f = testDir & "empty.txt";
				fileWrite( f, "" );
				var result = fileRead( f, "utf-8" );
				expect( result ).toBe( "" );
			});

			it( "reads single byte file", function() {
				var f = testDir & "single.txt";
				fileWrite( f, "A", "utf-8" );
				var result = fileRead( f, "utf-8" );
				expect( result ).toBe( "A" );
			});

			it( "does not strip BOM-like bytes mid-content", function() {
				var f = testDir & "mid-bom.txt";
				var content = "before" & chr( 65279 ) & "after"; // U+FEFF mid-stream
				fileWrite( f, content, "utf-8" );
				var result = fileRead( f, "utf-8" );
				expect( result ).toBe( content );
			});

		});

	}

	private function charNew( required numeric size ) {
		var arr = [];
		arrayResize( arr, arguments.size );
		return arr;
	}

}
