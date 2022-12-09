component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
        variables.uri = createURI("LDEV3549");
    }
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3549", function() {
            it( title="Checks the jar file locking with this.javaSettings.reloadOnChange=true", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    url: {
                        tempJarFolder: server._getTempDir( "LDEV3549" )
                    }
                );
                expect(trim(result.filecontent)).toBe("jar file wasn't locked");
            });
        });
    }
    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}