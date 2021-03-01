component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2979");
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2979", function() {
			it(title = "query param not working without CFSQLTYPE for date type values", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2979.cfm"
				);
				expect("Invalid column name 'item_idtest'.").toBe(trim(result.filecontent));
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}