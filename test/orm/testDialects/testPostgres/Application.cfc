component {
    this.name = "orm" & hash( getCurrentTemplatePath() );
    this.ORMenabled = true;

    this.ormSettings.dbcreate = "dropcreate"; 
    this.ormSettings.dialect = "PostgreSQL";

    this.datasource =  server.getDatasource("postgres");

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}