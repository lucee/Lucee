component extends="org.lucee.cfml.test.LuceeTestCase"{
	multiDSNError = "can't use different connections inside a transaction";
	function run(){
		describe( title="Test suite for checking cftransaction with multiple datasources(hsql with ORM)", body=function(){
			it(title="With valid table names, but different datasources", body=function(){
				uri = createURI("LDEV0563/hsql/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				errorMsg = listGetAt(result.fileContent.trim(), 1, "|", true);
				Count1 = listGetAt(result.fileContent.trim(), 2, "|", true);
				Count2 = listGetAt(result.fileContent.trim(), 3, "|", true);
				Count3 = listGetAt(result.fileContent.trim(), 4, "|", true);
				expect(errorMsg).toBe("");
				expect(Count1).toBe(2);
				expect(Count2).toBe(2);
				expect(Count2).toBe(2);
			});

			it(title="With invalid table name to check first table is rolled back or not", body=function(){
				uri = createURI("LDEV0563/hsql/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				errorMsg = listGetAt(result.fileContent.trim(), 1, "|", true);
				Count1 = listGetAt(result.fileContent.trim(), 2, "|", true);
				Count2 = listGetAt(result.fileContent.trim(), 3, "|", true);
				Count3 = listGetAt(result.fileContent.trim(), 4, "|", true);
				expect(errorMsg).toBe("Table not found in statement [INSERT INTO users4]");
				expect(Count1).toBe(1);
				expect(Count2).toBe(1);
				expect(Count3).toBe(1);
			});
		});

		describe( title="Test suite for checking cftransaction with multiple datasources(mysql without ORM)", skip=checkMySqlEnvVarsAvailable(), body=function(){
			it(title="With valid table names, but different datasources", body=function(){
				uri = createURI("LDEV0563/mysql/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				errorMsg = listGetAt(result.fileContent.trim(), 1, "|", true);
				Count1 = listGetAt(result.fileContent.trim(), 2, "|", true);
				Count2 = listGetAt(result.fileContent.trim(), 3, "|", true);
				Count3 = listGetAt(result.fileContent.trim(), 4, "|", true);
				expect(errorMsg).toBe('');
				expect(Count1).toBe(2);
				expect(Count2).toBe(2);
				expect(Count3).toBe(2);
			});

			it(title="With invalid table name to check first table is rolled back or not", body=function(){
				uri = createURI("LDEV0563/mysql/test.cfm");
				result = _InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				errorMsg = listGetAt(result.fileContent.trim(), 1, "|", true);
				Count1 = listGetAt(result.fileContent.trim(), 2, "|", true);
				Count2 = listGetAt(result.fileContent.trim(), 3, "|", true);
				Count3 = listGetAt(result.fileContent.trim(), 4, "|", true);
				var find1=findNoCase("users4' doesn't exist",errorMsg)>0;
				expect(find1).toBe(true);
				expect(Count1).toBe(1);
				expect(Count2).toBe(1);
				expect(Count3).toBe(1);

				// With invalid table name to check first table is rolled back or not:Expected [
				// Table 'luceetestdb.users4' doesn't exist] but received [
				// Table 'test.users4' doesn't exist]
			});
		});
	}

	// Private functions
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
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