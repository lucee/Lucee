component extends="org.lucee.cfml.test.LuceeTestCase"  labels="mysql,orm" {
	function beforeAll(){
		variables.uri = createURI("LDEV0908");
	}
	// skip closure
	/*function isNotSupported() {
		var mySql = getCredentials();
		if(!isNull(mysql)){
			return false;
		} else{
			return true;
		}
	}*/

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-908", body=function() {
			it(title = "Checking ", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('');
			});
		});
	}

	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	/*private struct function getCredentials() {
		return server.getDatasource("mysql");
	}*/
}

