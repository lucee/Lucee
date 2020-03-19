component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		variables.uri = createURI("LDEV2732");
	}
	
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2732", function() {
			it( title = "cffunction with roles attribute without logging user", body = function( currentSpec ) {
				uri = createURI("LDEV2732/LDEV2732.cfm");
				local.result = _InternalRequest(
					template : uri,
					forms : {Scene = 1}
				);
				expect(result.filecontent).toBe("The current user is not authorized to invoke this method.");
			});

			it( title = "cffunction with roles attribute & logging user", body = function( currentSpec ) {
				uri = createURI("LDEV2732/LDEV2732.cfm");
				local.result = _InternalRequest(
					template : uri,
					forms : {Scene = 2}
				);
				expect(result.filecontent).toBe(1);
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}
