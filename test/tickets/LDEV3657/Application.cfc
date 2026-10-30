component {

	param name="url.datasource" default="default";

	this.name = "LDEV3657" & hash( getCurrentTemplatePath() ) & url.datasource;

	variables.db = server.getDatasource( "h2", server._getTempDir( "LDEV3657" & url.datasource ) );

	if ( url.datasource == "named" ) {
		this.datasources[ "ldev3657" ] = variables.db;
		this.datasource = "ldev3657";
	}
	else {
		this.datasource = variables.db;
	}

	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate: "dropcreate",
		cfclocation: getDirectoryFromPath( getCurrentTemplatePath() ) & "orm"
	};

	public function onRequestStart() {
		setting requesttimeout=10;
	}

}
