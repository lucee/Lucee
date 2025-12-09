component {

	this.name = "LDEV5964-session-test";

	// Get MySQL credentials and configure datasource with maxTotal=1
	creds = server.getDatasource( "mysql" );
	creds.storage = true;
	creds.maxTotal = 1;
	creds.maxWaitMillis = 5000; // 5 second timeout to detect deadlock

	this.datasources[ "LDEV5964_ds" ] = creds;

	// Use the datasource with maxTotal=1 for session storage
	this.sessionManagement = true;
	this.sessionTimeout = createTimeSpan( 0, 0, 1, 0 ); // 1 minute
	this.sessionStorage = "LDEV5964_ds";

	public function onRequestStart() {
		setting requesttimeout=3 showdebugOutput=false;
	}

}
