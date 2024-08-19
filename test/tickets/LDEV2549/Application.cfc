component {
	param name="form.db";
	this.name = "LDEV2549";
	this.datasources["LDEV2549_DSN"] = server.getDatasource(form.db);
	this.datasource = "LDEV2549_DSN";

	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV2549");
		}
		query{
			echo("CREATE TABLE LDEV2549( day date, id int)");
		}
		query{
			echo("INSERT INTO LDEV2549 VALUES( '1996-10-27','2')");
		}
		query{
			echo("INSERT INTO LDEV2549 VALUES( '1998-10-20','1')");
		}

		query{
			echo("DROP TABLE IF EXISTS LDEV2549_2");
		}
		query{
			echo("CREATE TABLE LDEV2549_2( day date, block int)");
		}
		query{
			echo("INSERT INTO LDEV2549_2 VALUES( '1996-10-27',1)");
		}
	}	

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2549");
		}
	}
}
