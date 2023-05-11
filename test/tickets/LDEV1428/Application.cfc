
component {
	this.name 	= "LDEV1428" & hash( getCurrentTemplatePath() );

	this.datasource= server.getDatasource("h2",server._getTempDir("LDEV1428") );
	
	this.ormEnabled = true;
	this.ormSettings = {
		savemapping=true,
		dbcreate = "update",
		secondarycacheenabled = false,
		logSQL 				= true,
		flushAtRequestEnd 	= false,
		autoManageSession	= false,
		skipCFCWithError	= false
	};

	public function onRequestStart() {
		setting requesttimeout=10;
	}
	
}
