component {
	this.name = "luceetest";
	this.datasources["luceedb"] = server.getDatasource("mssql");
	this.datasource = "luceedb";
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}
