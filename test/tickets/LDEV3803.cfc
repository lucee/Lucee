component extends="org.lucee.cfml.test.LuceeTestCase" labels="administrator" {
	function beforeAll(){
		variables.mySQL = getCredentials();
		variables.adm = new Administrator('server', request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD);
	}

	function run( testResults,testBox ){
		describe("Testcase for LDEV-3803", function(){
			it( title="Create datasource for MySQL with liveTimeout and connectionTimeout", skip="#notHasMysql()#", body=function( currentSpec ){
				variables.adm.updateDatasource(
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

				local.rtn = variables.adm.getdatasource(
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
		mySQL = server.getDatasource(service="mysql", onlyConfig=true);
		return mySQL;
	}

	private boolean function notHasMysql() {
		return structCount(server.getDatasource(service="mysql", onlyConfig=true)) == 0;
	}

	function afterAll(){
		variables.adm.removeDatasource(
			dsn: 'LDEV3803',
			remoteClients: "arrayOfClients"
		);
	}
}