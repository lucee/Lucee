component {
	this.name = "LDEV1214" & hash( getCurrentTemplatePath() );

	this.datasource = server.getDatasource("h2",server._getTempDir("LDEV1214") );

	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "update",
		secondarycacheenabled = false,
		flushAtRequestEnd 	= false,
		autoManageSession	= false,
		secondaryCacheEnabled = false,
		eventhandling = true
	};
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}