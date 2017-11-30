component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1518");
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-1518", function() {
			it(title = "Checking cfsilent in tag based", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe("test");
			});
			it(title = "Checking cfsilent in script based", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test2.cfm"
				);
				expect(local.result.filecontent.trim()).toBe("test");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
