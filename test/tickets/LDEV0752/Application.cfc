component {

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;

	mySQL = getCredentials();
	request.has = ( len(mySQL) gt 0 );
	
	if(request.has) {
		this.datasource = mySQL;

		// ORM settings
		this.ormEnabled = true;
		this.ormSettings = {
			// dialect = "MySQLwithInnoDB",
			dbcreate="dropcreate"
		};
	}


	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}


	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}