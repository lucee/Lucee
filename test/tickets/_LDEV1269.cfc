component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1269", function() {
			it( title="checking QoQ oprations", body=function( currentSpec ) {
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
