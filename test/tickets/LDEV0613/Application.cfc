component {
	this.name = hash( getCurrentTemplatePath() );
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());


 	this.datasource = {
	  class: 'org.hsqldb.jdbcDriver'
	, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db'
	};

	this.ormEnabled = true;
	this.ormSettings = {
		savemapping=true,
		dbcreate = 'dropcreate',
		logSQL=true
	};
}