component {

	param name="url.appName" default="AppOne";

	this.name = "#url.appName#";

	// any other application.cfc stuff goes below:
	this.sessionManagement	= true;
	this.mappings[ "/model" ] = getDirectoryFromPath( getCurrentTemplatePath() ) & 'model';


	// DATASOURCE CONFIG
	dbname	= 'testdb8';

	this.datasources[dbname] = server.getDatasource( "h2", server._getTempDir( "LDEV1984" ) );

	// ORM CONFIG
	this.defaultDatasource					= "#dbname#";
	this.ormenabled							= true;						// turnm ORM on or this application
	this.ormsettings.dbcreate				= 'update';					// valid settings: none | update | dropcreate
	this.ormsettings.datasource				= "#dbname#";				// default DB for ORM
	this.ormsettings.dialect				= "mysql";
	this.ormsettings.cfclocation			= ['/model/'];

	if(url.AppName EQ "AppTwo"){
		this.ormsettings.secondarycacheenabled	= true;						// use secondary cache
		this.ormsettings.cacheProvider			= 'ehcache';
	}
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}