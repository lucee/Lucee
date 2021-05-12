component {

	this.name = "lucee_postgres2";
	this.datasources["pgSQL_DSN"] = server.getDatasource("postgres");
	this.datasource = "pgSQL_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS test_pgSQL");
		}
		query{
			echo("CREATE TABLE test_pgSQL( id int, name varchar(20), age int)");
		}
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS test_pgSQL");
		}
	}
}
