component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1537");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1537", function() {
			it(title = "Checking mail spooler retries emails without from address", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('true');
			});
			it(title = "Checking cfmail tag with the from attribute, from address with colon", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test2.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('localhost:81@gmail.com');
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}

