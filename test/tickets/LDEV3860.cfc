component extends="org.lucee.cfml.test.LuceeTestCase" {
    function beforeAll(){
        variables.uri = createURI("LDEV3860");
    }
    
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3859 & LDEV-3860", function() {
            it( title="LDEV-3860 -- Checking the exception occured in transaction with ORM",  skip=true, body=function( currentSpec ) {
                    local.result = _InternalRequest(
                        template : "#uri#\LDEV3860.cfm"
                    );
                expect(trim(result.filecontent)).toBe(true);
            });
            it( title="LDEV-3859 -- Checking the multiple transactions with ORM and query", skip=true,body=function( currentSpec ) {
                    local.result = _InternalRequest(
                        template : "#uri#\LDEV3859.cfm"
                    );
                expect(trim(result.filecontent)).toBe(true);
            });
        });
    }

    private boolean function notHasDatasource(){
        return !structCount(server.getDatasource("mssql"));
    } 

    private string function createURI(string calledName) {
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}