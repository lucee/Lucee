component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
        variables.uri = createURI("LDEV3549");
    }
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3549", function() {
            it( title="Checks the jar file locking with this.javaSettings.reloadOnChange=true", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm"
                );
                expect(trim(result.filecontent)).toBe("jar file not get locked");
            });
        });
    }
    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}