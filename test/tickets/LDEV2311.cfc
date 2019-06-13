component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV2311");
	}

	function run( testResults, testBox ) {
		describe( "Test case for LDEV2311", function(){
			it( title="Query 'LOAD DATA INFILE' doesn't work with latest MySQL extension", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm"
				);
				expect(local.result.filecontent.trim() gt 0).tobe(true);
			}); 	
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}