component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-486", function() {
			it(title="private function shouldn't access through 'this' scope", body = function( currentSpec ) {
				uri=createURI("LDEV0486/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				uri = createURI("LDEV0486/test.cfc");
				path = expandpath(uri);
				expect(result.filecontent.trim()).toBe("The method testMethod was not found in component #path#");
			});

			it(title="private function access without any scope", body = function( currentSpec ) {
				uri=createURI("LDEV0486/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				expect(result.filecontent.trim()).toBe("true");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
