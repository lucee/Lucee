component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1442");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-1442", function() {
			it(title = "Checking thread scope within thread", body = function( currentSpec ) {
				var result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('bar');
			});

		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}