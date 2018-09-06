component {
	this.name = createUUID();
	request.mySQL = getCredentials();
	if(request.mySQL.count()!=0){
		this.datasource="#{
			class: 'com.mysql.cj.jdbc.Driver'
			, bundleName:'com.mysql.cj'
			, bundleVersion:'8.0.9'
			, connectionString: 'jdbc:mysql://'&request.mySQL.server&':'&request.mySQL.port&'/'&request.mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
			, username: request.mySQL.username
			, password: request.mySQL.password
			,storage:true
		}#";
	}


	public function onApplicationStart() {
		query {
			echo("DROP DATABASE IF EXISTS `LDEV1980DB`");
		}
		query {
			echo("
				CREATE DATABASE `LDEV1980DB`");
		}
	}
	private struct function getCredentials() {
		// getting the credentials from the enviroment variables
		var mySQLStruct={};
		if(
			!isNull(server.system.environment.MYSQL_SERVER) &&
			!isNull(server.system.environment.MYSQL_USERNAME) &&
			!isNull(server.system.environment.MYSQL_PASSWORD) &&
			!isNull(server.system.environment.MYSQL_PORT) &&
			!isNull(server.system.environment.MYSQL_DATABASE)) {
			mySQLStruct.server=server.system.environment.MYSQL_SERVER;
			mySQLStruct.username=server.system.environment.MYSQL_USERNAME;
			mySQLStruct.password=server.system.environment.MYSQL_PASSWORD;
			mySQLStruct.port=server.system.environment.MYSQL_PORT;
			mySQLStruct.database=server.system.environment.MYSQL_DATABASE;
		}
		// getting the credentials from the system variables
		else if(
			!isNull(server.system.properties.MYSQL_SERVER) &&
			!isNull(server.system.properties.MYSQL_USERNAME) &&
			!isNull(server.system.properties.MYSQL_PASSWORD) &&
			!isNull(server.system.properties.MYSQL_PORT) &&
			!isNull(server.system.properties.MYSQL_DATABASE)) {
			mySQLStruct.server=server.system.properties.MYSQL_SERVER;
			mySQLStruct.username=server.system.properties.MYSQL_USERNAME;
			mySQLStruct.password=server.system.properties.MYSQL_PASSWORD;
			mySQLStruct.port=server.system.properties.MYSQL_PORT;
			mySQLStruct.database=server.system.properties.MYSQL_DATABASE;
		}
		return mySQLStruct;
	}
}