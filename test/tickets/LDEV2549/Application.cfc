component {
	
	this.name = "Luceetest";
	this.datasources["LDEV2549_DSN"] = server.getDatasource("mssql");
	this.datasource = "LDEV2549_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2549");
		}
		query{
			echo("CREATE TABLE LDEV2549( day date, id int)");
		}
		query{
			echo("INSERT INTO LDEV2549 VALUES( '1996-10-27','2')");
			echo("INSERT INTO LDEV2549 VALUES( '1998-10-20','1')");
		}
		query{
			echo("DROP TABLE IF EXISTS mytest");
		}
		query{
			echo("CREATE TABLE mytest( day date, block int)");
		}
		query{
			echo("INSERT INTO mytest VALUES( '1996-10-27',1)");
		}
	}	

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2549");
		}
	}
}
