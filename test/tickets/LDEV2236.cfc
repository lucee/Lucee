component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2236");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2236", function() {
			it(title = "cfquery with returnType Array returns NULL  with Partially NULL Support",body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=1}
				);
				expect(trim(local.result.filecontent)).toBeEmpty();
			});

			it(title = "cfquery with returnType Array returns NULL  with Partially NULL Support",body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=2}
				);
				expect(trim(local.result.filecontent)).toBeEmpty();
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}