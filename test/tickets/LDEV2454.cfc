component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {

	// this needs to be manually run via a browser, internalRequest doesn't support file uploads (yet!!!)
	
	function beforeAll(){
		variables.uri = createURI("LDEV2454");
	}
	
	function run( testResults , testBox ) {

		describe( "test suite for LDEV2454", function() {
			it(title = "this.blockedExtForFileUpload should support *", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#pathOne#"&"\index.cfm"
				);
				expect(trim(result.fileContent)).notToBe("success");
			});
		});
	}
	
	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}

}