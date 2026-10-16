component {
	this.name = "LDEV-6003";
	this.datasource = server.getDatasource( "mssql" );
	this.ormenabled = true;
	this.ormsettings = {
		dbcreate: "none",
		dialect: "MicrosoftSQLServer",
		flushatrequestend: false,
		eventHandling: false,
		useDBForMapping: false
	};
}
