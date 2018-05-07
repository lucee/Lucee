component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1840");
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-1840", function() {
			it(title = "Checking array value return as string while using array loop ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:1}
				);
				expect(local.result.filecontent.trim()).toBe("3");
			});

			it(title = "Checking list value return as string while using list loop  ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:2}
				);
				expect(local.result.filecontent.trim()).toBe("lucee");
			});

			it(title = "Checking structure value return as string while using structure loop  ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene:3}
				);
				expect(local.result.filecontent.trim()).toBe("name");
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
