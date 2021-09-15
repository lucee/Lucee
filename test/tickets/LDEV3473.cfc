component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
    function beforeAll(){
        variables.uri = createURI("LDEV3473");
    }
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3473", function() {
            it( title="CFC instance in externally stored session scope with mixins", body=function( currentSpec ){
                var res = "";
                loop from=1 to=2 index="i" {
                    local.result = _InternalRequest(
                        template : "#uri#\test.cfm",
                        forms = {scene:"#i#"},
                        addToken = true
                    );
                    writeDump(result);
                    res = listAppend(res, trim(result.fileContent));
                }
                expect(trim(res)).toBe("from mixin method,from mixin method");
            });
        });
    }
    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}