component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

    function beforeAll() {
        variables.uri = createURI("LDEV4268");
    }

    function run( testResults , testBox ) {
        describe( "Testcase for LDEV-4268", function() {
            it( title="continue without a semicolon inside the script", body=function() {
                try {
                    var res = _internalRequest(
                        template: "#variables.uri#/LDEV4268.cfm"
                    ).filecontent.trim();
                }
                catch(any e) {
                    var res = e.message;
                }

                expect(res).toBe(9);
            });
        });
    }

    private string function createURI(string calledName) {
        var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }

}