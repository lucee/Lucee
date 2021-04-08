<cfscript>
 	class = CreateObject("java","com.microsoft.sqlserver.jdbc.SQLServerDriver");
	res = bundleinfo(class);

	mssql = server.getDatasource("mssql");
	mssql.bundleName = res.name;
	mssql.bundleVersion = res.version;
	
	this.datasources["test_dsn_new"] = mssql;
	this.datasource = "test_dsn_new";	
</cfscript>