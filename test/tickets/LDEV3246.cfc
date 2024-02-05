component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll() {
		variables.uri = createURI("LDEV3246");
	}

	function run( testResults,testBox ) {
		describe( 'Testcase for LDEV3246' , function() {
			it( title = 'checking default=null in cfparam' , body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/index.cfm"
				).filecontent.trim();

				expect(result).toBe("null");
			})
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()), "\/")#/";
		return baseURI&""&calledName;
	}
}
