component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql,qoq" {
	// skip closure
	function isNotSupported() {
		var mySql = getCredentials();
		if(isEmpty(mysql)){
			return true;
		} else{
			return false;
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1508", body=function() {
			it( title='Checking QoQ with local scope',skip=isNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV1508");
				var result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('lucee');
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