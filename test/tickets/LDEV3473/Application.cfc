component {
    this.name = "LDEV-3473";
    this.sessionManageMent = true;
	this.sessioncluster = true;
	this.sessionStorage = "testH2";

    this.datasources["testh2"] = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDE3473" ), 
		options={
			connectionLimit:100 // default:-1
			, liveTimeout:60 // default: -1; unit: minutes
			, storage:true // default: false
			, validate:false // default: false
		} 
	);
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}
}