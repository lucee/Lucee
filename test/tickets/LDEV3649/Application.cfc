component {
    this.name = "LDEV3649";
    this.ormEnabled = true;

    this.datasource = server.getDatasource( "h2", server._getTempDir( "LDEV3649" ) );
    
    this.ormEnabled = true;
    this.ormSettings = {
        dbcreate = "dropcreate"
    };
    
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}