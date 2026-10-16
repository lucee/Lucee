component {

	this.name = "LDEV-6139" & hash( getCurrentTemplatePath() );
	this.datasource = server.getDatasource( "postgres" );
	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "dropcreate",
		flushAtRequestEnd = true,
		autoManageSession = true
	};

	public function onRequestStart() {
		setting requesttimeout=10;
	}

}
