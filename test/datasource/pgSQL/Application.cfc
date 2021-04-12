component {

	pgSQL = getCredencials();

	this.name = "luceetest";
	this.datasources["pgSQL_DSN"] = pgSQL;
	this.datasource = "pgSQL_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS test_pgSQL");
		}
		query{
			echo("CREATE TABLE test_pgSQL( id int, name varchar(20), age int)");
		}
	}
	
	private struct function getCredencials() {
		return server.getDatasource("postgres");
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS test_pgSQL");
		}
	}
}
