component {
	this.name = "AutoDetectPostgres";
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());

	variables.suffix = "entity";

	postgres = getCredentials();
	if(postgres.count()!=0){
		this.datasource=postgres;
	}

	this.ormEnabled = true;
	this.ormSettings = {
		// dialect="MySQLwithInnoDB"
		//dialect="MicrosoftSQLServer"
	};

	private struct function getCredentials() {
		return server.getDatasource("postgres");
	}
}