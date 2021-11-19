component {

	this.name =	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;

	msSQL = server.getDatasource("mssql");
	msSQL.storage = true;
	msSQL.class= 'com.microsoft.sqlserver.jdbc.SQLServerDriver';
	msSQL.bundleName = 'mssqljdbc4';
	msSQL.bundleVersion = '4.0.2206.100';

	this.datasources["LDEV3030_MSSQL"] = mssql;

	msSQL = server.getDatasource("mssql");
	msSQL.storage = true;
	msSQL.class: 'net.sourceforge.jtds.jdbc.Driver';
	msSQL.bundleName:'jtds';
	msSQL.bundleVersion:'1.3.1';
	msSQL.connectionString: 'jdbc:jtds:sqlserver://'&msSQL.server&':'&msSQL.port&';DATABASENAME='&msSQL.database&';sendStringParametersAsUnicode=true;SelectMethod=direct';

	this.datasources["LDEV3030_jTDS"] = mssql;
}