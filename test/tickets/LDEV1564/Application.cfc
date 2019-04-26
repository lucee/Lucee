component {
	this.name =	"test123";
	this.ApplicationTimeout = CreateTimeSpan( 2, 0, 0, 0 );
	this.clientManagement = true;
	this.sessionmanagement = true;
	this.SessionTimeout = CreateTimeSpan( 0, 0, 10, 0 );
	this.setclientcookies = True ;

	this.ormenabled = true;
	this.ormsettings.cfclocation = ["/go"];
	this.ormsettings.dbCreate = "none";
	this.ormsettings.autoManageSession=false;
	this.ormsettings.dialect = "MicrosoftSQLServer";

	msSQL = msSqlCredentials();
	datasource = "ormTest";
	this.datasources[datasource] ={
		  class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName:'mssqljdbc4'
		, bundleVersion:'4.0.2206.100'
		, connectionString: 'jdbc:sqlserver://localhost:1433;DATABASENAME=luceetestdb;sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: msSQL.username
		, password: msSQL.password
		, storage:true
	};

	this.dataSource = datasource;

	function onApplicationStart(){
		query{
			echo("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'users') BEGIN DROP TABLE users END");
   		}
		query{
			echo("CREATE TABLE users( uid INT IDENTITY(1,1) PRIMARY KEY, uName varchar(50) )");
		}
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
}