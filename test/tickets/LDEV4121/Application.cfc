component {
	this.name = 'LDEV4121';
	this.ORMenabled = "true";
	this.ormSettings = {
		datasource = "testH2",
		dbCreate = "dropcreate",
		useDBForMapping = false,
		dialect = "h2"
	};
	this.datasources["testH2"] = server.getDatasource("h2", server._getUniqueTempDir("LDEV4121") );
	this.datasource = "testH2";
}