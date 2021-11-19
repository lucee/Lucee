component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		variables.uri = createURI("LDEV2524");
	}
	function run( testResults, testBox ){
		describe( "test suite for LDEV2524", function(){
			it( title = "DeserializeJSON doesn't handle UPPERCASE letters", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					form : { scene = '1' }
				);
				expect(trim(result.filecontent)).tobe("11");
			});
			it( title = "DeserializeJSON does handle LOWERCASE letters", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#/test.cfm",
					form : { scene = '2' }
				);
				expect(trim(result.filecontent)).tobe("11");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}