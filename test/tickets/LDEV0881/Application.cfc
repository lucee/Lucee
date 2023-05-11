component {
	this.name 				= "LDEV881" & hash( getCurrentTemplatePath() );

	this.datasource = server.getDatasource( "h2", server._getTempDir( "LDEV0881" ) );

	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "dropcreate"
	};
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}
}