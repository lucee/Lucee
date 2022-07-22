component {

	this.name = "luceetest";
	this.datasources["ldev3109_DSN"] = server.getDatasource("mssql");
	this.datasource = "ldev3109_DSN";

	
	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS ldev3109");
		}
		query{
			echo("CREATE TABLE ldev3109( numbers decimal(15,2))");
		}
	}	

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS ldev3109");
		}
	}
}