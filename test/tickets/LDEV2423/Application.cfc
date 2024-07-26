component {
	this.name = "ldev2403";
	this.datasources["ldev2403"] = server.getDatasource(db);
	this.datasource = "ldev2403";
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}
