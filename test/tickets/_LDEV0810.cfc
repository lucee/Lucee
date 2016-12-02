component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-810", function() {
			it(title="checking reEscape function in lucee", body = function( currentSpec ) {
				uri=createURI("LDEV0810/test.cfm");
				result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("lucee\?\[\]\^");
			});
		});
	}
	// private function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
