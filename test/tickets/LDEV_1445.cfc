component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {
	function beforeAll(){
		variables.mySQL= getCredentials();
	}

	function run( testResults,testBox ){
		describe("Testcase for LDEV-1445", function(){
			it( title = "Create datasource for MySQL with default connectionLimit", body = function( currentSpec ){
				if ( structCount(variables.mySQL) eq 0 ) 
					return;
				// adm = new Administrator('server', 'password');
				adm = new Administrator('server', request.SERVERADMINPASSWORD?:server.SERVERADMINPASSWORD);
					adm.updateDatasource(
						name: 'datasource1445',
						newname: 'datasource1445',
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
						name: 'datasource1445'
				 	);
				 	expect(local.rtn.connectionLimit).toBe(-1);
			});
		});
	}

	private struct function getCredentials() {
		return server.getDatasource( service="mysql", onlyConfig=true );
	}

	function afterAll(){
		if(!isNull(adm)) {
			adm.removeDatasource(
				dsn: 'datasource1445',
				remoteClients: "arrayOfClients"
			);
		}
	}
}