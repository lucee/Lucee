component {
	this.name = "LDEV-4339";
	this.datasources["ldev4339"] = server.getDatasource( "h2", server._getTempDir( "LDEV4339" ) );
	this.ormenabled = true;

	param name="url.autoManageSession" default="false";
	param name="url.flushAtRequestEnd" default="false";

	this.ormsettings = {
		dbcreate="dropCreate",
		datasource="ldev4339",
		autoManageSession = url.autoManageSession,
		flushAtRequestEnd = url.flushAtRequestEnd
	}
}