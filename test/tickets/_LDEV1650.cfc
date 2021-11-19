component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {
	// skip closure
	function isNotSupported() {
		var mySql = getCredentials();
		if(!isNull(mysql)){
			return false;
		} else{
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1650", body=function() {
			it( title='Checking psuedo-constructor in Application.cfc',skip=isNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV1650");
				var result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('1');
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