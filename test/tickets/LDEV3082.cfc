component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV3082");
	}

	function run( testResults , testBox ) {
		describe( "This test case suit for LDEV-3082 ", function(){

			it( title = "image.Paste() with ordered arguments", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {scene = 1}
					);
				expect(result.filecontent).toBe("Success");
			});

			it( title = "image.Paste() with named arguments", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {scene = 2}
					);
				expect(trim(result.filecontent)).toBe("Success");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}