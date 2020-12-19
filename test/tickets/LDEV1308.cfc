component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1308");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1308", function() {
			it( title='Checking thread function using arrayEach', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/index.cfm"
				);
				expect(local.result.filecontent.trim()).toBe("t1,t2,t3");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}