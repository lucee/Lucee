component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-398", function() {
			it(title="checking SerializeJSON() with extended object", body = function( currentSpec ) {
				uri=createURI("LDEV0398/test.cfm");
				result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"childProp":"200","parentprop":"100"}');
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
