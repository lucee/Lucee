component {

	this.Name = "#createuuid()#";
	this.sessionManagement = false;
	this.ormEnabled = true;
	this.datasource = "LDEV2859";
	this.ormSettings = {
        dbCreate = "none",
        useDBForMapping = false,
        dialect = "MicrosoftSQLServer"
    };

	msSQL = getCredencials(); 
	this.datasources["LDEV2859"] = {
		  class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName:'mssqljdbc4'
		, bundleVersion:'4.0.2206.100'
		, connectionString: 'jdbc:sqlserver://'&#msSQL.server#&':'&#msSQL.port#&';DATABASENAME='&#msSQL.database#&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: msSQL.username
		, password: msSQL.password
		, storage:true
	};
	this.datasource = "LDEV2859";

	public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS test");
		}
		query{
			echo("CREATE TABLE test( id int, name varchar(20))");
		}
		query{
			echo("INSERT INTO test VALUES( '1', 'lucee' )");
			echo("INSERT INTO test VALUES( '2', 'railo' )");
		}
	}


	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
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
		// getting the credetials from the system variables
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

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS test");
		}
	}

}