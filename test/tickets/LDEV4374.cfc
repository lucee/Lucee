component extends = "org.lucee.cfml.test.LuceeTestCase" labels="string" {

    function beforeAll() {
        variables.uri = createURI("LDEV4374");
        variables.mystring = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    }

    function run( testResults, textbox ) {
        describe("Testcase for LDEV-4374", function() {
            it(title="Extract the String using array notation with range", body=function( currentSpec ) {
                try {
                    var res = _internalRequest(
                        template: "#variables.uri#/LDEV4374.cfm",
                        forms: { scene : 1 }
                    ).filecontent.trim();
                }
                catch(any e) {
                    var res = e.message;
                }
                expect(res).toBe("DEFGHIJKLM");
            });
            it(title="Extract the String using array notation with range and step", body=function( currentSpec ) {
                try {
                    var res = _internalRequest(
                        template: "#variables.uri#/LDEV4374.cfm",
                        forms: { scene : 2 }
                    ).filecontent.trim();
                }
                catch(any e) {
                    var res = e.message;
                }
                expect(res).toBe("DFHJL");
            });
            it(title="Extract the String using array notation with negative index range", body=function( currentSpec ) {
                try {
                    var res = _internalRequest(
                        template: "#variables.uri#/LDEV4374.cfm",
                        forms: { scene :3 }
                    ).filecontent.trim();
                }
                catch(any e) {
                    var res = e.message;
                }
                expect(res).toBe("QRSTUVW");
            });
            it(title="Extract the String using array notation with negative index range and step", body=function( currentSpec ) {
                try {
                    var res = _internalRequest(
                        template: "#variables.uri#/LDEV4374.cfm",
                        forms: { scene : 4 }
                    ).filecontent.trim();
                }
                catch(any e) {
                    var res = e.message;
                }
                expect(res).toBe("QSUW");
            });
        });
    }

    private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}