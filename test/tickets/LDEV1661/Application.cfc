component {
	this.name =	"tests345";
	mySQL = getCredentials();
	this.datasource = server.getDatasource("mysql");

	function onRequestStart(){
		setting showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS users");
		}
		query{
			echo("CREATE TABLE users(sNo INT,datetimeField datetime,myTimestamp timestamp)");
		}
		query{
			echo("INSERT INTO users(sNo,datetimeField,myTimestamp)VALUES(123,'2018-04-10 02:50:30','2018-04-10 02:50:30'),(345,'2018-07-12 08:30:30','2018-03-03 04:50:30')");
		}
	}

	private struct function getCredentials() {
		// getting the credentials from the enviroment variables
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
