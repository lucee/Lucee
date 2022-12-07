component {

	this.name = "LDEV-4185" & hash( getCurrentTemplatePath() );
	this.datasource= server.getDatasource("h2", server._getUniqueTempDir("LDEV4185") );
	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "dropcreate"
	};


	public function onRequestStart() {
		setting requesttimeout=10;
	}

}