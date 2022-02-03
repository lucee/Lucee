component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3860", function() {
            it( title="Checking the exception occured in transaction with ORM", body=function( currentSpec ) {
                    local.result = _InternalRequest(
                        template : "#createURI("LDEV3860")#\test.cfm"
                    );
                expect(trim(result.filecontent)).toBe(true);
            });
        });
    }
    
    private string function createURI(string calledName) {
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}