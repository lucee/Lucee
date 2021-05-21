component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
        variables.uri = createURI( "LDEV3270" );
        directoryCreate(uri);
        fileWrite(uri&"\test.txt", "this is content")
    }	
    
	function afterAll(){
		directorydelete(uri,true);
    }
    
	function run( testResults , testBox ) {
		describe( "test case for getFileInfo", function() {
			it(title = "Checking with getFileInfo function", body = function( currentSpec ) {
                info = getFileInfo(uri&"\test.txt");
                writeDump(info);
				expect(true).toBe(info.canRead);
				expect(true).toBe(info.canWrite);
				expect(false).toBe(info.isHidden);
				expect(info.type).toBe("file");
				expect(info.name).toBe("test.txt");
				expect(info.canRead).toBe(true);
			});
		});
    }
    
    private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}