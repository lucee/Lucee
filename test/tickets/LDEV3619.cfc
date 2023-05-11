component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run ( testResults , testBox ) {
        describe("Testcase for LDEV-3619", function(){
            it(title="Single space between param and name with dot notation", body=function( currentSpec ){
                param cfml.engine = "Lucee";
                expect(cfml.engine).toBe("Lucee");
            });
            it(title="Multiple spaces between param and name", body=function( currentSpec ){
                param  engine = "Lucee";
                expect(engine).toBe("Lucee");
            });
            it(title="Multiple spaces between param and name with dot notation", body=function( currentSpec ){
                try {
                    local.result = _internalRequest(
                        template : "#createURI("LDEV3619")#/test.cfm"
                    );
                }
                catch(any e) {
                    result.filecontent = e.message;
                }
                expect(trim(result.filecontent)).toBe("Lucee");
            });
        });
    }

    private string function createURI(string calledName){
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}