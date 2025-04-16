component extends="org.lucee.cfml.test.LuceeTestCase" labels="internalRequest"  {

	function beforeAll() {
		variables.uri = createURI("LDEV4199"); 	
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4199", function() {
			it( title="InternalRequest() without method argument", body=function( currentSpec ) {
				var result = _internalRequest(
					template="#variables.uri#/LDEV4199.cfm"
				);
				expect(result.filecontent.trim()).toBe("GET");
			});
			it( title="InternalRequest() with method argument", body=function( currentSpec ) {
				var result = _internalRequest(
					template = "#variables.uri#/LDEV4199.cfm",
					method="post"
				);
				expect(result.filecontent.trim()).toBe("POST");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}	
}