component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1560");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1560", function() {
			it(title = "Passing string value while the value is defined as numeric", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('false');
			});
			it(title = "Passing numeric value while the value is defined as numeric", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test2.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('{"alice":12,"bob":false}');
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
