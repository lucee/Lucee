component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1819");
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1819", body=function() {
			it(title = "Checking with braces in multi line ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test1.cfm"
				);
				expect(local.result.fileContent).toBe('Condition_True');
			});
			it(title = "Checking without braces in single line", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test2.cfm"
				);
				expect(local.result.fileContent).toBe('Condition_True');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 