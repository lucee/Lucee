component {

	this.name = "LDEV-1937";
	variables.adminWeb = new org.lucee.cfml.Administrator("web", server.WebAdminPassword);

    createDatasouce("testMYSQL", "mysql", server.getDatasource("mysql"));
    createDatasouce("testMSSQL", "mssql", server.getDatasource("mssql"));

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
