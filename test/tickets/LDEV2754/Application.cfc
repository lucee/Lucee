component {

	msSQL = getCredentials();
	this.name = "luceetest";
	this.datasources["ldev2754_dsn"] = {
	 	class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName: 'mssqljdbc4'
		, bundleVersion: '4.0.2206.100'
		, connectionString: 'jdbc:sqlserver://'&msSQL.server&':'&msSQL.port&';DATABASENAME='&msSQL.database&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: msSQL.username
		, password: msSQL.password
	};
	this.datasource = "ldev2754_dsn";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2754");
		}
		query{
			echo("CREATE TABLE LDEV2754( id int, stu_name varchar(20))");
		}
		query{
			echo("INSERT INTO LDEV2754 VALUES( 1,'juwait' )");
		}
	}
	
	private struct function getCredentials() {
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
			echo("DROP TABLE IF EXISTS LDEV2754");
		}
	}
}
