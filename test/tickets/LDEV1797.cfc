component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1797", function() {
			it( title='checking thread in member function', body=function( currentSpec ) {
				var uri = createURI("LDEV1797");
				var result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('false');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}