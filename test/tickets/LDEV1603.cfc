component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1603", body=function() {
			it( title='Checking cflock in script based with variable scope',body=function( currentSpec ) {
				lock type="exclusive" timeout="5" name="test"
				{ WriteOutput("got the lock<br />"); }
				expect(IsDefined("cflock")).toBe(false);
			});

			it( title='Checking cflock in tag based with variable scope',body=function( currentSpec ) {
				var uri = createURI("LDEV1603");
				var result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('false');
			});
		});
	}

	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}