component {

	this.name = "luceetest";
	this.datasources["LDEV2708"] = server.getDatasource("mssql");
	this.datasource = "LDEV2708";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2708");
		}
		query{
			echo("CREATE TABLE LDEV2708( id int, when_created datetime)");
		}
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2708");
		}
	}
}
