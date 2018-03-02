component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(  testResults , testBox ) {
		describe( title="Test suite for LDEV-1569",  skip=checkMySqlEnvVarsAvailable(), body=function() {
			it(title="checking SerializeJSON() with ORM keys having NULL value", body = function( currentSpec ) {
				var uri=createURI("LDEV1569/test.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe('{"Name":null,"ID":2}');
			});

			it(title="checking SerializeJSON() with Query having NULL values ", body = function( currentSpec ) {
				var qry = QueryNew("ID,Name");
				QueryAddRow(qry);
				QuerySetCell(qry,"ID",2);
				QuerySetCell(qry,"Name", null);


				var qEx = queryExecute("select * from qry",{}, {
					returntype: "array",
					dbtype: "query"
				});
				expect(serializeJSON(qEx)).toBe('[{"Name":null,"ID":2}]');
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function checkMySqlEnvVarsAvailable() {
		// getting the credentials from the environment variables
		var mySQL={};
		if(isNull(server.system)){
			server.system = structNew();
			currSystem = createObject("java", "java.lang.System");
			server.system.environment = currSystem.getenv();
			server.system.properties = currSystem.getproperties();
		}

		if(
			!isNull(server.system.environment.MYSQL_SERVER) &&
			!isNull(server.system.environment.MYSQL_USERNAME) &&
			!isNull(server.system.environment.MYSQL_PASSWORD) &&
			!isNull(server.system.environment.MYSQL_PORT) &&
			!isNull(server.system.environment.MYSQL_DATABASE)) {
			mySQL.server=server.system.environment.MYSQL_SERVER;
			mySQL.username=server.system.environment.MYSQL_USERNAME;
			mySQL.password=server.system.environment.MYSQL_PASSWORD;
			mySQL.port=server.system.environment.MYSQL_PORT;
			mySQL.database=server.system.environment.MYSQL_DATABASE;
		}
		// getting the credentials from the system variables
		else if(
			!isNull(server.system.properties.MYSQL_SERVER) &&
			!isNull(server.system.properties.MYSQL_USERNAME) &&
			!isNull(server.system.properties.MYSQL_PASSWORD) &&
			!isNull(server.system.properties.MYSQL_PORT) &&
			!isNull(server.system.properties.MYSQL_DATABASE)) {
			mySQL.server=server.system.properties.MYSQL_SERVER;
			mySQL.username=server.system.properties.MYSQL_USERNAME;
			mySQL.password=server.system.properties.MYSQL_PASSWORD;
			mySQL.port=server.system.properties.MYSQL_PORT;
			mySQL.database=server.system.properties.MYSQL_DATABASE;
		}
		return structIsEmpty(mySQL);
	}
}
