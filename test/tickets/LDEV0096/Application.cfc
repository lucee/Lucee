component{

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;	
	
	this.datasource =  server.getDatasource( "h2", server._getTempDir( "LDEV0096" ) );

	// ORM settings
	this.ormEnabled = true;
	this.ormSettings = {
		autoManageSession = false
		,flushAtRequestEnd = false
		,secondaryCacheEnabled=true
	};

	public function onRequestStart() {
		setting requesttimeout=10;
	}
	
}