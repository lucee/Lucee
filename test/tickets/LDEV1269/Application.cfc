component {

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;

	mySQL=getCredencials();

	this.datasources["sample"] ={
		  class: 'org.gjt.mm.mysql.Driver'
		, bundleName:'com.mysql.jdbc'
		, bundleVersion:'5.1.38'
		, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
		, username: mySQL.username
		, password: mySQL.password
	};
	this.datasource = "sample";

	function onRequestStart(){
		setting showdebugOutput=false;
		// init the table used
		query {
	        echo("SET FOREIGN_KEY_CHECKS=0");
		}
		query {
	        echo("DROP TABLE IF EXISTS `test`");
		}
		query {
	        echo("CREATE TABLE `test` (
	`id` int(11) unsigned NOT NULL
	)");
		}
		query {
	        echo("INSERT INTO `test` VALUES ('4294967295'), ('4294967294'), ('4294967293');");
		}
	}


	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
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
		// getting the credetials from the system variables
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