component {
	this.name = "AutoDetectMySql";
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());

	variables.suffix = "entity";

	mySQL = getCredentials();
	if (mySQL.count()!=0){
		this.datasource=mySQL;
	}

	this.ormEnabled = true;
	this.ormSettings = {
		// dialect="MySQLwithInnoDB"
		//dialect="MicrosoftSQLServer"
	};
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}