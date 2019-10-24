component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2534");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2534", function() {
			it(title = "multiple function in lucee doesn't throw an error", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/test.cfm"
				);
				expect(trim(result.filecontent)).toBe('Routines cannot be declared more than once.');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI& calledName;
	}
}