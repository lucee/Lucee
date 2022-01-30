component {
	this.name =	"LDEV304";
	this.datasource = server.getDatasource("mssql");

	function onRequestStart(){
		setting showdebugOutput=false;
	}
}
