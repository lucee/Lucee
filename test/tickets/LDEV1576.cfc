component extends="org.lucee.cfml.test.LuceeTestCase"  labels="mysql,image" {
	// skip closure
	function isNotSupported() {
		var mySql = getCredentials();
		if( len( mysql ) gt 0 ){
			return false;
		} else{
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1576", body=function() {
			it( title='Checking cfimage binary data stored in Database',skip=isNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV1576");
				var result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('true');
			});
		});
	}
	
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private struct function getCredentials() {
		// getting the credentials from the environment variables
		return server.getDatasource("mysql");
	}
	
}