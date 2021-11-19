component {
	this.name = "AutoDetectOracle";
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());

	variables.suffix = "entity";

	oracle = getCredentials();
	if (oracle.count()!=0){
		this.datasource=oracle;
	}

	this.ormEnabled = true;
	this.ormSettings = {
		// dialect="MySQLwithInnoDB"
		//dialect="MicrosoftSQLServer"
	};

	private struct function getCredentials() {
		return server.getDatasource("oracle");
	}
}