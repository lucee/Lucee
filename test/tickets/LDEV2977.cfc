component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2977");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV-2977", function() {
			it( title = "udpatemapping ", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#variables.uri#/test.cfm"
				)
				expect(result.filecontent).tobe("success")
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}