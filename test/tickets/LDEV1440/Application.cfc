component {
	this.name =	"test";
	mySQL = getCredentials();
	this.datasource = mySQL;

	
	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS usersDetails");
		}
		query{
			echo("CREATE TABLE usersDetails( id INT , name VARCHAR(30) )");
		}
		query{
			echo("INSERT INTO usersDetails VALUES(1,'micha'), (2, 'lucee')");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}
