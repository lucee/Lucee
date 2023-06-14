component extends="org.lucee.cfml.test.LuceeTestCase" labels="mssql" {
	function beforeAll(){
		variables.mssql= getCredentials();
	}

	function run( testResults,testBox ){
		describe("Testcase for LDEV-4555", function(){
			it( title = "checking query()", body = function( currentSpec ){
				if ( structCount(variables.mssql) eq 0 ) 
					return;

					dsn = StructNew();
					dsn.type = "mssql";
					dsn.port = 1433;
					dsn.database = "luceetestdb";
					dsn.host = "localhost";
					dsn.dbType = "sqlserver";
					dsn.username = mssql.USERNAME;
					dsn.password = mssql.PASSWORD;
					dsn.class =  mssql.CLASS;
					query = new Query(datasource=dsn);
					sql = "SELECT 1";
					query.setSQL(sql);
					try {
						var result = query.execute().getResult().recordCount();
					}
					catch(any e) {
						var result = e.message;
					}
				 	expect(result).toBe(1);
			});
		});
	}

	private struct function getCredentials() {
		return mssql = server.getDatasource("mssql");
	}

}