component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		variables.uri = createURI("LDEV2616");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2581", function() {
			it(title = " value empty throws an error for queryexecute function", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : uri&"/LDEV2616.cfm",
					forms : {scene = 1}
				);
				expect(result.filecontent).toBe(0);
			});

			it(title = "Value empty execute in query member function query.queryexecute", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : uri&"/LDEV2616.cfm",
					forms : {scene = 2}
				);
				expect(result.filecontent).toBe(0);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}