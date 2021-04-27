component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1671");
	}
	function isNotSupported() {
		var mySql = getCredentials();
		if(!isNull(mysql)){
			return false;
		} else{
			return true;
		}
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1671", function() {
			it(title = "Checking value attribute of cfqueryparam is blank", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe("true");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}

