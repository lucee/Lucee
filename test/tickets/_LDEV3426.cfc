component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
        variables.uri = createURI("LDEV3426");
    }
    function run( testResults, testBox ){
        describe("Testcase for LDEV-3426", function() {
            it( title="Create table using foreign Key without ON DELETE action and result attribute", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 1, onDel = false }
                );
                expect(trim(result.filecontent)).toBe("Success");
            });
            it( title="Create table using foreign Key without ON DELETE action and with result attribute", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 2, onDel = false }
                );
                expect(trim(result.filecontent)).toBe("Success");
            });
            it( title="Create table using foreign Key with ON DELETE action and without result attribute", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 1, onDel = true }
                );
                expect(trim(result.filecontent)).toBe("Success");
            });
            it( title="Create table using foreign Key with ON DELETE action and result attribute", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 2, onDel = true }
                );
                expect(trim(result.filecontent)).toBe("Success");
            });
        });
    }
    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}
