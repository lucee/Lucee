component {
	this.name = "LDEV4150"
	this.ORMenabled = true;
	this.datasource = server.getDatasource("mssql");
	this.ormSettings = {
		dbcreate = "dropcreate",
		dialect = "MicrosoftSQLServer"
	};
}