component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1603", body=function() {
			it( title='Checking cflock in script based with variable scope',body=function( currentSpec ) {
				lock type="exclusive" timeout="5" name="test" result="local.res"
				{ WriteOutput("got the lock<br />"); }
				expect(IsDefined("cflock")).toBe(false);
				expect(IsDefined("local.res")).toBe(true);
			});

		});
	}

	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}