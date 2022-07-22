component {
	
	this.name = "luceetest";
	this.datasources["LDEV2616"] = server.getDatasource("mssql");
	this.datasource = "LDEV2616";

	
	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV2616");
		}
		query{
			echo("CREATE TABLE LDEV2616( id int, name varchar(20))");
		}
	}	

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2616");
		}
	}
}
