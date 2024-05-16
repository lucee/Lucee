component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2826");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2826", function() {
			it(title = "Seems wrong in returns property", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm"
				);
				expect(trim(result.filecontent)).toBe("property_name-test");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}