component {
	this.name = hash( getCurrentTemplatePath() );
	this.ormEnabled="true";
    this.ormSettings = {
		dbcreate="update",
		cfcLocation="orm",
		savemapping=true
    };
    
    this.datasource = {
		class: 'org.h2.Driver'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
	};  

}