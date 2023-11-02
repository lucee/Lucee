component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2810");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2810", function() {
			it(title = "URL variables can be automatically parsed into a struct", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV2810.cfm"
				);
				expect(trim(result.filecontent)).toBe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}