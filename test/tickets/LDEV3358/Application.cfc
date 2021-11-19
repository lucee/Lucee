component {

	mySQL=getCredencials();
	this.name = "LDEV3358";
	this.datasources["LDEV3358"] = {
		  class: 'com.mysql.cj.jdbc.Driver'
		, bundleName: 'com.mysql.cj'
		, bundleVersion: '8.0.19'
		, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?characterEncoding=UTF-8'
		, username: "root"
		, password: ""
	};
	this.datasource = "LDEV3358";
	
	public function onRequestStart(){
		query{
			echo('DROP TABLE IF EXISTS LDEV3358');
		}
		query{
			echo("CREATE TABLE LDEV3358( id_signed smallint, id_unsigned smallint unsigned, value varchar(20) )");
		}

		query{
			echo("INSERT into LDEV3358 values( 32767, 32767, 'lucee'), ( 32222, 60000, 'lucee'), ( 31000, 32768, 'lucee')");
		}
	}

	private struct function getCredencials() {
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
		return mySQL;
	}

	public function onRequestEnd(){
		query{
			echo('DROP TABLE IF EXISTS LDEV3358');
		}
	}
}