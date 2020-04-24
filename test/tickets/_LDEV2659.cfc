component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV2659");
	}

	function run ( testResults, testBox ) {
		describe( "Test case for LDEV2659" , function(){
			it( title = "cfcatch variable name doesn't work with TAG based", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 1 }
				)
				expect(trim(result.filecontent)).toBe("variable [TEST] doesn't exist");
			});
			it( title = "cfcatch variable name worked successfully with SCRIPT based", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 2 }
				)
				expect(trim(result.filecontent)).toBe("variable [TEST] doesn't exist");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}