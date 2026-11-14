component {

	this.name = "LDEV-6138" & hash( getCurrentTemplatePath() );
	this.datasource = server.getDatasource( "postgres" );
	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "dropcreate"
	};

	public function onRequestStart() {
		setting requesttimeout=10;
	}

}
