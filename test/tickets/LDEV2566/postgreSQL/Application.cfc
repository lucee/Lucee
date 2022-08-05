component {

	this.name = "luceeTestPostgreSQL";
	this.datasources["ldev2566_POSTGRESQL"] = server.getDatasource("postgres");
	this.datasource = "ldev2566_POSTGRESQL";

	
	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV2566_postTable");
		}
		query{
			echo("CREATE TABLE LDEV2566_postTable( id serial, name varchar(20))");
		}
	}
	
	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2566_postTable");
		}
	}
}