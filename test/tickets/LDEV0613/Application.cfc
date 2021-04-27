component {
	this.name = hash( getCurrentTemplatePath() );
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());


 	this.datasource = {
	  class: 'org.h2.Driver'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
	};

	this.ormEnabled = true;
	this.ormSettings = {
		savemapping=true,
		dbcreate = 'dropcreate',
		logSQL=true
	};
}