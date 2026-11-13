component {

	this.name = "LDEV-6208-noorm";
	this.datasources["LDEV6208h2"] = server.getDatasource( "h2", server._getTempDir( "LDEV6208noorm" ) );
	this.datasource = "LDEV6208h2";

}
