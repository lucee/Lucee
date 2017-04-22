component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1266", function() {
			it( title="checking createObject(), with containing jar directory", body=function( currentSpec ) {
				var uri = createURI("LDEV1266/index.cfm");
				var result = _InternalRequest(
						template:uri,
					forms:{Scene:1}
				);
				expect(result.fileContent.trim()).toBe("true");
			});

			it( title="checking createObject(), with single Jar jar", body=function( currentSpec ) {
				var uri = createURI("LDEV1266/index.cfm");
				var result = _InternalRequest(
						template:uri,
					forms:{Scene:2}
				);
				expect(result.fileContent.trim()).toBe("true");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
