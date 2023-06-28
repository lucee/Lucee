component extends="org.lucee.cfml.test.LuceeTestCase" labels="booleanFormat" skip=true {
	function beforeAll() {
		variables.uri = createURI("LDEV4589");
	}

	function run( testResults , testBox ) {
		describe( title = "Testcase for booleanFormat() function", body = function() {
			it( title="Checking booleanFormat() with numeric value", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#uri#\LDEV4589.cfm",
					forms : {Scene = 1}
				);
				expect(result.fileContent.trim()).toBe("true");
			});
			it( title="Checking booleanFormat() with string value", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#uri#\LDEV4589.cfm",
					forms : {Scene = 2}
				);
				expect(result.fileContent.trim()).toBe("false");
			});
			it( title="Checking booleanFormat() with string(numeric) value", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#uri#\LDEV4589.cfm",
					forms : {Scene = 3}
				);
				expect(result.fileContent.trim()).toBe("true");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
