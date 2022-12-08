component {
	this.name 				= "LDEV1370" & hash( getCurrentTemplatePath() );

	this.datasource = server.getDatasource(service="h2", dbFile=server._getTempDir("LDEV1370"), connectionString=";MVCC=true" );	

	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "update",
		secondarycacheenabled = false,
		//flushAtRequestEnd 	= true,
		//autoManageSession	= true,
		secondaryCacheEnabled = false,
		eventhandling = true
	};

	if(!isNull(url.flushAtRequestEnd)) this.ormSettings.flushAtRequestEnd=url.flushAtRequestEnd;
	if(!isNull(url.autoManageSession)) this.ormSettings.autoManageSession=url.autoManageSession;
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}
}