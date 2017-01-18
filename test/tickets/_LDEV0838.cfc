component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-838", function() {
			it(title="checking isSafeHTML() function", body = function( currentSpec ) {
				uri=createURI("LDEV0838/isSafeHTML.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				expect(result.filecontent.trim()).toBe("YES");
			});

			it(title="checking isSafeHTML() function, with attribute PolicyFile", body = function( currentSpec ) {
				uri=createURI("LDEV0838/isSafeHTML.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				expect(result.filecontent.trim()).toBe("No");
			});

			it(title="checking getSafeHTML() function", body = function( currentSpec ) {
				uri=createURI("LDEV0838/getSafeHTML.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				expect(result).toBe("Lucee Starts Works on getSafeHTML()");
			});

			it(title="checking getSafeHTML() function, with attribute PolicyFile", body = function( currentSpec ) {
				uri=createURI("LDEV0838/getSafeHTML.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				expect(result).toBe("");
			});
		});
	}
	// private function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
