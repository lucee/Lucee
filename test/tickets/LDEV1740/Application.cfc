component {
	this.name =	"LDEV1740-" & Hash( GetCurrentTemplatePath() );
	mySQL = getCredentials();
	this.datasource = mySQL;

	function onRequestStart(){
		setting showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV1740");
		}
		query{
			echo("CREATE TABLE LDEV1740(name varchar(255))");
		}
		query{
			echo("Insert into LDEV1740 values ('test')");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}
