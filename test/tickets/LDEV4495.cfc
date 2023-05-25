component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.uri = createURI("LDEV4495");
	}

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-4495", function() {
			it( title="Checking structFind() with callback", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 1}
				);
				expect(result.fileContent.trim()).toBe("meow");
			});

			it( title="Checking struct.find() with callback", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 2}
				);
				expect(result.fileContent.trim()).toBe("meow");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}