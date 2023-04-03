/**
* Copyright Since 2005 Ortus Solutions, Corp
* www.ortussolutions.com
**************************************************************************************
*/
component {
	this.name = "LDEV1641" & hash( getCurrentTemplatePath() );
	// any other application.cfc stuff goes below:
	this.sessionManagement = true;

	// any mappings go here, we create one that points to the root called test.
	this.mappings[ "/tests" ] = getDirectoryFromPath( getCurrentTemplatePath() );	
	this.datasources["testdb"] = server.getDatasource("h2", server._getTempDir("LDEV1641") );

	// any orm definitions go here.

	this.defaultDatasource = "testdb";
	this.ormEnabled = true;
	this.ormenabled							= true;						// turnm ORM on or this application
	this.ormsettings.dbcreate				= 'update';					// valid settings: none | update | dropcreate
	//this.ormsettings.eventhandling		= true;						// activate event handing
	//this.ormsettings.eventhandler			= "ta.model.EventHandler";
	this.ormsettings.flushatrequestend		= false;					// we are going to manually commit all transactions
	this.ormsettings.automanagesession		= false;					// we are going to manually commit all transactions
	this.ormsettings.secondarycacheenabled	= false;
	this.ormsettings.cfclocation			= ['/model/'];		// CFC location (s)
	this.ormsettings.datasource				= "testdb";	// default DB for ORM
	this.ormsettings.dialect				= "mySQL";	// set in environments to support alternate databases
	this.ormsettings.useDBForMapping		= false;					// false = do not walk the db on startup trying to create ORM definitions 

	// request start
	
	public function onRequestStart() {
		setting requesttimeout=10;

		return true;
	}
}