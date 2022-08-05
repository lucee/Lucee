component {

	this.name = hash( getCurrentTemplatePath() );

	this.datasource = {
		class: 'org.h2.Driver'
		  , connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
	};
	
	this.ormEnabled = true;
	this.ormSettings.dbcreate = 'dropcreate';	

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}