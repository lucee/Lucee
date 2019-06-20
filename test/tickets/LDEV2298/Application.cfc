component {

	msSQL=getCredencials();
	
	this.name = "luceetest";
	this.datasources["ldev2298_DSN"] = {
	 	class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName: 'com.microsoft.sqlserver.mssql-jdbc'
		, bundleVersion: '7.0.0'
		, connectionString: 'jdbc:sqlserver://'&#msSQL.server#&':'&#msSQL.port#&';DATABASENAME='&#msSQL.database#&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: #msSQL.username#
		, password: #msSQL.password#
	};
	this.datasource = "ldev2298_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS ldev2298_notnull");
		}
		query{
			echo("CREATE TABLE ldev2298_notnull( id int, employee varchar(20), emp_join_date datetime NOT NULL)");
		}
		query{
			echo("INSERT INTO ldev2298_notnull VALUES( 1,'testcase','2019-06-19' )");
		}
		query{
			echo("DROP TABLE IF EXISTS ldev2298_null");
		}
		query{
			echo("CREATE TABLE ldev2298_null( id int, employee varchar(20), emp_join_date datetime)");
		}
		query{
			echo("INSERT INTO ldev2298_null VALUES( 1,'lucee','1997-04-11' )");
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
			echo("DROP TABLE IF EXISTS ldev2298_notnull");
		}
		query{
			echo("DROP TABLE IF EXISTS ldev2298_null");
		}
	}
}
