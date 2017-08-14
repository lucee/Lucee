component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1375");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-1375", function() {
			it(title = "checking Cfcache with action=clientcache ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('HelloWorld');
			});

		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
