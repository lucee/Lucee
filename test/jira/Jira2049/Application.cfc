component {

	this.name = hash( getCurrentTemplatePath() );
	
	/*this.datasource={
  		class: 'org.h2.Driver'
	  	, bundleName: 'org.lucee.h2'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
	};*/

	mySQL=getCredencials();
	this.datasource=server.getDatasource("mysql");

	this.ormEnabled = true;
	this.ormSettings.flushatrequestend = false;
	this.ormSettings.autoManageSession = false;
	this.ormSettings.dbcreate = "dropcreate";
	this.ormSettings.savemapping = true;
	this.ormSettings.eventHandling = true;
	
	public any function onRequestStart() {
		ormReload();
	}

	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
		return server.getDatasource("mysql");
	}
}