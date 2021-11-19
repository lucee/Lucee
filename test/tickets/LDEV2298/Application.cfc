component {

	this.name = "luceetest";
	this.datasources["ldev2298_DSN"] = server.getDatasource("mssql");
	this.datasource = "ldev2298_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS ldev2298_notnull");
		}
		query{
			echo("CREATE TABLE ldev2298_notnull( id int, employee varchar(20), emp_join_date datetime NOT NULL)");
		}
		query{
			echo("INSERT INTO ldev2298_notnull VALUES( 1,'testcase','2019-06-19' )");
		}
		query{
			echo("DROP TABLE IF EXISTS ldev2298_null");
		}
		query{
			echo("CREATE TABLE ldev2298_null( id int, employee varchar(20), emp_join_date datetime)");
		}
		query{
			echo("INSERT INTO ldev2298_null VALUES( 1,'lucee','1997-04-11' )");
		}
	}	

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS ldev2298_notnull");
		}
		query{
			echo("DROP TABLE IF EXISTS ldev2298_null");
		}
	}
}
