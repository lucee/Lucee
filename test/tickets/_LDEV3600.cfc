component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
        variables.uri = createURI("LDEV3600");
    }
    function run( testResults, testBox ){
        describe(title="Testcase for LDEV-3600", body=function( currentSpec ) {
            it(title="Checking 'this' scope from rootApplication are available in subApplication", body=function( currentSpec )  {
                local.result = _InternalRequest(
                    template : "#uri#/site1/test.cfm"
                );
                expect(trim(result.filecontent)).toBe("this scope was available");
            });
        });
    }
    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}