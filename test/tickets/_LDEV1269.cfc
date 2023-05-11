component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1269", body=function() {
			it( title="checking QoQ operations", body=function( currentSpec ) {
				var uri = createURI("LDEV1269/test.cfm");
				var result = _InternalRequest(
						template:uri
				);
				expect(result.fileContent.trim()).toBe("1");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
