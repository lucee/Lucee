component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("pgSQL");
	}

	function run( testResults, testBox ) {
		describe( "Test case for postgreSQL", function(){
			it( title="Insert two data - returns two values instead of one value", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 1 }
				);
				expect(trim(result.filecontent)).toBe("1");
			});
			it( title="Insert three data - returns three values instead of one value", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 2 }
				);
				expect(trim(result.filecontent)).toBe("1");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}