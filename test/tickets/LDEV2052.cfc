component extends="org.lucee.cfml.test.LuceeTestCase" labels="zip"{
	function beforeAll(){
		variables.uri = createURI("LDEV2052");
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2052", function() {
			it(title = "Checking overwrite attribute in cfzip action=unzip", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe("UpdatedContent");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
