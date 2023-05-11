component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.uri = createURI( "testDirfileSetLastModified" );
		directoryCreate( uri );
		fileWrite( uri&"\testfile.txt", "fileSetLastModified" )
	}	

	function afterAll(){
		directorydelete( uri , true );
	}

	function run( testResults , testBox ) {
		describe( title="Testcase for fileSetLastModified", body=function() {
			it(title="Checking with fileSetLastModified function", body=function( currentSpec ) {
				fileSetLastModified(uri&"\testfile.txt", dateAdd("d", 1, CreateDateTime(2023, 05, 04, 1, 1, 1)));
				expect( getFileInfo(uri&"\testfile.txt").lastmodified ).toBe("{ts '2023-05-05 01:01:01'}");
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI&""&calledName;
	}
}