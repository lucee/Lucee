component {
	this.name =	"LDEV1576-" & Hash( GetCurrentTemplatePath() );
	mySQL = getCredentials();
	this.datasource = server.getDatasource("mysql");

	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV1576");
		}
		query{
			echo("CREATE TABLE LDEV1576(id INT, passThumbnail MEDIUMBLOB)");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}
