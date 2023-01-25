component extends = "org.lucee.cfml.test.LuceeTestCase" labels="ORM" {

    function beforeAll() {
        variables.uri = createURI("LDEV4380");
    }

    function run( testResults, textbox ) {
        describe("Testcase for LDEV-4380", function() {
            it(title="ORMFlush() with ORM Event handler", body=function( currentSpec ) {
                var res = _internalRequest(
                    template: "#variables.uri#/test4380.cfm"
                );
                expect(res.filecontent.trim()).toBe("success");
            });
        });
    }

    private string function createURI(string calledName) {
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}