component {

	this.name	=	'LDEV1793';
	mySQL = getCredentials();

	mySQL = getCredentials();
	if(mySQL.count()!=0){
		this.datasource ="#server.getDatasource("mysql")#";
	}

	// ORM settings
	this.ormEnabled = true;
	this.ormSettings = {
		dialect = "MySQLwithInnoDB"
	};
	function onRequestStart(){

		query {
	        echo("SET FOREIGN_KEY_CHECKS=0");
		}
		query {
	        echo("DROP TABLE IF EXISTS `LDEV1793`");
		}
		query {
	        echo("CREATE TABLE `LDEV1793` (
	          `ID` binary(255),
	          `name` varchar(100)
	        )");
		}
		query {
	        echo("INSERT INTO LDEV1793(id, name) VALUES ('10001100010', 'lucee');");
		}
	}

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