component{

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;	
	

    this.datasource = {
	  class: 'org.hsqldb.jdbcDriver'
		, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db2'
	};  

	// ORM settings
	this.ormEnabled = true;
	this.ormSettings = {
		autoManageSession = false
		,flushAtRequestEnd = false
		,secondaryCacheEnabled=true
	};

}