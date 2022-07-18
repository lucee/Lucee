component {
	this.name = "LDEV2902";
	msSql = server.getDatasource("mssql");
	msSql.timezone = 'America/chicago';
	this.datasources["testTimeZone"] = msSql;
	this.datasources["testNoTimeZone"] = server.getDatasource("mssql");
	this.datasources["testemptyTimeZone"] = server.getDatasource("mssql");

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}