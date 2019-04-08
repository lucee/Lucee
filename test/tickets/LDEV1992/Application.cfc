component {

	this.name	=	'LDEV1992';
	mySQL = getCredentials();
	variables.adminWeb = new org.lucee.cfml.Administrator("web", request.WebAdminPassword);
	tmpStrt.name = "TestDSN";
	tmpStrt.type = "MYSQL";
	tmpStrt.newName = "TestDSN1";
	tmpStrt.host = mySQL.server;
	tmpStrt.database = mySQL.database;
	tmpStrt.port = mySQL.port;
	tmpStrt.timezone = "";
	tmpStrt.username = mySQL.username;
	tmpStrt.password = mySQL.password;
	tmpStrt.connectionLimit = "10";
	tmpStrt.connectionTimeout = "0";
	tmpStrt.metaCacheTimeout = "60000";
	tmpStrt.blob = false;
	tmpStrt.clob = false;
	tmpStrt.validate = false;
	tmpStrt.storage = false; 
	tmpStrt.allowedSelect = false;
	tmpStrt.allowedInsert = false;
	tmpStrt.allowedUpdate = false;
	tmpStrt.allowedDelete = false;
	tmpStrt.allowedAlter = false;
	tmpStrt.allowedDrop = false;
	tmpStrt.allowedRevoke = false;
	tmpStrt.allowedCreate = false;
	tmpStrt.allowedGrant = false;
	tmpStrt.verify = false;
	adminWeb.updateDatasource(argumentCollection = tmpStrt);
	this.datasource = "TestDSN1";
	
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
	        echo("DROP TABLE IF EXISTS `users`");
		}
		query {
	        echo("DROP TABLE IF EXISTS `roles`");
		}
		query {
	        echo("CREATE TABLE `roles` (
			  `roleID` INT(11),
			  `role` VARCHAR(100) DEFAULT NULL,
			  PRIMARY KEY (`roleID`)
			)");
		}
		query {
	        echo("
			INSERT INTO `roles` (`roleID`, `role`)
				VALUES
					(13,'Administrator'),
					(23,'Moderator'),
					(33,'Anonymous')
			");
		}

		query {
	        echo("CREATE TABLE `users` (
		`user_id` VARCHAR(50) NOT NULL,
		`firstName` VARCHAR(50) NOT NULL,
		`FKRoleID` INT(11) DEFAULT NULL,
		PRIMARY KEY (`user_id`),
		KEY `FKRoleID` (`FKRoleID`),
		CONSTRAINT `users_ibfk_1` FOREIGN KEY (`FKRoleID`) REFERENCES `roles` (`roleID`) ON DELETE CASCADE ON UPDATE CASCADE
		)");
		}
		query {
	        echo("
				INSERT INTO `users` (`user_id`, `firstName`, `FKRoleID`)
					VALUES
						('4028818e2fb6c893012fe637c5db00a7','lucee',23)
				");
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