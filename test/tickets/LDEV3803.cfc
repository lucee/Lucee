component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.mySQL = getCredentials();
	}

	function run( testResults,testBox ){
		describe("Testcase for LDEV-3803", function(){
			it( title="Create datasource for MySQL with liveTimeout and connectionTimeout", body=function( currentSpec ){
				adm = new Administrator('server', request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD);
				adm.updateDatasource(
					name: 'LDEV3803',
					newname: 'LDEV3803',
					type: 'MYSQL',
					host: '#mySQL.SERVER#',
					database: #mySQL.DATABASE#,
					port: #mySQL.PORT#,
					username: #mySQL.USERNAME#,
					password: #mySQL.PASSWORD#,
					connectionTimeout: 15,
					liveTimeout: 720,
					storage: false,
					blob: true,
					clob: true
				);

				local.rtn = adm.getdatasource(
					name: 'LDEV3803'		
				);
				expect(local.rtn.connectionTimeout).toBe(15);
				expect(local.rtn.liveTimeout).toBe(720);
			});
		}); 
	}

	private struct function getCredentials() {
		// getting the credentials from the environment variables
		var mySQL = {};
		if (
			!isNull(server.system.environment.MYSQL_SERVER) &&
			!isNull(server.system.environment.MYSQL_USERNAME) &&
			!isNull(server.system.environment.MYSQL_PASSWORD) &&
			!isNull(server.system.environment.MYSQL_PORT) &&
			!isNull(server.system.environment.MYSQL_DATABASE)) {
			mySQL.server=server.system.environment.MYSQL_SERVER;
			mySQL.username=server.system.environment.MYSQL_USERNAME;
			mySQL.password=server.system.environment.MYSQL_PASSWORD;
			mySQL.port=server.system.environment.MYSQL_PORT;
			mySQL.database=server.system.environment.MYSQL_DATABASE;
		}
		// getting the credentials from the system variables
		else if (
			!isNull(server.system.properties.MYSQL_SERVER) &&
			!isNull(server.system.properties.MYSQL_USERNAME) &&
			!isNull(server.system.properties.MYSQL_PASSWORD) &&
			!isNull(server.system.properties.MYSQL_PORT) &&
			!isNull(server.system.properties.MYSQL_DATABASE)) {
			mySQL.server=server.system.properties.MYSQL_SERVER;
			mySQL.username=server.system.properties.MYSQL_USERNAME;
			mySQL.password=server.system.properties.MYSQL_PASSWORD;
			mySQL.port=server.system.properties.MYSQL_PORT;
			mySQL.database=server.system.properties.MYSQL_DATABASE;
		}
		return mysql;
	}

	function afterAll(){
		adm.removeDatasource(
			dsn: 'LDEV3803',
			remoteClients: "arrayOfClients"
		);
	}
}