component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-4156", function() {
			it( title="tag-island after the thread", body=function() {
				try {
					var result = _InternalRequest(
						template : "#createURI("LDEV4157")#/LDEV4157.cfm"
					).filecontent;
				}
				catch(any e) {
					var result = e.message;
				}
				expect( trim(result) ).toBe("thread and tag-island after the thread statement works");
			});

			it( title="tag-island after the thread in cfc", body=function() {
				try {
					var result = new LDEV4157.test4157().foo();
				}
				catch(any e) {
					var result = e.message;
				}
				expect( trim(result) ).toBe("thread and tag-island after the thread statement in cfc works");
			})
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI& calledName;
	}
}