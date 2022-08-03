component {
    this.name = "orm" & hash( getCurrentTemplatePath() );
    this.ORMenabled = true;

    this.ormSettings.dbcreate = "dropcreate"; 
    this.ormSettings.dialect = "mysql";

    this.datasource = server.getDatasource("mysql");

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}