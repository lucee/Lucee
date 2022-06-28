component extends="org.lucee.cfml.test.LuceeTestCase" labels="cache,ehCache" {
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-549", function() {
			it(title="checking EHCache with  the name 'default' ", body = function( currentSpec ) {
				var uri=createURI("LDEV0549/App1/test.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("AppA549");
			});

			it(title="checking RamCache with  the name 'default' ", body = function( currentSpec ) {
				var uri=createURI("LDEV0549/App2/test.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("AppB549");
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
