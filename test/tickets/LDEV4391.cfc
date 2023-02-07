component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV4391");
	}

	function run( testResults, textbox ) {
		describe("Testcase for LDEV-4391", function() {
			it(title="checking Rest operator for function", body=function( currentSpec ) {
				try {
					var res = _internalRequest(
						template: "#variables.uri#/LDEV4391.cfm",
						forms: { scene : 1 }
					).filecontent.trim();
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toBe("5,2,3");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}