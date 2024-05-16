component extends="org.lucee.cfml.test.LuceeTestCase" skip=true labels="orm" {
    function beforeAll(){
        variables.uri = createURI("LDEV2862");
    }
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-2862", function() {
            it( title="Duplicate() with the ORM entity which has relationship mappings", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm"
                );
                expect(trim(result.fileContent)).toBe("success");
            });
        });
    }
    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}
