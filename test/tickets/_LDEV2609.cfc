component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2609");
	}

	function run( testResults, testBox ){
		describe("test case for LDEV2609", function(){
			it( title = "Error occurred using false == false", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : uri&"/test.cfm",
					form : { scene = 1 }
				);
				expect(result.filecontent).toBe(1);
			});
			it( title = "Error doesn't occur using both boolean and assigned values", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : uri&"/test.cfm",
					form : { scene = 2 }
				);
				expect(result.filecontent).toBe(2);
			});
			it( title = "Error doesn't occur using both boolean values only", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : uri&"/test.cfm",
					form : { scene = 3 }
				);
				expect(result.filecontent).toBe(3);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}