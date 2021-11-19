component {
	this.name = "App1";
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());

	variables.suffix = "entity";

	mySQL = getCredentials();
	if(mySQL.count()!=0){
		this.datasource="#{
			class: 'com.mysql.cj.jdbc.Driver'
			, bundleName:'com.mysql.cj'
			, bundleVersion:'8.0.15'
			, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
			, username: mySQL.username
			, password: mySQL.password
		}#";
	}

	this.ormEnabled = true;
	this.ormSettings = {
		// dialect="MySQLwithInnoDB"
		//dialect="MicrosoftSQLServer"
	};

	private struct function getCredentials() {
		// getting the credentials from the enviroment variables
		var mySQL={};
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