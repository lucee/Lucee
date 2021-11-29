component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
    function beforeAll(){
        variables.uri = createURI("LDEV3768");
    }
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3768", function() {
            it( title="ORMExecuteQuery(), HQL with correct column name to declare param", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 1 }
                );
                expect(trim(result.filecontent)).toBe("true");
            });
            it( title="ORMExecuteQuery(), HQL with wrong case column name without param", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 2 }
                );
                expect(trim(result.filecontent)).toBe("true");
            });
            it( title="ORMExecuteQuery(), HQL with wrong case column name to declare param", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 3 }
                );
                expect(trim(result.filecontent)).toBe("true");
            });
        });
    }
    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}