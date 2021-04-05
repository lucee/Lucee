component{

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;	
	

    this.datasource = {
	  class: 'org.h2.Driver'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db2;MODE=MySQL'
	}; 

	// ORM settings
	this.ormEnabled = true;
	this.ormSettings = {
		autoManageSession = false
		,flushAtRequestEnd = false
		,secondaryCacheEnabled=true
	};

}