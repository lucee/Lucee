component extends = "org.lucee.cfml.test.LuceeTestCase" {
	
	function beforeAll(){
		variables.uri = createURI("LDEV3097");
	}

	function run( testResults , textBox ) {
		describe("Testcase for LDEV-3097", function(){

			it( title = "Check transaction rollback without exit tag", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 1, value = 'Without_Exit' }
				);
				expect(result.filecontent).toBe(1);
			});

			it(title = "Check transaction rollback with exit tag", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 2, value = 'With_Exit' }
				);
				expect(result.filecontent).toBe(2);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}