component extends = "org.lucee.cfml.test.LuceeTestCase" labels="s3" {

	function beforeAll(){
		variables.uri = createURI("LDEV4407");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV4407", function(){
			it(title = "only define S3.ACL in Application.cfc", skip=isNotSupported() body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\index.cfm",
				)
				expect(result.filecontent).tobe("true");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	
	private struct function isNotSupported() {
		res= server.getTestService("s3");
		return isNull(res) || len(res)==0;
	}
}