component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql,orm" {
	// skip closure
	function isNotSupported() {
		var mySql = getCredentials();
		if(!isNull(mysql) && structCount(mySql)){
			return false;
		} else{
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1793", body=function() {
			it( title='Checking EntityLoadByPK() with ID value as Binary format',skip=isNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV1793");
				var result = _InternalRequest(
					template:"#uri#/index.cfm"
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
		return server.getDatasource("mysql");
	}
}