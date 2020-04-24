component extends = "org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2646");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV2646", function(){
			it( title = "checking lock with comment line" , body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : { scene = 1}
				);
				expect(trim(result.filecontent)).toBe(true);
			});
			
			it( title = "checking lock without comment line" , body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : { scene = 2 }
				);
				expect(trim(result.filecontent)).toBe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}