component extends = "org.lucee.cfml.test.LuceeTestCase" labels="mssql" {

	function beforeAll(){
		variables.uri = createURI("LDEV2423");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV2423 MSSQL", function(){
			it(title = "cfqueryparam not working with CF_SQL_FLOAT for negative exponent numbers (mssql)", skip=true, body = function(){ //skip=notHas("mssql")
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {
						Scene = "CF_SQL_FLOAT",
						db: "mssql"
					}
				);
				expect(trim(result.filecontent)).tobe("1");  // TODO this returns 0
			});

			it(title = "cfqueryparam not working with CF_SQL_NUMERIC for negative exponent numbers (mssql)", skip=notHas("mssql"), body = function(){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {
						Scene = "CF_SQL_NUMERIC",
						db: "mssql"
					}
				);
				expect(trim(result.filecontent)).tobe("1");
			});
		});

		describe( "Test case for LDEV2423 MYSQL", function(){
			it(title = "cfqueryparam not working with CF_SQL_FLOAT for negative exponent numbers (mysql)", skip=notHas("mysql"), body = function(){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {
						Scene = "CF_SQL_FLOAT",
						db: "mysql"
					}
				);
				expect(trim(result.filecontent)).tobe("1");
			});

			it(title = "cfqueryparam not working with CF_SQL_NUMERIC for negative exponent numbers (mysql)", skip=notHas("mysql"), body = function(){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {
						Scene = "CF_SQL_NUMERIC",
						db: "mysql"
					}
				);
				expect(trim(result.filecontent)).tobe("1");
			});
		});

		describe( "Test case for LDEV2423 qoq", function(){
			it(title = "round trip exponent number, qoq",body = function(){
				var q = queryNew( "id" );
				queryAddRow( q );
				var res = QueryExecute( "SELECT 1E-8 as num from q",{},{dbtype="query", result="local.result"});
				expect( res.recordCount ).toBe( 1 );
				expect( res.num ).toBe( 1E-8 );
			});

			it(title = "cfqueryparam not working with CF_SQL_FLOAT for negative exponent numbers, qoq", body = function(){
				var q = queryNew( "id" );
				queryAddRow( q );
				var res = QueryExecute(
					"SELECT 1 FROM q WHERE 1E-8 = :FloatingPoint",
					{
						FloatingPoint = {
							cfsqltype="CF_SQL_FLOAT",
							value="0.00000001"
						}
					},
					{dbtype="query", result="local.result"}
				);
				systemOutput( res, true );
				systemOutput( result, true);
				expect( res.recordCount ).toBe( 1 );
			});

			it(title = "cfqueryparam not working with CF_SQL_NUMERIC for negative exponent numbers, qoq", body = function(){
				var q = queryNew( "id" );
				queryAddRow( q );
				var res = QueryExecute(
					"SELECT 1 FROM q WHERE 1E-8 = :FloatingPoint",
					{
						FloatingPoint = {
							cfsqltype="CF_SQL_NUMERIC",
							value="0.00000001"
						}
					},
					{dbtype="query", result="local.result"}
				);
				systemOutput( res, true );
				systemOutput( result, true);
				expect( res.recordCount ).toBe( 1 );
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function notHas(db) {
		return structCount(server.getDatasource(arguments.db)) == 0;
	}
}