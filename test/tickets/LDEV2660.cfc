component extends="org.lucee.cfml.test.LuceeTestCase" labels="zip"{
	function beforeAll(){
		variables.uri = createURI("LDEV2660");
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2660", function() {
			it(title = "Checking overwrite attribute in cfzip action=unzip", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/index.cfm"
				);
				expect(local.result.filecontent.trim()).toBe("true");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
