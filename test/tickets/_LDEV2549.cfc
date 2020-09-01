component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2549");
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2549", function() {
			it(title = "query param not working without CFSQLTYPE for date type values", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2549.cfm",
					forms:	{scene=1}
				);
				expect(1).toBe(result.filecontent);
			});

			it(title = "query param works fine with CFSQLTYPE for date type values", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2549.cfm",
					forms:	{scene=2}
				);
				expect(2).toBe(local.result.filecontent);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}