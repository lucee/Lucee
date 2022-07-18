component {
	this.name = "AutoDetectMsSql";
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());

	variables.suffix = "entity";

	msSQL = getCredentials();
	if (msSQL.count()!=0){
		this.datasource=msSQL;
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
		return server.getDatasource("mssql");
	}
}