component {
	this.name = 'LDEV4067';
	this.ORMenabled = "true";
	this.ormSettings = {
		datasource = "testH2",
		dbCreate = "dropcreate",
		useDBForMapping = false,
		dialect = "h2"
	};
	this.datasources["testH2"] = server.getDatasource("h2", "#getDirectoryFromPath(getCurrentTemplatePath())#datasource\dbh2");
}