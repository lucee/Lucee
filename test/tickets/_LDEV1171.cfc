component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1171", function() {
			it(title="checking cfform tag with attribute 'preservedata' ", body = function( currentSpec ) {
				var uri=createURI("LDEV1171/test.cfm");
				local.result = _InternalRequest(
					template:uri
				);
				expect(local.result.filecontent.trim()).toBe();
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}