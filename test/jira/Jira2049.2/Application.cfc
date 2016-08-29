component {

	this.name = hash( getCurrentTemplatePath() );
	
	this.datasource={
  		class: 'org.h2.Driver'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
	};

	this.ormEnabled = true;
	this.ormSettings.flushatrequestend = false;
	this.ormSettings.autoManageSession = false;
	this.ormSettings.dbcreate = "update";
	this.ormSettings.savemapping = true;
	this.ormSettings.eventHandling = true;
	
	public any function onRequestStart() {
		ormReload();
	}
}