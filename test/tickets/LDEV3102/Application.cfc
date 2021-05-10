component {

	msSQL = getcredentials();

	this.name = "luceetest";
	this.datasources["ldev3102_DSN"] = server.getDatasource("mssql");
	this.datasource = "ldev3102_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS ldev3102");
		}
		query{
			echo("CREATE TABLE ldev3102( id int, test varchar(20))");
		}
		query{
			echo("INSERT INTO ldev3102 VALUES( 1,'testcase' )");
		}	
	}	

	private struct function getcredentials() {
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
			echo("DROP TABLE IF EXISTS ldev3102");
		}
	}
}