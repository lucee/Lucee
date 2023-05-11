component {
	this.name = "LDEV-4056";
	
	dbDef = server.getdatasource("mysql");
	dbDef.requestExclusive ="true";
	this.datasources["testMysql"] = dbDef;

	this.datasource ="testMysql";
}