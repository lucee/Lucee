component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.uri = createURI( "testDirFileInfo" );
		directoryCreate( uri );
		fileWrite( uri&"\testfile.txt", "fileInfo" )
	}	

	function afterAll() {
		directorydelete( uri, true );
	}

	function run( testResults , testBox ) {
		describe( "test case for fileInfo", function() {
			it(title = "Checking with fileInfo function", body = function( currentSpec ) {
				var info =fileInfo( uri&"\testfile.txt" );
				expect( info.read ).toBeTrue();
				expect( info.write ).toBeTrue();
				expect( info.type ).toBe( "file" );
				expect( info.name ).toBe( "testfile.txt" );
				expect( info ).toHaveKey( "dateLastModified" );
				expect( info).toHaveKey( "path" );
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ),"\/" )#/";
		return baseURI&""&calledName;
	}
}