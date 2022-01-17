component {

	msSQL = getcredentials();

	this.name = "luceetest-sqlserver";
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
		// getting the credentials from the environment variables
		return server._getSystemPropOrEnvVars( "SERVER, USERNAME, PASSWORD, PORT, DATABASE", "MSSQL_");
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS ldev3102");
		}
	}
}