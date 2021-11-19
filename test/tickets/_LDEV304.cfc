component extends="org.lucee.cfml.test.LuceeTestCase" labels="mssql"{
		function beforeAll(){
		variables.uri = createURI("LDEV304");
	}
	// skip closure
	function isMsSqlNotSupported() {
		var msSQL = msSqlCredentials();
		if(!isNull(msSQL)){
			return false;
		} else{
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-304", function() {
			it( title='Checking Query object does not return query result in cfquery', skip=isMsSqlNotSupported(), body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test1.cfm");
				expect(result.filecontent.trim()).toBe("test");
			});
			it( title='Checking Query object does not return query result in query()',skip=isMsSqlNotSupported(),  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test2.cfm");
				expect(result.filecontent.trim()).toBe("test");
			});
		});
	}

	private struct function msSqlCredentials() {
		// getting the credentials from the environment variables
		return server.getDatasource("mssql");
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 
