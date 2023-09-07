component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-4685", function() {
			it( title="Checking tag-island in condition loop", body=function() {
				try {
					var result = _InternalRequest(
						template : "#createURI("LDEV4685")#/LDEV4685.cfm"
					).filecontent;
				}
				catch(any e) {
					var result = e.message;
				}
				expect( trim(result) ).toBe("2");
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI& calledName;
	}
}