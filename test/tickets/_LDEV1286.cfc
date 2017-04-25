component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1286");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1286", function() {
			it( title='Checking cfparam it raises "missing required parameter error" in tag based', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=1}
				);
				expect(local.result.filecontent.trim()).toBe("The required parameter [address1] was not provided.");
			});

			it( title='Checking cfparam it raises "missing required parameter error" in script based', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=2}
				);
				expect(local.result.filecontent.trim()).toBe("The required parameter [address1] was not provided.");
			});

			it( title='Checking cfparam it sets boolean value instead of raising "missing required parameter error"', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=3}
				);
				expect(local.result.filecontent.trim()).toBe("The required parameter [address1] was not provided.");
			});

			it( title='Checking cfparam it sets string value instead of raising "missing required parameter error"', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=4}
				);
				expect(local.result.filecontent.trim()).toBe("The required parameter [address2] was not provided.");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}