component {

	this.name = "luceetest";
	this.datasources["LDEV3097_DSN"] = server.getDatasource("mssql");
	this.datasource = "LDEV3097_DSN";

	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV3097");
		}
		query{
			echo("CREATE TABLE LDEV3097 ( name varchar(200) )");
		}	
	}	

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV3097");
		}
	}
}