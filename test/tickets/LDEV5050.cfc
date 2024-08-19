component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll() {
		variables.uri = createURI("LDEV5050");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-5050", function() {
			
			it (title = "Empty list value with query.queryexecute should throw", body = function( currentSpec ) {
				var result = "";
				try {
					result = _InternalRequest(
						template : uri&"/LDEV5050.cfm"
					);
					result = result.fileContent;
				} catch ( e ) {
					result = e.message;
				}
				expect(result).toInclude("param [id] may not be empty" );
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}