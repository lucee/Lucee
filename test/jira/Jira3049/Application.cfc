component {
	this.name = hash( getCurrentTemplatePath() );
	this.ormEnabled="true";
    this.ormSettings = {
		dbcreate="update",
		cfcLocation="orm",
		savemapping=true
    };
    
    this.datasource = {
	  class: 'org.hsqldb.jdbcDriver'
		, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db'
	};  

}