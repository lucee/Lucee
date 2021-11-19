component {
	this.name = "test";
	mySQL= getCredentials();

	mysql1 = server.getDatasource("mysql");
	mysql1.custom = { useUnicode:true };
	this.datasource = mysql1;

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}