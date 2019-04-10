component extends="org.lucee.cfml.test.LuceeTestCase"{
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
		var msSQL={};
		if(isNull(server.system)){
			server.system = structNew();
			currSystem = createObject("java", "java.lang.System");
			server.system.environment = currSystem.getenv();
			server.system.properties = currSystem.getproperties();
		}

		if(
			!isNull(server.system.environment.MSSQL_SERVER) &&
			!isNull(server.system.environment.MSSQL_USERNAME) &&
			!isNull(server.system.environment.MSSQL_PASSWORD) &&
			!isNull(server.system.environment.MSSQL_PORT) &&
			!isNull(server.system.environment.MSSQL_DATABASE)) {
			msSQL.server=server.system.environment.MSSQL_SERVER;
			msSQL.username=server.system.environment.MSSQL_USERNAME;
			msSQL.password=server.system.environment.MSSQL_PASSWORD;
			msSQL.port=server.system.environment.MSSQL_PORT;
			msSQL.database=server.system.environment.MSSQL_DATABASE;
		}
		// getting the credentials from the system variables
		else if(
			!isNull(server.system.properties.MSSQL_SERVER) &&
			!isNull(server.system.properties.MSSQL_USERNAME) &&
			!isNull(server.system.properties.MSSQL_PASSWORD) &&
			!isNull(server.system.properties.MSSQL_PORT) &&
			!isNull(server.system.properties.MSSQL_DATABASE)) {
			msSQL.server=server.system.properties.MSSQL_SERVER;
			msSQL.username=server.system.properties.MSSQL_USERNAME;
			msSQL.password=server.system.properties.MSSQL_PASSWORD;
			msSQL.port=server.system.properties.MSSQL_PORT;
			msSQL.database=server.system.properties.MSSQL_DATABASE;
		}
		return msSQL;
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 
