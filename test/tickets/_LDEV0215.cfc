component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run(){
		describe( title="Test suite for LDEV-215", skip=checkMySqlEnvVarsAvailable(), body=function(){
			it(title="Checking MYSQL, INDEX for the client storage table", body=function(){
				var uri = createURI("LDEV0215/test.cfm");
				var result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				expect(result.fileContent.trim() > 0).toBeTrue();
			});

			it(title="Checking MSSQL db, INDEX value for the client storage table", skip=checkMsSqlEnvVarsAvailable(), body=function(){
				var uri = createURI("LDEV0215/test.cfm");
				var result = _InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				expect(result.fileContent.trim() > 0).toBeTrue();
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

	private boolean function checkMsSqlEnvVarsAvailable() {
		// getting the credentials from the environment variables
		var msSQL={};
		if(isNull(server.system)){
			server.system = structNew();
			currSystem = createObject("java", "java.lang.System");
			server.system.environment = currSystem.getenv();
			server.system.properties = currSystem.getproperties();
		}

		if(
			!isNull(server.system.environment.MsSQL_SERVER) &&
			!isNull(server.system.environment.MsSQL_USERNAME) &&
			!isNull(server.system.environment.MsSQL_PASSWORD) &&
			!isNull(server.system.environment.MsSQL_PORT) &&
			!isNull(server.system.environment.MsSQL_DATABASE)) {
			msSQL.server=server.system.environment.MsSQL_SERVER;
			msSQL.username=server.system.environment.MsSQL_USERNAME;
			msSQL.password=server.system.environment.MsSQL_PASSWORD;
			msSQL.port=server.system.environment.MsSQL_PORT;
			msSQL.database=server.system.environment.MsSQL_DATABASE;
		}
		// getting the credentials from the system variables
		else if(
			!isNull(server.system.properties.MsSQL_SERVER) &&
			!isNull(server.system.properties.MsSQL_USERNAME) &&
			!isNull(server.system.properties.MsSQL_PASSWORD) &&
			!isNull(server.system.properties.MsSQL_PORT) &&
			!isNull(server.system.properties.MsSQL_DATABASE)) {
			msSQL.server=server.system.properties.MsSQL_SERVER;
			msSQL.username=server.system.properties.MsSQL_USERNAME;
			msSQL.password=server.system.properties.MsSQL_PASSWORD;
			msSQL.port=server.system.properties.MsSQL_PORT;
			msSQL.database=server.system.properties.MsSQL_DATABASE;
		}
		return structIsEmpty(msSQL);
	}
}