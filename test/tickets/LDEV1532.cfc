component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.uri = createURI("LDEV1532");
	}

	function afterAll() {
		cleanup();
	}

	private function cleanUp() {
		if (!notHasMssql()) {
			queryExecute( sql="DROP TABLE IF EXISTS LDEV1532", options: {
				datasource: server.getDatasource("mssql")
			}); 
		}
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1532", function() {
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=false & value is null (dbtype=query)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=1}
				);
				expect(local.result.filecontent.trim()).toBe('true');
			});
			
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=true & value is null (dbtype=query)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=2}
				);
				expect(local.result.filecontent.trim()).toBe("0");
			});
			
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=false & value is not null (dbtype=query)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=3}
				);
				expect(local.result.filecontent.trim()).toBe("1");
			});
			
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=true & value is not null (dbtype=query)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=4}
				);
				expect(local.result.filecontent.trim()).toBe('0');
			});

			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=true & value is null (datasource)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=5}
				);
				expect(local.result.filecontent.trim()).toBe('true');;
			});

			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=true & value is null (datasource)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=6}
				);
				expect(local.result.filecontent.trim()).toBe("0");
			});
			
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=false & value is not null (datasource)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=7}
				);
				expect(local.result.filecontent.trim()).toBe("1");
			});
			
			it(title = "Checking cfqueryparam with datatype cf_sql_integer, null=true & value is not null (datasource)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=8}
				);
				expect(local.result.filecontent.trim()).toBe('0');
			});
		});
	}
	private string function createURI(string calledName) {
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function notHasMssql() {
		return !structCount(server.getDatasource("mssql"));
	}
}