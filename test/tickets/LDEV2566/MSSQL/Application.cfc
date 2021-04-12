component {

	this.name = "luceetestmssql";
	this.datasources["ldev2566_MSSQL"] = server.getDatasource("mssql");
	this.datasource = "ldev2566_MSSQL";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2566_mssql");
		}
		query{
			echo("CREATE TABLE LDEV2566_mssql( id int primary key identity(1,1), name varchar(20), age int)");
		}
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2566_mssql");
		}
	}
}