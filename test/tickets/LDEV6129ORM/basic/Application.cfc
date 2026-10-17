component {

	this.name = "LDEV-6129";
	this.datasources["LDEV6129h2"] = server.getDatasource( "h2", server._getTempDir( "LDEV6129basic" ) );
	this.datasources["LDEV6129h2"]["connectionLimit"] = 1;
	this.datasources["LDEV6129h2"]["maxTotal"] = 1;
	this.ormEnabled = true;
	this.datasource = "LDEV6129h2";
	this.ormSettings = {
		dbcreate: "dropcreate",
		dialect: "h2",
		flushAtRequestEnd: true,
		autoManageSession: true
	};

	public function onRequestStart() {
		setting requesttimeout = 10;
	}

}
