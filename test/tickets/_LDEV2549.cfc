component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.uri = createURI("LDEV2549");
	}

	function run( testResults , testBox ) {
		describe( "test suite for LDEV2549",skip=isNotSupported(), function() {
			it(title = "query param not working without CFSQLTYPE for date type values", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2549.cfm",
					forms:	{scene=1}
				);
				expect(1).toBe(result.filecontent);
			});

			it(title = "query param works fine with CFSQLTYPE for date type values", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2549.cfm",
					forms:	{scene=2}
				);
				expect(2).toBe(local.result.filecontent);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function isNotSupported() {
		// getting the credetials from the enviroment variables
		if(
			!isNull(server.system.environment.MSSQL_SERVER) && 
			!isNull(server.system.environment.MSSQL_USERNAME) && 
			!isNull(server.system.environment.MSSQL_PASSWORD) && 
			!isNull(server.system.environment.MSSQL_PORT) && 
			!isNull(server.system.environment.MSSQL_DATABASE)) {
			return false;
		}
		// getting the credetials from the system variables
		else if(
			!isNull(server.system.properties.MSSQL_SERVER) && 
			!isNull(server.system.properties.MSSQL_USERNAME) && 
			!isNull(server.system.properties.MSSQL_PASSWORD) && 
			!isNull(server.system.properties.MSSQL_PORT) && 
			!isNull(server.system.properties.MSSQL_DATABASE)) {
			return false;
		}
		return true;
	}
}