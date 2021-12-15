component {

	this.name =	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;

	msSQL = getCredentials();

	this.datasources["LDEV3030_MSSQL"] ={
		  class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName:'mssqljdbc4'
		, bundleVersion:'4.0.2206.100'
		, connectionString: 'jdbc:sqlserver://'&msSQL.server&':'&msSQL.port&';DATABASENAME='&msSQL.database&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: msSQL.username
		, password: msSQL.password
		, storage:true
	};
	this.datasources["LDEV3030_jTDS"] = {
		  class: 'net.sourceforge.jtds.jdbc.Driver'
		, bundleName:'jtds'
		, bundleVersion:'1.3.1'
		, connectionString: 'jdbc:jtds:sqlserver://'&msSQL.server&':'&msSQL.port&';DATABASENAME='&msSQL.database&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: msSQL.username
		, password: msSQL.password
		, storage:true
	};

	private struct function getCredentials() {
		// getting the credentials from the enviroment variables
		var msSQL={};
		if(
			!isNull(server.system.environment.MsSQL_SERVER) && 
			!isNull(server.system.environment.MsSQL_USERNAME) && 
			!isNull(server.system.environment.MsSQL_PASSWORD) && 
			!isNull(server.system.environment.MsSQL_PORT) && 
			!isNull(server.system.environment.MsSQL_DATABASE)) {
			msSQL.server=server.system.environment.MsSQL_SERVER;
			msSQL.username=server.system.environment.MsSQL_USERNAME;
			msSQL.password=server.system.environment.MsSQL_PASSWORD;
			msSQL.port=server.system.environment.MsSQL_PORT;
			msSQL.database=server.system.environment.MsSQL_DATABASE;
		}
		// getting the credentials from the system variables
		else if(
			!isNull(server.system.properties.MsSQL_SERVER) && 
			!isNull(server.system.properties.MsSQL_USERNAME) && 
			!isNull(server.system.properties.MsSQL_PASSWORD) && 
			!isNull(server.system.properties.MsSQL_PORT) && 
			!isNull(server.system.properties.MsSQL_DATABASE)) {
			msSQL.server=server.system.properties.MsSQL_SERVER;
			msSQL.username=server.system.properties.MsSQL_USERNAME;
			msSQL.password=server.system.properties.MsSQL_PASSWORD;
			msSQL.port=server.system.properties.MsSQL_PORT;
			msSQL.database=server.system.properties.MsSQL_DATABASE;
		}
		return msSql;
	}

}