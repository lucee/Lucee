component {

	this.name = hash( getCurrentTemplatePath() );
	
	/*this.datasource={
  		class: 'org.h2.Driver'
	  	, bundleName: 'org.h2'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
	};*/

	mySQL=getCredencials();
	this.datasource=server.getDatasource("mysql");

	this.ormEnabled = true;
	this.ormSettings.flushatrequestend = false;
	this.ormSettings.autoManageSession = false;
	this.ormSettings.dbcreate = "dropcreate";
	this.ormSettings.savemapping = true;
	this.ormSettings.eventHandling = true;
	
	public any function onRequestStart() {
		ormReload();
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