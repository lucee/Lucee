component extends="org.lucee.cfml.test.LuceeTestCase" labels="file" {

	// based on the original work by Zac Spitzer in https://github.com/lucee/Lucee/pull/2509

	function beforeAll() {
		variables.fileTag = createObject( "java", "lucee.runtime.tag.FileTag" );
		variables.fileUtil = createObject( "java", "lucee.runtime.tag.util.FileUtil" );
		variables.uri = createURI( "LDEV2454" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-2454 blocked extensions support * to block all uploads", function() {

			it( "blockedExtensions='*' blocks files with an extension", function() {
				expect( function() {
					check( "test.txt", "", "*" );
				}).toThrow();
			});

			it( "blockedExtensions='*' blocks files without an extension", function() {
				expect( function() {
					check( "test", "", "*" );
				}).toThrow();
			});

			it( "allowedExtensions wins over blockedExtensions='*'", function() {
				expect( check( "test.txt", "txt", "*" ) ).toBe( "txt" );
			});

			it( "a normal blocklist still only blocks the listed extensions", function() {
				expect( check( "test.txt", "", "exe,jsp" ) ).toBe( "txt" );
				expect( function() {
					check( "test.exe", "", "exe,jsp" );
				}).toThrow();
				expect( isNull( check( "test", "", "exe,jsp" ) ) ).toBeTrue();
			});

			it( "this.blockedExtForFileUpload='*' blocks all uploads", function() {
				expect( appCheck( "test.txt" ) ).toBe( "blocked" );
				expect( appCheck( "test" ) ).toBe( "blocked" );
			});

			it( "the * check is a value compare, not a reference compare", function() {
				// built at runtime, so not an interned literal
				var star = chr( 42 );
				expect( variables.fileUtil.isBlockAll( [ " " & star & " " ] ) ).toBeTrue();
				expect( variables.fileUtil.isBlockAll( [ "exe", "jsp" ] ) ).toBeFalse();
			});
		});
	}

	private function check( string name, string allowed, string blocked ) {
		var res = getPageContext().getConfig().getResource( getTempDirectory() & arguments.name );
		return variables.fileTag.checkExtension(
			res,
			len( arguments.allowed ) ? variables.fileUtil.toExtensionFilter( arguments.allowed ) : javaCast( "null", "" ),
			len( arguments.blocked ) ? variables.fileUtil.toExtensionFilter( arguments.blocked ) : javaCast( "null", "" ),
			getPageContext().getApplicationContext()
		);
	}

	private string function appCheck( string name ) {
		var result = _InternalRequest( template: variables.uri & "/index.cfm", url: { name: arguments.name } );
		return trim( result.fileContent );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & arguments.calledName;
	}
}
