component {
	this.name =	"ldev1440";
	mySQL = getCredentials();
	this.datasource = mySQL;

	
	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS ldev1440");
		}
		query{
			echo("CREATE TABLE ldev1440( id INT , name VARCHAR(30) )");
		}
		query{
			echo("INSERT INTO ldev1440 VALUES(1,'micha'), (2, 'lucee')");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}
