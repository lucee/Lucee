component {
	this.name = "test-ldev-1292-2";
	mySQL= getCredentials();
	mysql.custom = { useUnicode:true };
	this.datasource = mysql;
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}