component {

	this.name = createUUID();
	include "../../datasource/configuration/mssql_configuration.cfm";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS test_table");
		}
		query{
			echo("CREATE TABLE test_table( id int, name varchar(20))");
		}
		query{
			echo("insert into test_table values(1,'lucee_test')");
		}
	}
	
	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS test_table");
		}
	}
}
