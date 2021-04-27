component {

	mySQL=getCredencials();
	this.name = "ldev3091";
	this.datasources["ldev3091"] = mySQL;
	this.datasource = "ldev3091";
	

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS ldev3091");
		}
		query{
			echo("CREATE TABLE ldev3091( col_signed smallint ,col_unsigned smallint unsigned )");
		}
	}	

	private struct function getCredencials() {
		return server.getDatasource("mysql");
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS ldev3091");
		}
	}
}