component {
	this.name = "test345";

	mySQL= getCredentials();
	mySQL.storage = true;	
	this.datasource = mySQL;
	
	
	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS users");
		}
		query{
			echo("CREATE TABLE users( id INT , name VARCHAR(30) )");
		}
		query{
			echo("INSERT INTO users VALUES(123,'micha'), (999, 'lucee')");
		}
	}
	
	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

}
