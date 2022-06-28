component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.mySQL= getCredentials();
	}

	function run( testResults,testBox ){
		describe("Testcase for LDEV-1445", function(){
			it( title = "Create datasource for MySQL with default connectionLimit", body = function( currentSpec ){
				// adm = new Administrator('server', 'password');
				adm = new Administrator('server', request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD);
		            adm.updateDatasource(
			            name: 'datasource1',
			            newname: 'datasource1',
			            type: 'MYSQL',
			            host: '#mySQL.SERVER#',
			            database: #mySQL.DATABASE#,
			            port: #mySQL.PORT#,
			            username: #mySQL.USERNAME#,
			            password: #mySQL.PASSWORD#,
			            connectionTimeout: 12,
			            storage: false,
			            blob: true,
			            clob: true
		            );
		         	
		         	local.rtn = adm.getdatasource(
						name: 'datasource1'	         		
		         	);
		         	expect(local.rtn.connectionLimit).toBe(-1);
			});
		});
	}

	private struct function getCredentials() {
		// getting the credentials from the environment variables
		var mySQL={};
		if(
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
		else if(
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
			dsn: 'datasource1',
			remoteClients: "arrayOfClients"
		);
	}
}