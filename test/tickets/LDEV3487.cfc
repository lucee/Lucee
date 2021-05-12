component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
        variables.uri = createURI("LDEV3487");
    }
    function run( testResults, testBox ) {
        var skip = structIsEmpty(server.getDatasource("mysql"));
        describe("Testcase for LDEV-3487", function() {
            it( title="Check generatedKey in insert operation for MySQL", skip=skip, body=function( currentSpec ){
                try{
                    local.result = _InternalRequest(
                        template : "#uri#\test.cfm"
                    );
                }
                catch(any e){
                    result.filecontent = e.message;
                }
                expect(trim(result.filecontent)).toBe("true,1");
            });
        });
    }
    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}