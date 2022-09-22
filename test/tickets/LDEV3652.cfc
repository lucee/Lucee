component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-3652", function() {
			it(title="checking SerializeJSON() with custom getter and remotingFetch", body = function( currentSpec ) {
				var uri=createURI("LDEV3652/test.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"number2":20,"sum":60}');
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
