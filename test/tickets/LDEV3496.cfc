component extends="org.lucee.cfml.test.LuceeTestCase"{
    function beforeAll(){
        variables.uri = createURI("LDEV3496");
    }
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3496", function() {
            it( title="safe-navigation operator with variables scope", body=function( currentSpec ){
                try {
                    loop key = "local.key" value = "local.value" struct = structNew() {
                            if ( variables?.value ) { }
                    }
                    res = "itsCompiled";
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe("itsCompiled");
            });
            it( title="safe-navigation operator with local scope", body=function( currentSpec ){
                try{
                    local.result = _InternalRequest(
                        template : "#uri#\test.cfm"
                    );
                }
                catch(any e){
                    result.filecontent = e.message;
                }
                expect(trim(result.filecontent)).toBe("itsCompiled");
            });
        });
    }
    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}