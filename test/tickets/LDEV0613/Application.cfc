component {
	this.name = hash( getCurrentTemplatePath() );
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());


 	this.datasource = server.getDatasource( "h2", server._getTempDir( "LDEV0613" ) );

	this.ormEnabled = true;
	this.ormSettings = {
		savemapping=true,
		dbcreate = 'dropcreate',
		logSQL=true
	};
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}