component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.uri = createURI("LDEV2902");
	}

	function run( testResults, testBox ) { 
		describe( "testcase for LDEV-2902", function(){
			it(title="Checking datasource configured with timezone",body=function( currentSpec ){
				if(!hasCredencials()) return;
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 1}
				);
				expect(trim(result.filecontent)).toBe("America/Chicago");
			});

			it(title="Checking datasource configured without timezone",body=function( currentSpec ){
				if(!hasCredencials()) return;
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 2}
				);
				expect(trim(result.filecontent)).toBe("key [TIMEZONE] doesn't exist");
			});

			it(title="Checking datasource configured Empty timezone",body=function( currentSpec ){
				if(!hasCredencials()) return;
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 3}
				);
				expect(trim(result.filecontent)).toBe("key [TIMEZONE] doesn't exist");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private struct function hasCredencials() {
		if(
			!isNull(server.system.environment.MSSQL_SERVER) && 
			!isNull(server.system.environment.MSSQL_USERNAME) && 
			!isNull(server.system.environment.MSSQL_PASSWORD) && 
			!isNull(server.system.environment.MSSQL_PORT) && 
			!isNull(server.system.environment.MSSQL_DATABASE)) {
			return true;
		}
		else if(
			!isNull(server.system.properties.MSSQL_SERVER) && 
			!isNull(server.system.properties.MSSQL_USERNAME) && 
			!isNull(server.system.properties.MSSQL_PASSWORD) && 
			!isNull(server.system.properties.MSSQL_PORT) && 
			!isNull(server.system.properties.MSSQL_DATABASE)) {
			return true;
		}
		return false;
	}
}