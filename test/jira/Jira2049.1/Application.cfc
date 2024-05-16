component {

	this.name = hash( getCurrentTemplatePath() );
	
	this.datasource = server.getDatasource("h2",server._getTempDir("jira2049.1") );
	
	this.ormEnabled = true;
	this.ormSettings.flushatrequestend = false;
	this.ormSettings.autoManageSession = false;
	this.ormSettings.dbcreate = "update";
	this.ormSettings.savemapping = true;
	this.ormSettings.eventHandling = true;
	
	
	public function onRequestStart() {
		setting requesttimeout=10;
		ormReload();
	}
}