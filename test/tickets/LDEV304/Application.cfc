component {
	this.name =	"LDEV304";
	this.datasource = server.getDatasource("mssql");

	
	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}
}
