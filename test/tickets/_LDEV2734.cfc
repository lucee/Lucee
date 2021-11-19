component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run( testResults, textbox ) {
        describe("testcase for LDEV-2734", function(){
            it(title="Tag island inside script-based try/catch", body=function( currentSpec ){
                try {
                    ```
                        <cfset var res = "Tag island worked">
                    ```
                }
                catch (any e) {
                    res = e.message;
                }
                expect(res).toBe("Tag island worked");
            });
            it(title="Tag island inside tag-based try/catch", body=function( currentSpec ){
                try {
                    local.result = _internalRequest(
                        template = createURI("LDEV2734") & "/LDEV2734.cfm"
                    ).fileContent.trim();
                }
                catch (any e) {
                    result = e.message;
                }
                expect(result).toBe("Tag island worked");
            });
        });
    }

    private string function createURI(string calledName){
        var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}