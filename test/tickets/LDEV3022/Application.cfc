component {

	msSQL=getCredentials();
	
	this.name = "luceetest";
	this.datasources["LDEV3022_DSN"] = {
	 	class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName: 'com.microsoft.sqlserver.mssql-jdbc'
		, bundleVersion: '7.2.2.jre8'
		, connectionString: 'jdbc:sqlserver://'&#msSQL.server#&':'&#msSQL.port#&';DATABASENAME='&#msSQL.database#&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: #msSQL.username#
		, password: #msSQL.password#
	};
	
	this.datasource = "LDEV3022_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS ldev3022");
		}
		query{
			echo("CREATE TABLE ldev3022( id int, price decimal(10,2))");
		}
		query{
			echo("INSERT INTO ldev3022 VALUES( 1,'11.97' )");
		}
	}
	
	private struct function getCredentials() {
		// getting the credentials from the enviroment variables
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
		// getting the credentials from the system variables
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
			echo("DROP TABLE IF EXISTS ldev3022");
		}
	}
}