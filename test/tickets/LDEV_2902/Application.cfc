component {
	this.name = 'LDEV2902';

	this.datasources["testTimezone"] = server.getDatasource( service="h2", 
		dbFile=server._getTempDir( "LDEV2902-testTimeZone" ), 
		options={ timezone: 'America/Chicago'}
	);

	this.datasources["testNoTimezone"] = server.getDatasource( service="h2", 
		dbFile=server._getTempDir( "LDEV2902-testNoTimeZone" )
	);

	this.datasources["testEmptyTimezone"] = server.getDatasource( service="h2",
		dbFile=server._getTempDir( "LDEV2902-testEmptyTimeZone" ), 
		options={ timezone: ''}
	);
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}