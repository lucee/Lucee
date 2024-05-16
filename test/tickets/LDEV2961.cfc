component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2961");
	}

	function run( testResults , testBox ) {
		describe( title = "Test suite for LDEV2961", body = function() {
			it(title = "passby attribute doesn't work using struct with array", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms :	{ scene = 1 }
				)
				expect(trim(result.filecontent)).toBe(3);
			});
			it(title = "passby attribute works with array", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					forms :	{ scene = 2 }
				)
				expect(trim(result.filecontent)).toBe(2);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}