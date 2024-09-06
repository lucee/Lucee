component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		variables.uri = createURI("LDEV2616");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2616", function() {
			it(title = " value empty throws an error for queryexecute function", skip=msSqlUnavailable(), 
					body = function( currentSpec ) {
				var result = "";
				try {
					result = _InternalRequest(
						template : uri&"/LDEV2616.cfm"
					);
					result = result.fileContent;
				} catch ( e ) {
					result = e.message;
				}
				expect(result).toInclude("param [id] may not be empty" );
			});

		});
	}

	private boolean function msSqlUnavailable() {
		// getting the credentials from the environment variables
		return isEmpty(server.getDatasource("mssql"));
	}
	

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}