component {

	this.name = "luceetest";
	this.datasources["ldev2754_dsn"] = server.getDatasource("mssql");
	this.datasource = "ldev2754_dsn";

	
	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV2754");
		}
		query{
			echo("CREATE TABLE LDEV2754( id int, stu_name varchar(20))");
		}
		query{
			echo("INSERT INTO LDEV2754 VALUES( 1,'juwait' )");
		}
	}
	
	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2754");
		}
	}
}
