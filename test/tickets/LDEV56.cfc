component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV56");
	}
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-56:CFCATCH does not support NAME attribute", function() {
			it(title = "Checking with script format", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=1}
				);
				expect(local.result.filecontent.trim()).toBe('Testcase Passes');
			});

			it(title = "Checking with tag basis", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=2}
				);
				
				expect(local.result.filecontent.trim()).toBe('Testcase Passes');
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}