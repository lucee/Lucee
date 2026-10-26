component {
	this.name = "LDEV-1569-ormh2-#url.nullSupport#";
	this.nullSupport = url.nullSupport;
	this.datasource = server.getDatasource( "h2", server._getTempDir( "LDEV1569-#url.nullSupport#" ) );
	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate: "dropcreate",
		dialect: "h2"
	};
}
