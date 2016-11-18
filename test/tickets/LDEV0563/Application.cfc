component {
	this.name = hash( getCurrentTemplatePath() ) & "1";
	request.baseURL = "http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath = GetDirectoryFromPath(getCurrentTemplatePath());


	this.datasources = {
		DSN1 = {
			class: 'org.hsqldb.jdbcDriver'
		, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource1/db'
		},
		DSN2 = {
			class: 'org.hsqldb.jdbcDriver'
		, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource1/db'
		}
	};

	this.datasource = "DSN1";

	this.ormEnabled = true;
	this.ormSettings = {
		savemapping=true,
		dbcreate = 'dropcreate',
		logSQL=true
	};
}