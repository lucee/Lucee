component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1812", function() {
			it( title='checking covariance implements', body=function( currentSpec ) {
				var uri = createURI("LDEV1812");
				var result = _InternalRequest(
					template:"#uri#/index.cfm"
				);
				expect(result.filecontent.trim()).toBe('true');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}