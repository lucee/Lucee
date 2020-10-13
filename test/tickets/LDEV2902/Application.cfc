component {

	msSQL = getCredencials();

	this.name = "LDEV2902";

	this.datasources["testTimeZone"] = {
		  class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName: 'mssqljdbc4'
		, bundleVersion: '4.0.2206.100'
		, connectionString: 'jdbc:sqlserver://'&#msSQL.server#&':'&#msSQL.port#&';DATABASENAME='&#msSQL.database#&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: #msSQL.username#
		, password: #msSQL.password#
		, timezone:'America/chicago'
		
	};

	this.datasources["testNoTimeZone"] = {
		 class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName: 'mssqljdbc4'
		, bundleVersion: '4.0.2206.100'
		, connectionString: 'jdbc:sqlserver://'&#msSQL.server#&':'&#msSQL.port#&';DATABASENAME='&#msSQL.database#&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: #msSQL.username#
		, password: #msSQL.password#
		
	};

	this.datasources["testemptyTimeZone"] = {
		 class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName: 'mssqljdbc4'
		, bundleVersion: '4.0.2206.100'
		, connectionString: 'jdbc:sqlserver://'&#msSQL.server#&':'&#msSQL.port#&';DATABASENAME='&#msSQL.database#&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: #msSQL.username#
		, password: #msSQL.password#
		
	};

	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
		var msSQL={};
		if(
			!isNull(server.system.environment.MSSQL_SERVER) && 
			!isNull(server.system.environment.MSSQL_USERNAME) && 
			!isNull(server.system.environment.MSSQL_PASSWORD) && 
			!isNull(server.system.environment.MSSQL_PORT) && 
			!isNull(server.system.environment.MSSQL_DATABASE)) {
			msSQL.server=server.system.environment.MSSQL_SERVER;
			msSQL.username=server.system.environment.MSSQL_USERNAME;
			msSQL.password=server.system.environment.MSSQL_PASSWORD;
			msSQL.port=server.system.environment.MSSQL_PORT;
			msSQL.database=server.system.environment.MSSQL_DATABASE;
		}
		// getting the credetials from the system variables
		else if(
			!isNull(server.system.properties.MSSQL_SERVER) && 
			!isNull(server.system.properties.MSSQL_USERNAME) && 
			!isNull(server.system.properties.MSSQL_PASSWORD) && 
			!isNull(server.system.properties.MSSQL_PORT) && 
			!isNull(server.system.properties.MSSQL_DATABASE)) {
			msSQL.server=server.system.properties.MSSQL_SERVER;
			msSQL.username=server.system.properties.MSSQL_USERNAME;
			msSQL.password=server.system.properties.MSSQL_UPASSWORD;
			msSQL.port=server.system.properties.MSSQL_PORT;
			msSQL.database=server.system.properties.MSSQL_DATABASE;
		}
		return msSQL;
	}

}
