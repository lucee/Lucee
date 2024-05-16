component extends="org.lucee.cfml.test.LuceeTestCase" skip=true  {
	function beforeAll() {
		variables.uri = createURI("LDEV4642");
		variables.cfc = getDirectoryFromPath( getCurrentTemplatepath() ) & "LDEV4642/test4642.cfc";
		variables.stashSrc= fileRead( variables.cfc );
	}

	function afterAll() {
		fileWrite( variables.cfc, variables.stashSrc );
	}

	function run( testResults , testBox ) {
		describe( title = "checking recompile after an error", body = function() {
			it( title="check recompile", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#uri#/LDEV4642.cfm",
					url: {
						scene: "good"
					}
				);
				expect(result.fileContent.trim()).toBe("lucee");

				// call the cfc, write out a bad one, try it, ignore error, try again
				var result = _InternalRequest(
					template : "#uri#/LDEV4642.cfm",
					url: {
						scene: "bad"
					}
				);
				expect(result.fileContent.trim()).notToBe("lucee");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test-once/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
