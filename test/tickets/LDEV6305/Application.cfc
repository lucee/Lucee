component {

	this.name = "ldev6305";
	this.datasources["LDEV6305"] = server.getDatasource("mssql");
	this.datasource = "LDEV6305";

	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV6305");
		}
		query{
			echo("CREATE TABLE LDEV6305(id INT IDENTITY(1,1) PRIMARY KEY, float_value FLOAT)");
		}
		query{
			echo("INSERT INTO LDEV6305(float_value) VALUES(2.01)");
		}
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV6305");
		}
	}
}
