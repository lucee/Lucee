component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-1946", function() {
			it(title = "checking onMissingMethod() & onError() ", body = function( currentSpec ) {
				result = _internalRequest(template=createURI("LDEV1946/missing.cfm"));
				expect(result.filecontent).toBe('Missing');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}