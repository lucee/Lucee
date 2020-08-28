component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		variables.uri = createURI("LDEV3022");
	}
	
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-3022", function() {
			it( title = "Checked with 'float' sql type ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : { scene = '1' }
				);
				expect(result.filecontent).toBe(11.97);
			});
			it( title = "Checked with 'decimal' sql type ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					form : { scene = '2' }
				);
				expect(result.filecontent).toBe(11.97);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}