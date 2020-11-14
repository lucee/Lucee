component {

	mySQL = getCredentials();

	this.name = "luceedfdftefdfst";
	this.datasources["ldev2566_MYSQL"] = {
		class : 'com.mysql.cj.jdbc.Driver'
		, bundleName : 'com.mysql.cj'
		, bundleVersion : '8.0.15'
		, connectionString : 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
		, username : mySQL.username
	};
	this.datasource = "ldev2566_MYSQL";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2566_mysql");
		}
		query{
			echo("CREATE TABLE LDEV2566_mysql( emp_id int primary key auto_increment, emp_name varchar(20), emp_age int)");
		}
	}

	private struct function getCredentials() {
		// getting the credetials from the enviroment variables
		var mySQL = {};
		if(
			!isNull(server.system.environment.MYSQL_SERVER) && 
			!isNull(server.system.environment.MYSQL_USERNAME) && 
			!isNull(server.system.environment.MYSQL_PORT) && 
			!isNull(server.system.environment.MYSQL_DATABASE)) {
			mySQL.server = server.system.environment.MYSQL_SERVER;
			mySQL.username = server.system.environment.MYSQL_USERNAME;
			mySQL.port = server.system.environment.MYSQL_PORT;
			mySQL.database = server.system.environment.MYSQL_DATABASE;
		}
		// getting the credetials from the system variables
		else if(
			!isNull(server.system.properties.MYSQL_SERVER) && 
			!isNull(server.system.properties.MYSQL_USERNAME) && 
			!isNull(server.system.properties.MYSQL_PORT) && 
			!isNull(server.system.properties.MYSQL_DATABASE)) {
			mySQL.server = server.system.properties.MYSQL_SERVER;
			mySQL.username = server.system.properties.MYSQL_USERNAME;
			mySQL.port = server.system.properties.MYSQL_PORT;
			mySQL.database = server.system.properties.MYSQL_DATABASE;
		}
		return mySQL;
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2566_mysql");
		}
	}
}