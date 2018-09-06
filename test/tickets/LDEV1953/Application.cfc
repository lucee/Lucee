component {
	this.name = "test345";

	mySQL= getCredentials();
	this.datasource = {
		  class: 'com.mysql.cj.jdbc.Driver'
		, bundleName: 'com.mysql.cj'
		, bundleVersion: '8.0.11'
		, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Calcutta&useLegacyDatetimeCode=true'
		, connectionLimit:100 
		, storage:true
		, alwaysSetTimeout:true 
		, blob:true // default: false
		, clob:true // default: false
		, username: mySQL.username
		, password: mySQL.password
	};
	
	function onRequestStart(){
		setting showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS users");
		}
		query{
			echo("CREATE TABLE users( id INT , name VARCHAR(30) )");
		}
		query{
			echo("INSERT INTO users VALUES(123,'micha'), (999, 'lucee')");
		}
	}
	
	private struct function getCredentials() {

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
