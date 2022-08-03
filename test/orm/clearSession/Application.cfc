component {
	this.name 				= "orm" & hash( getCurrentTemplatePath() );

	this.datasource = server.getDatasource("h2", "#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db" );

	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "dropcreate"
	};

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}