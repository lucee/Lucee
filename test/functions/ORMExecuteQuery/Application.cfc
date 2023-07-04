component {

	this.name = hash( getCurrentTemplatePath() );

	this.datasource =  server.getDatasource( "h2", server._getTempDir( "ormExecuteQuery" ) );
	
	this.ormEnabled = true;
	this.ormSettings.dbcreate = 'dropcreate';	

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}