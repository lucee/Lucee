component extends="org.lucee.cfml.test.LuceeTestCase" labels="mssql" {
	function beforeAll(){
		variables.mssql= getCredentials();
	}

	function run( testResults,testBox ){
		describe("Testcase for LDEV-4555", function(){
			it( title = "checking query()", skip=true, body = function( currentSpec ){
				if ( structCount(variables.mssql) eq 0 )
					return;
				// the following dsn config used to work and now throws an error
				dsn = StructNew();
				dsn.type = "mssql";
				dsn.port =  mssql.PORT;
				dsn.database =  mssql.DATABASE
				dsn.host =  mssql.SERVER
				dsn.username = mssql.USERNAME;
				dsn.password = mssql.PASSWORD;

				query = new Query(datasource=dsn);
				sql = "SELECT 1";
				query.setSQL(sql);
				try {
					var result = query.execute().getResult().recordCount();
				}
				catch(any e) {
					var result = e.stacktrace;
				}
				expect(result).toBe(1);
			});

			it( title = "checking query()", body = function( currentSpec ){
				if ( structCount(variables.mssql) eq 0 )
					return;

				query = new Query(datasource=mssql);
				sql = "SELECT 1";
				query.setSQL(sql);
				try {
					var result = query.execute().getResult().recordCount();
				}
				catch(any e) {
					var result = e.stacktrace;
				}
				expect(result).toBe(1);
			});
		});
	}

	private struct function getCredentials() {
		return mssql = server.getDatasource(service="mssql", onlyConfig=true)
	}

}