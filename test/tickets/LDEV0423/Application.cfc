component {

	this.datasources.test = server.getDatasource( "h2", server._getTempDir( "LDEV0423" ) );

	this.name = "LDEV0423";
	this.datasource = "test";
	this.ormEnabled = true;
	this.ormSettings = {
    	dbcreate: "dropcreate",
      	logSQL=true
   	};
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}
}
