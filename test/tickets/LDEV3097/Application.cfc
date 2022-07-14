component {

	msSQL = getcredentials();

	this.name = "luceetest";
	this.datasources["LDEV3097_DSN"] = {
	 	class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName: 'com.microsoft.sqlserver.mssql-jdbc'
		, bundleVersion: '7.0.0'
		, connectionString: 'jdbc:sqlserver://'&msSQL.server&':'&msSQL.port&';DATABASENAME='&msSQL.database&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: msSQL.username
		, password: msSQL.password
	};
	this.datasource = "LDEV3097_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV3097");
		}
		query{
			echo("CREATE TABLE LDEV3097 ( name varchar(200) )");
		}	
	}	

	private struct function getcredentials() {
		var msSQL={};
		if(
			!isNull(server.system.environment.MSSQL_SERVER) && 
			!isNull(server.system.environment.MSSQL_USERNAME) && 
			!isNull(server.system.environment.MSSQL_PASSWORD) && 
			!isNull(server.system.environment.MSSQL_PORT) && 
			!isNull(server.system.environment.MSSQL_DATABASE)) {
			msSQL.server=server.system.environment.MSSQL_SERVER;
			msSQL.username=server.system.environment.MSSQL_USERNAME;
			msSQL.password=server.system.environment.MSSQL_PASSWORD;
			msSQL.port=server.system.environment.MSSQL_PORT;
			msSQL.database=server.system.environment.MSSQL_DATABASE;
		}
		else if(
			!isNull(server.system.properties.MSSQL_SERVER) && 
			!isNull(server.system.properties.MSSQL_USERNAME) && 
			!isNull(server.system.properties.MSSQL_PASSWORD) && 
			!isNull(server.system.properties.MSSQL_PORT) && 
			!isNull(server.system.properties.MSSQL_DATABASE)) {
			msSQL.server=server.system.properties.MSSQL_SERVER;
			msSQL.username=server.system.properties.MSSQL_USERNAME;
			msSQL.password=server.system.properties.MSSQL_UPASSWORD;
			msSQL.port=server.system.properties.MSSQL_PORT;
			msSQL.database=server.system.properties.MSSQL_DATABASE;
		}
		return msSQL;
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV3097");
		}
	}
}