component {

	this.name = hash( getCurrentTemplatePath() );
	
	this.datasource = server.getDatasource("mysql");

	this.ormEnabled = true;
	this.ormSettings.flushatrequestend = false;
	this.ormSettings.autoManageSession = false;
	this.ormSettings.dbcreate = "dropcreate";
	this.ormSettings.savemapping = true;
	this.ormSettings.eventHandling = true;
	
	public function onRequestStart() {
		setting requesttimeout=10;
		ormReload();
	}
}