component {

	mySQL = getCredentials();
	this.name = "luceetest";
	this.datasources["ldev3091_DSN"] = mySQL;
	this.datasource = "ldev3091_DSN";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS ldev3118");
		}
	}	

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS ldev3118");
		}
	}
}