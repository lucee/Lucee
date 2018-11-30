component {
	this.name =	"App2";
	this.clientManagement = true;
	this.sessionmanagement = true;
	this.sessionTimeout = createTimeSpan(0,0,30,0);

	msSQL = msSqlCredentials();
	datasource = "ms-ldev-215";
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
	this.clientStorage = datasource;
	this.sessionStorage = datasource;

	function onRequestStart(){
		setting showdebugOutput=false;
	}

	function onApplicationStart(){
		query {
			echo("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'cf_client_data') BEGIN DROP TABLE cf_client_data END");
		}
		query {
  			echo("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'cf_session_data') BEGIN DROP TABLE cf_session_data END");
		}
		session.test = "App2";
		client.test = "App2";
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