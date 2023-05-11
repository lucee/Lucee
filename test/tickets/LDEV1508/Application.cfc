component {
	this.name =	"test";
	mySQL = getCredentials();
	this.datasource = mySQL;

	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS test_asset");
		}
		query{
			echo("CREATE TABLE test_asset( asset_id INT , asset_name VARCHAR(50) )");
		}
		query{
			echo("INSERT INTO test_asset VALUES(1,'micha'), (2, 'lucee')");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}