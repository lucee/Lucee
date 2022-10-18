component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.mySQL= getCredentials();
	}

	function run( testResults,testBox ){
		describe("Testcase for LDEV-1445", function() {
			it( title = "Create datasource for MySQL with default connectionLimit", skip=isNotSupported() ,body = function( currentSpec ){
				variables.adm = new Administrator('server', request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD);
				variables.adm.updateDatasource(
					name: 'datasource1',
					newname: 'datasource1',
					type: 'MYSQL',
					host: '#mySQL.SERVER#',
					database: '#mySQL.DATABASE#',
					port: '#mySQL.PORT#',
					username: '#mySQL.USERNAME#',
					password: '#mySQL.PASSWORD#',
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
		mysql = server.getDatasource(service="mysql", onlyConfig=true);
		return mysql;
	}

	private function isNotSupported() {
		variables.mysql=getCredentials();
		if(!isNull(variables.mysql.server)) {
			variables.supported=true;
		}
		else
			variables.supported=false;

		return !variables.supported;
	}

	function afterAll(){
		if(!structKeyExists(variables, "adm")) return; 
		variables.adm.removeDatasource(
			dsn: 'datasource1',
			remoteClients: "arrayOfClients"
		);
	}
}