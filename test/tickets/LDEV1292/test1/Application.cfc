component {
	this.name = "test-ldev-1292-1";
	mySQL= getCredentials();

	mysql1 = server.getDatasource("mysql");
	mysql1.custom = { useUnicode:true };
	this.datasource = mysql1;
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}