component extends = "org.lucee.cfml.test.LuceeTestCase" labels="array" skip="true" {

	function beforeAll() {
		variables.uri = createURI("LDEV4370");
	}

	function run( testResults, textbox ) {
		describe("Testcase for LDEV-4370", function() {
			it(title="Checking typed Array syntax with String type", body=function( currentSpec ) {
				try {
					var res = _internalRequest(
						template: "#variables.uri#/LDEV4370.cfm",
						forms: { scene : 1 }
					).filecontent.trim();
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toBe(serializeJSON(["Word1", "Word2"]));
			});
			it(title="Checking typed Array syntax with Numeric type", body=function( currentSpec ) {
				try {
					var res = _internalRequest(
						template: "#variables.uri#/LDEV4370.cfm",
						forms: { scene : 2 }
					).filecontent.trim();
				}
				catch(any e) {
					var res = e.message;
				}
				expect(res).toBe("[1,23]");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
