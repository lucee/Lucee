component {

	param name="url.appName" default="myAppOne";

	this.name = "#url.appName#";

	// any other application.cfc stuff goes below:
	this.sessionManagement	= true;
	this.mappings[ "/model" ] = getDirectoryFromPath( getCurrentTemplatePath() ) & 'model';


	// DATASOURCE CONFIG
	dbname	= 'testdb8';

	this.datasources[dbname] = server.getDatasource( "h2", server._getTempDir( "LDEV1659" ) );


	// ORM CONFIG
	this.defaultDatasource					= "#dbname#";
	this.ormenabled							= true;						// turnm ORM on or this application
	this.ormsettings.dbcreate				= 'update';					// valid settings: none | update | dropcreate
	this.ormsettings.flushatrequestend		= false;					// we are going to manually commit all transactions
	this.ormsettings.automanagesession		= false;					// we are going to manually commit all transactions
	this.ormsettings.secondarycacheenabled	= true;						// use secondary cache
	this.ormsettings.cacheProvider			= 'ehcache';				// ehCache for now
	this.ormsettings.cfclocation			= ['/model/'];				// CFC location (s)
	this.ormsettings.datasource				= "#dbname#";				// default DB for ORM
	this.ormsettings.dialect				= "mysql";
	this.ormsettings.useDBForMapping		= false;					// false = do not walk the db on startup trying to create ORM definitions

	public function onRequestStart() {
		setting requesttimeout=10;
	}

}