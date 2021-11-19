component {

	param name="url.appName" default="myAppOne";

	this.name = "#url.appName#";

	// any other application.cfc stuff goes below:
	this.sessionManagement	= true;
	this.mappings[ "/model" ] = getDirectoryFromPath( getCurrentTemplatePath() ) & 'model';


	// DATASOPURCE CONFIG
	dbname	= 'testdb8';
	dbpath	= expandPath("#getTempDirectory()#/data/#dbname#");

	this.datasources[dbname] = {
		  class: 'org.h2.Driver'
		, bundleName: 'org.h2'
		, bundleVersion: '1.3.172'
		, connectionString: 'jdbc:h2:#dbpath#;MODE=MySQL'
		, connectionLimit:100 // default:-1
	};


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


}