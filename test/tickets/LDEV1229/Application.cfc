component {

	this.name	=	'test';
	this.sessionManagement 	= false;
	
	if (url.db=='h2') {
		this.datasource = server.getDatasource("h2", "#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db" );
	} else {
		mySQL = getCredentials();
		if(mySQL.count()!=0){
			this.datasource={
				class: 'org.gjt.mm.mysql.Driver'
				, bundleName:'com.mysql.jdbc'
				, bundleVersion:'5.1.38'
				, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
				, username: mySQL.username
				, password: mySQL.password
			};
		}
	}

	// ORM settings
	this.ormEnabled = true;
	this.ormSettings = {
		autoManageSession = false
		,flushAtRequestEnd = false
		,dialect = "MySQLwithInnoDB"
	};
	function onRequestStart(){
		if(url.db=='mysql') {
			query {
		        echo("SET FOREIGN_KEY_CHECKS=0");
			}
		}
		query {
	        echo("DROP TABLE IF EXISTS `child`");
		}
		query {
	        echo("DROP TABLE IF EXISTS `parent`");
		}
		query {
	        echo("CREATE TABLE `child` (
	          `ID` int(10) unsigned NOT NULL,
	          `parentID` int(10) unsigned DEFAULT NULL,
	          PRIMARY KEY (`ID`)
	        );");
		}
		query {
	        echo("CREATE TABLE `parent` (
				  `ID` int(10) unsigned NOT NULL,
				  PRIMARY KEY (`ID`)
				);");
		}
		query {
	        echo("INSERT INTO `parent` VALUES ('1');");
		}
		if(url.db=='mysql') {
			query {
	        	echo("SET FOREIGN_KEY_CHECKS=1");
			}
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