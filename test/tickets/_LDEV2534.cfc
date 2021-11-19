component extends = "org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2534");
	}
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2534", function() {
			it(title = "Multiple function with same name - lucee doesn't throw an error", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm"
				);
				expect(trim(result.filecontent)).toBe("Lucee shouldn't call more function with same name");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI& calledName;
	}
}