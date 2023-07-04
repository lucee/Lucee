component {
	this.name = createUUID();
	this.ORMenabled = true;
	this.datasource = server.getDatasource("mssql");
	this.ormSettings = {
		dbcreate = "dropcreate",
		dialect = "MicrosoftSQLServer"
	};
}