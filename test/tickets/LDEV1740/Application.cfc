component {
	this.name =	"LDEV1740-" & Hash( GetCurrentTemplatePath() );
	mySQL = getCredentials();
	this.datasources["LDEV1740_dsn"] = mySQL;
	this.datasource = "LDEV1740_dsn";

	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV1740");
		}
		query{
			echo("CREATE TABLE LDEV1740(name varchar(255))");
		}
		query{
			echo("INSERT INTO LDEV1740 VALUES ('test')");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}