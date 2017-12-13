component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1619");
	}
	function run( testResults , testBox ) {
		describe( "Test cases for LDEV-1619", function() {
			it(title = "Checking URL Parameter", body = function( currentSpec ) {
				local.res = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					url : {a:'',a:"1"}
				);
				expect(local.res.filecontent).toBe(",1");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
