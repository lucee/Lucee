component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

	function beforeAll() {
		variables.uri = createURI("LDEV4160");
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV4160", function() {
			it( title="Chaining method on returned object from returned UDF", body=function( currentSpec ) {
				try {
					var res = _internalRequest(
						template: "#uri#/LDEV4160.cfm"
					).fileContent;
				}
				catch(any e) {
					var res = e.message;
				}
				expect(trim(res)).toBe("chaining method works");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
