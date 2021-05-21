component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
		variables.uri = createURI( "LDEV3294" );
		directoryCreate(uri);
	}	

	function afterAll(){
		directorydelete(uri,true);
	}

	function run( testResults , testBox ) {
		describe( "test case for directoryinfo", function() {
			it(title = "Checking with directoryinfo function", body = function( currentSpec ) {
				info = directoryinfo(uri);
				expect(info.directoryName).toBe(listLast(uri,"/\"));
				expect(true).toBe(structKeyExists(info,"directoryCreated"));
				expect(true).toBe(structKeyExists(info,"dateLastModified"));
				expect(true).toBe(info.isReadable);
			});
		});
	}
	
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}