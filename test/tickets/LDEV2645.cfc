component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2645");
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2645", function() {
			it(title = "component Property function does not recognize variables defined at the top of that Component", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2645.cfm"
				);
				expect(trim(result.filecontent)).toBe("Luceetest");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}