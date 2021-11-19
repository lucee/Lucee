component {

	msSQL=getCredencials();
	
	this.name = "Luceetest";
	this.datasources["LDEV2549_DSN"] = {
	 	class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName: 'com.microsoft.sqlserver.mssql-jdbc'
		, bundleVersion: '7.0.0'
		, connectionString: 'jdbc:sqlserver://'&#msSQL.server#&':'&#msSQL.port#&';DATABASENAME='&#msSQL.database#&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: #msSQL.username#
		, password: #msSQL.password#
	};
	this.datasource = "LDEV2549_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2549");
		}
		query{
			echo("CREATE TABLE LDEV2549( day date, id int)");
		}
		query{
			echo("INSERT INTO LDEV2549 VALUES( '1996-10-27','2')");
			echo("INSERT INTO LDEV2549 VALUES( '1998-10-20','1')");
		}
		query{
			echo("DROP TABLE IF EXISTS mytest");
		}
		query{
			echo("CREATE TABLE mytest( day date, block int)");
		}
		query{
			echo("INSERT INTO mytest VALUES( '1996-10-27',1)");
		}
	}	
	
	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
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
		// getting the credetials from the system variables
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
			echo("DROP TABLE IF EXISTS LDEV2549");
		}
	}
}
