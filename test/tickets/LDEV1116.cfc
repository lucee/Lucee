component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test suite for LDEV-1116", body=function(){
			it(title="Checking oracle query to select current Date & time", body=function(){
				var orc = getCredentials();
				var oracletestdb = {
					  class: 'oracle.jdbc.OracleDriver'
					, bundleName: 'ojdbc6'
					, bundleVersion: '11.2.0.4'
					, connectionString: 'jdbc:oracle:thin:@#orc.server#:#orc.port#/#orc.database#'
					, username: orc.username
					, password: orc.password
				};
				var Test = queryExecute("SELECT	systimestamp AS db_time FROM dual", {}, {datasource="#oracletestdb#", result="result"});
				//expect(getMetadata(Test.DB_Time[1]).getName()).toBe("java.lang.String");
				expect(Test.DB_Time[1]).toBe("java.lang.String");
			});
		});
	}

	private struct function getCredentials() {
		var orc = {};

		if(
			!isNull(server.system.environment.ORACLE_SERVER) &&
			!isNull(server.system.environment.ORACLE_USERNAME) &&
			!isNull(server.system.environment.ORACLE_PASSWORD) &&
			!isNull(server.system.environment.ORACLE_PORT) &&
			!isNull(server.system.environment.ORACLE_DATABASE)
		) {
			// getting the credentials from the environment variables
			orc.server=server.system.environment.ORACLE_SERVER;
			orc.username=server.system.environment.ORACLE_USERNAME;
			orc.password=server.system.environment.ORACLE_PASSWORD;
			orc.port=server.system.environment.ORACLE_PORT;
			orc.database=server.system.environment.ORACLE_DATABASE;
		}else if(
			!isNull(server.system.properties.ORACLE_SERVER) &&
			!isNull(server.system.properties.ORACLE_USERNAME) &&
			!isNull(server.system.properties.ORACLE_PASSWORD) &&
			!isNull(server.system.properties.ORACLE_PORT) &&
			!isNull(server.system.properties.ORACLE_DATABASE)
		){
			// getting the credetials from the system variables
			orc.server=server.system.properties.ORACLE_SERVER;
			orc.username=server.system.properties.ORACLE_USERNAME;
			orc.password=server.system.properties.ORACLE_PASSWORD;
			orc.port=server.system.properties.ORACLE_PORT;
			orc.database=server.system.properties.ORACLE_DATABASE;
		}
		return orc;
	}
}
