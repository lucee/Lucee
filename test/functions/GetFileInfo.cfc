component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI( "LDEV3270" );
		directoryCreate( uri );
		fileWrite( uri&"\test.txt", "this is content" )
	}	

	function afterAll(){
		directorydelete( uri, true );
	}

	function run( testResults , testBox ) {
		describe( "test case for getFileInfo", function() {
			it(title = "Checking with getFileInfo function", body = function( currentSpec ) {
				local.info = getFileInfo( uri&"\test.txt" );
				expect( info.canRead ).toBeTrue();
				expect( info.canWrite ).toBeTrue();
				expect( info.isHidden ).toBeFalse();
				expect( info.type ).toBe( "file" );
				expect( info.name ).toBe( "test.txt" );
				expect( info.canRead ).toBeTrue();
			});
		});
	}

	private string function createURI( string calledName ){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ),"\/" )#/";
		return baseURI&""&calledName;
	}
}