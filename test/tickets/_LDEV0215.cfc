component extends="org.lucee.cfml.test.LuceeTestCase"{
	function isMySqlNotSupported() {
		var mySql = mySqlCredentials();
		if(!isNull(mysql)){
			return false;
		} else{
			return true;
		}
	}

	function isMsSqlNotSupported() {
		var msSql = msSqlCredentials();
		if(!isNull(mysql)){
			return false;
		} else{
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-215", body=function() {
			it( title='Checking MYSQL, INDEX for the client/session storage table',skip=isMySqlNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV0215");
				var result = _InternalRequest(
					template:"#uri#/App1/test.cfm"
				);
				expect(result.fileContent.trim()).toBe("True");
			});
			it( title='Checking MsSQL, INDEX for the client/session storage table',skip=isMsSqlNotSupported(),body=function( currentSpec ) {
				var uri = createURI("LDEV0215");
				var result = _InternalRequest(
					template:"#uri#/App2/test.cfm"
				);
				expect(result.fileContent.trim()).toBe("True");
			});
		});
	}

	// Private functions
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}

	private struct function mySqlCredentials() {
		// getting the credentials from the environment variables
		return server.getDatasource("mysql");
	}

	private struct function msSqlCredentials() {
		// getting the credentials from the environment variables
		return server.getDatasource("mssql");
	}
}