component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		variables.uri = createURI("LDEV3614");
		if(!directoryExists(uri)) {
			directorycreate(uri);
		}
	}

	function testDirectoryListWithAccents( ) {
		FileWrite(uri & "/fileSimpleName.txt","");
        FileWrite(uri & "/fileWithAccent_é.txt","");
        DirectoryCreate(uri & "/dirSimpleName");
        DirectoryCreate(uri & "/dirWithAccent_à");

        var all = DirectoryList(uri, false, "query", "", "", "all");
        expect( all.recordcount ).toBe(4);
        var dir = DirectoryList(uri, false, "query", "", "", "dir");
        expect( dir.recordcount ).toBe(2);
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}

	function afterAll() {
		if(directoryExists(uri)) {
			directorydelete(uri,true);
		}
	}
}