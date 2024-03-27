component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-279", function() {
			it("checking interface component, implements with 'accessors=true' method", function( currentSpec ) {
					uri=createURI("LDEV0279/failure/test.cfm");
					result = _InternalRequest(
						template:uri
					);
				expect(result.filecontent.trim()).toBe(1);
			});

			it("checking interface component, without any method ", function( currentSpec ) {
				uri=createURI("LDEV0279/success/test.cfm");
					result = _InternalRequest(
						template:uri
					);
				expect(result.filecontent.trim()).toBe("test");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}