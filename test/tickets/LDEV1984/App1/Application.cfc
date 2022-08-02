component {

	param name="url.appName" default="AppOne";

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