component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function beforeAll() {
		variables.uri = createURI("LDEV3359");
	}

	function run( testresults , testbox ) {
		describe( "Testcase for LDEV-3359", function () {
			it( title="checking getBaseTagList() inside CFModule execution", body = function ( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV3359.cfm"
				).filecontent;
			
				expect(trim(result)).toBe("CF_INNER");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}