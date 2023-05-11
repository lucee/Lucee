component {
	this.name = hash( getCurrentTemplatePath() );
	this.ormEnabled="true";
    this.ormSettings = {
		dbcreate="update",
		cfcLocation="orm",
		savemapping=true
    };
    
	this.datasource = server.getDatasource("h2", server._getTempDir("jira3049") );
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}