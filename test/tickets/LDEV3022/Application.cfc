component {
	
	this.name = "luceetest";
	this.datasources["LDEV3022_DSN"] = server.getDatasource("mssql");
	
	this.datasource = "LDEV3022_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS ldev3022");
		}
		query{
			echo("CREATE TABLE ldev3022( id int, price decimal(10,2))");
		}
		query{
			echo("INSERT INTO ldev3022 VALUES( 1,'11.97' )");
		}
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS ldev3022");
		}
	}
}