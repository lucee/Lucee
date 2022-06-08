component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" skip=true {

	function beforeAll() {
		variables.uri = createURI("LDEV4001");
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV4001", function() {
			it( title="java code in lucee using cfjava tag", body=function( currentSpec ) {
				try {
					var res = _internalRequest(
						template:"#variables.uri#/LDEV4001.cfm",
						forms:{scene:1}
					).fileContent.trim();
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toBe("Lucee");
			});
			it( title="java code in lucee using java block", body=function( currentSpec ) {
				try {
					var res = _internalRequest(
						template:"#variables.uri#/LDEV4001.cfm",
						forms:{scene:2}
					).fileContent.trim();
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toBe("java block worked in Lucee");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}