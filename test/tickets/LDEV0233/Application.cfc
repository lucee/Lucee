component output="false" {

	this.name = 'ORMTransaction';
	this.sessionmanagement = true;

	mySQL = getCredentials();

	this.ormenabled = true;
	this.datasources["trans"] = server.getDatasource("mysql");
	this.ormsettings={datasource="trans"
		,logsql="false"
		,cfclocation="model"
		,dbcreate="dropcreate"
		,dialect="MySQL"
		,flushatrequestend=false
	};

	public function onRequestStart() {
		oRMReload();
		saveData = EntityNew('ContentType');
		saveData.setid(1);
		saveData.setTitle("Title");
		EntitySave(saveData);
		oRMFlush();
	}

	// Private function to get dsn details from env vars
	private struct function getCredentials() {
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
		return mysql;
	}

}
