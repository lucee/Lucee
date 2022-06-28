component {

	this.name	=	"LDEV3207";

    msSQL=getCredencials();
    
	this.datasources["LDEV3207"] ={
		  class: 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
		, bundleName:'mssqljdbc4'
		, bundleVersion:'4.0.2206.100'
		, connectionString: 'jdbc:sqlserver://'&msSQL.server&':'&msSQL.port&';DATABASENAME='&msSQL.database&';sendStringParametersAsUnicode=true;SelectMethod=direct'
		, username: msSQL.username
		, password: msSQL.password
	};
	this.datasource = "LDEV3207";

	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS empty_table");
        }
        query{
			echo("DROP TABLE IF EXISTS value_table");
		}
		query {
	        echo("CREATE TABLE empty_table ( id int, value varchar(20))");
        }
        query {
	        echo("CREATE TABLE value_table ( id int, value varchar(20))");
		}
		query {
	        echo("INSERT INTO empty_table VALUES ('1', ''), ('2', ''), ('3', '');");
        }
        query {
	        echo("INSERT INTO value_table VALUES ('1', 'lucee'), ('2', 'test'), ('3', 'lucee test');");
		}
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS empty_table");
        }
        query{
			echo("DROP TABLE IF EXISTS value_table");
		}
	}

	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
		var msSQL={};
		if(
			!isNull(server.system.environment.msSQL_SERVER) && 
			!isNull(server.system.environment.msSQL_USERNAME) && 
			!isNull(server.system.environment.msSQL_PASSWORD) && 
			!isNull(server.system.environment.msSQL_PORT) && 
			!isNull(server.system.environment.msSQL_DATABASE)) {
			msSQL.server=server.system.environment.msSQL_SERVER;
			msSQL.username=server.system.environment.msSQL_USERNAME;
			msSQL.password=server.system.environment.msSQL_PASSWORD;
			msSQL.port=server.system.environment.msSQL_PORT;
			msSQL.database=server.system.environment.msSQL_DATABASE;
		}
		// getting the credetials from the system variables
		else if(
			!isNull(server.system.properties.msSQL_SERVER) && 
			!isNull(server.system.properties.msSQL_USERNAME) && 
			!isNull(server.system.properties.msSQL_PASSWORD) && 
			!isNull(server.system.properties.msSQL_PORT) && 
			!isNull(server.system.properties.msSQL_DATABASE)) {
			msSQL.server=server.system.properties.msSQL_SERVER;
			msSQL.username=server.system.properties.msSQL_USERNAME;
			msSQL.password=server.system.properties.msSQL_PASSWORD;
			msSQL.port=server.system.properties.msSQL_PORT;
			msSQL.database=server.system.properties.msSQL_DATABASE;
		}
		return mssql;
	}
	


}