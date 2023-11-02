component {

	this.name = "LDEV-1937";
	mySQL = getCredentials();
	msSql = msSqlCredentials();
	variables.adminWeb = new org.lucee.cfml.Administrator("web", server.WebAdminPassword);

    createDatasouce("testMYSQL", "mysql", mySQL);
    createDatasouce("testMSSQL", "mssql", msSql);

	// any other application.cfc stuff goes below:
	this.sessionManagement	= true;

	// ORM CONFIG
	this.datasource	= "testMsSQL";
	this.ormenabled	= true;	
	this.ormsettings.dialect = {testMYSQL="mysql"}; //testMYSQL is datasource, this dialect is applicable for mysql database
	this.ormsettings.dialect = {testMsSQL="MicrosoftSQLServer"}; //testMsSQL is datasource this dialect is applicable for mysql database
	this.ormsettings.dbcreate = {testMYSQL="dropcreate"}; // testMYSQL
	this.ormsettings.dbcreate = "none"; // this applicable for default datasource
	this.ormsettings.flushatrequestend = true;	// we are going to manually commit all transactions

	function onApplicationStart(){
		query{
			echo("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'mssql') BEGIN DROP TABLE mssql END");
   		}
		query{
			echo("CREATE TABLE mssql( id INT IDENTITY(1,1) PRIMARY KEY, label varchar(50) )");
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

	private struct function msSqlCredentials() {
		// getting the credentials from the environment variables
		var msSQL={};
		if(isNull(server.system)){
			server.system = structNew();
			currSystem = createObject("java", "java.lang.System");
			server.system.environment = currSystem.getenv();
			server.system.properties = currSystem.getproperties();
		}

		if(
			!isNull(server.system.environment.MsSQL_SERVER) &&
			!isNull(server.system.environment.MsSQL_USERNAME) &&
			!isNull(server.system.environment.MsSQL_PASSWORD) &&
			!isNull(server.system.environment.MsSQL_PORT) &&
			!isNull(server.system.environment.MsSQL_DATABASE)) {
			msSQL.server=server.system.environment.MsSQL_SERVER;
			msSQL.username=server.system.environment.MsSQL_USERNAME;
			msSQL.password=server.system.environment.MsSQL_PASSWORD;
			msSQL.port=server.system.environment.MsSQL_PORT;
			msSQL.database=server.system.environment.MsSQL_DATABASE;
		}
		// getting the credentials from the system variables
		else if(
			!isNull(server.system.properties.MsSQL_SERVER) &&
			!isNull(server.system.properties.MsSQL_USERNAME) &&
			!isNull(server.system.properties.MsSQL_PASSWORD) &&
			!isNull(server.system.properties.MsSQL_PORT) &&
			!isNull(server.system.properties.MsSQL_DATABASE)) {
			msSQL.server=server.system.properties.MsSQL_SERVER;
			msSQL.username=server.system.properties.MsSQL_USERNAME;
			msSQL.password=server.system.properties.MsSQL_PASSWORD;
			msSQL.port=server.system.properties.MsSQL_PORT;
			msSQL.database=server.system.properties.MsSQL_DATABASE;
		}
		return msSQL;
	}


	private void function createDatasouce(string name, string type, struct dataSQL) {
		var tmpStr = {};
		tmpStrt.name = "TestDSN";
		tmpStrt.type = "#arguments.type#";
		tmpStrt.newName = "#arguments.name#";
		tmpStrt.host = dataSQL.server;
		tmpStrt.database = dataSQL.database;
		tmpStrt.port = dataSQL.port;
		tmpStrt.timezone = "";
		tmpStrt.username = dataSQL.username;
		tmpStrt.password = dataSQL.password;
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
	}
}
