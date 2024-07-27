component {
	
	this.name = "LDEV5050";
	this.datasources["LDEV5050"] = server.getDatasource("mssql");
	this.datasource = "LDEV5050";

	
	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV5050");
		}
		query{
			echo("CREATE TABLE LDEV5050( id int, name varchar(20))");
		}
	}	

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV5050");
		}
	}
}
