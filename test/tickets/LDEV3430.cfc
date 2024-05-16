component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
        variables.uri = createURI("LDEV3430");
    }
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3430", function() {
            it( title="ORMExecuteQuery() with positional argument(hql)", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 1 }
                );
                expect(trim(result.filecontent)).toBeTrue();
            });
            it( title="ORMExecuteQuery() with positional arguments(hql, params, unique, queryOptions)", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 2 }
                );
                expect(trim(result.filecontent)).toBeTrue();
            });
            it( title="ORMExecuteQuery() with named arguments(hql, params, unique, queryOptions)", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 3 }
                );
                expect(trim(result.filecontent)).toBeTrue();
            });
            it( title="ORMExecuteQuery() with named argument(hql)", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 4 }
                );
                expect(trim(result.filecontent)).toBeTrue();
            });
            it( title="ORMExecuteQuery() with named arguments(hql, params)", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 5 }
                );
                expect(trim(result.filecontent)).toBeTrue();
            });
            it( title="ORMExecuteQuery() with named arguments(hql, params, unique)", body=function( currentSpec ){
                local.result = _InternalRequest(
                    template : "#uri#\test.cfm",
                    forms : { Scene = 6 }
                );
                expect(trim(result.filecontent)).toBeTrue();
            });
        });
    }
    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}