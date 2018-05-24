component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1856");
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1856", body=function() {
			it(title = "Checking cfhttp with charset", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.Filecontent.trim()).toBe('KÃ¤sesauce,RÃ¶stinchen,WeiÃkrautsalat');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 