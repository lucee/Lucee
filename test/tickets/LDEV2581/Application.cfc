component {

	this.name = "luceetest";
	this.datasources["LDEV2581"] = server.getDatasource("mssql");
	this.datasource = "LDEV2581";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2581");
		}
		query{
			echo("CREATE TABLE LDEV2581( id tinyint, name varchar(20) )");
		}
		query{
			echo("INSERT INTO LDEV2581 VALUES( 1,'testcase' )");
		}
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2581");
		}
	}
}