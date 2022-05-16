component {
    this.name = "orm" & hash( getCurrentTemplatePath() );
    this.ORMenabled = true;

    this.ormSettings.dbcreate = "dropcreate"; 
    this.ormSettings.dialect = "MicrosoftSQLServer";

    this.datasource = server.getDatasource("mssql");

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}