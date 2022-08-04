component {

	this.name = "luceetest";
	this.datasources["ldev2979_dsn"] = server.getDatasource("mssql");
	this.datasource = "ldev2979_dsn";

	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS ldev2979");
		}
	}
	
	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS ldev2979");
		}
	}
}