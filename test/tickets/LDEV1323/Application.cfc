component {
	this.name = "test";
	mySQL= getCredentials();
	this.datasource = {
		 type: "mysql"
		 ,host: "#mySQL.server#"
		 ,port: "#mySQL.port#"
		 ,database: "#mySQL.database#?"
		 ,username: "#mySQL.username#"
		 ,password: "#mySQL.password#"
		 ,custom: { useUnicode:true }
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

	private function createTable() {
		query {
	        echo("DROP TABLE IF EXISTS `test`");
		}
		query {
	        echo("CREATE TABLE `test` (`id` int,`name` varchar)");
		}
		query {
	        echo("INSERT INTO `test` VALUES (1,'john');");
		}
	}
}