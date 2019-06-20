component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV2298");
	}

	function run( testResults, testBox ) {
		describe( "Test case for LDEV2298", function(){
			it( title="queryExecute() param withStruct", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene=1}
				);
				expect(result.filecontent).tobe("{ts '1900-01-01 00:00:00'}");
			}); 	

			it( title="queryExecute() param with Array of Struct", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene=2}
				);
				expect(result.filecontent).tobe("{ts '1900-01-01 00:00:00'}");
			}); 
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}