component {

	this.name = "LDEV-6208-orm";
	this.datasources["LDEV6208h2"] = server.getDatasource( "h2", server._getTempDir( "LDEV6208orm" ) );
	this.ormEnabled = true;
	this.datasource = "LDEV6208h2";
	this.ormSettings = {
		dbcreate: "dropcreate",
		dialect: "h2",
		flushAtRequestEnd: false,
		autoManageSession: false
	};

}
