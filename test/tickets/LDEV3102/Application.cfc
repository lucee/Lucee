component {

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

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS ldev3102");
		}
	}
}