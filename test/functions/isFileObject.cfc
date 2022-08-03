component extends = "org.lucee.cfml.test.LuceeTestCase" {
    
    function beforeAll(){
        variables.uri = createURI("FileObject");
        Variables.path = "#uri#/test.txt";
        if (!directoryExists(uri)) directoryCreate(uri);
        filewrite(path, "fileContent to be write");
        variables.fileObj = fileOpen(path);
    }
    
    function afterAll(){
        if (directoryExists(uri)) directoryDelete(uri, true);
    }

    function run( testResults, testBox ) {
        describe( "Test case for isFileObject", function(){
            it( title="test with isFileObject", body=function( currentSpec ) {
                expect(isFileObject(fileObj)).toBeTrue();
                expect(isFileObject(path)).toBeFalse();
            });
            it( title="test with isvalid fileObject", body=function( currentSpec ) {
                expect(isValid("fileObject", fileObj)).toBeTrue();
                expect(isValid("fileObject", path)).toBeFalse();
            });
        });
    }

    private string function createURI(string calledName){
        var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
} 
