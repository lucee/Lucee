component {
	this.name = "LDEV1953";

	mySQL= getCredentials();
	mySQL.storage = true;	
	this.datasource = mySQL;
	
	
	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV1953");
		}
		query{
			echo("CREATE TABLE LDEV1953( id INT , name VARCHAR(30) )");
		}
		query{
			echo("INSERT INTO LDEV1953 VALUES(123,'micha'), (999, 'lucee')");
		}
	}
	
	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

}
