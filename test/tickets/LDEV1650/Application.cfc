component {
	this.name = createUUID();
	mySQL= getCredentials();
	
	this.datasources["testdb"] = mySQL;
	
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

	try{
		cfdbinfo( type="Version", datasource="testdb", name="info" );
		result = info.RecordCount ;
	} catch (any e){
		result = e.message;
	}
	writeoutput(result); 

}