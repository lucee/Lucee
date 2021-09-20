component extends = "org.lucee.cfml.test.LuceeTestCase"{

    function run ( testResults , testBox ) {
        describe("Testcase for LDEV-3617",function(){
            it(title="_internalRequest() forms, urls argument string as input with duplicate parameters", body =function( currentSpec ){
                res = _internalRequest(
                    template = createURI("LDEV3617/LDEV3617.cfm"),
                    urls = "a=1&a=2&a=3",
                    forms = "b=1&b=2&b=3"
                );
                expect(listFirst(res.filecontent)).toBe('{"a":"1,2,3"}');
                expect(listlast(res.filecontent)).toBe('{"b":"1,2,3"}');
            });
        });
    }

    private string function createURI(string calledName){
        var baseURI = "test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}