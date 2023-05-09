component {
    this.name = "orm" & hash( getCurrentTemplatePath() );
    this.ORMenabled = true;

    this.ormSettings.dbcreate = "dropcreate"; 
    this.ormSettings.dialect = "H2";

    this.datasources.test = server.getDatasource( "h2", server._getTempDir( "orm-h2-dialect" ) );
    this.datasource = 'test'; 

	public function onRequestStart() {
		setting requesttimeout=10;
	}

}