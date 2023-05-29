component {
	this.name = 'LDEV3768';
	this.ORMenabled = "true";
	this.ormSettings = {
		datasource = "testH2",
		dbCreate = "dropcreate",
		useDBForMapping = false,
		dialect = "h2"
	};
	this.datasources["testH2"] = server.getDatasource("h2", server._getTempDir("LDEV3768"));

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}