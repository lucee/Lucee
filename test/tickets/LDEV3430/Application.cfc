component {
	this.name = 'LDEV2902';
	this.ORMenabled = "true";
	this.ORMsettings = {
		datasource = "LDEV3430",
		dbCreate = "dropCreate",
		dialect = " MySQL"
	}
	this.datasource = "LDEV3430";
	this.datasources["LDEV3430"] = server.getDatasource( "h2", server._getTempDir( "LDEV3430" ) );
		
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}
