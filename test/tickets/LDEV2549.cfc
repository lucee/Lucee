component extends = "org.lucee.cfml.test.LuceeTestCase" labels="mssql" {

	function beforeAll() {
		variables.uri = createURI("LDEV2549");
	}

	function run( testResults , testBox ) {
		describe( title="test suite for LDEV2549",skip=isNotSupported("mssql"), body=function() {
			it(title = "query param without CFSQLTYPE='cf_sql_date' for date type value (mssql)", skip=true, body = function( currentSpec ) {
				// query fails trying to convert {ts '1996-10-27 00:00:00'} to a date time
				local.result = _InternalRequest(
					template : "#uri#/LDEV2549.cfm",
					forms:	{scene: "varchar", db: "mssql"}
				);
				expect(1).toBe(result.filecontent);
			});

			it(title = "query param with CFSQLTYPE='cf_sql_date' for date type value (mssql)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2549.cfm",
					forms:	{scene: "date", db: "mssql"}
				);
				expect(2).toBe(local.result.filecontent);
			});
		});

		describe( title="test suite for LDEV2549",skip=isNotSupported("mysql"), body=function() {
			it(title = "query param without CFSQLTYPE='cf_sql_date' for date type value (mysql)", skip=true, body = function( currentSpec ) {
				// query fails trying to convert {ts '1996-10-27 00:00:00'} to a date time
				local.result = _InternalRequest(
					template : "#uri#/LDEV2549.cfm",
					forms:	{scene: "varchar", db: "mysql"}
				);
				expect(1).toBe(result.filecontent);
			});

			it(title = "query param with CFSQLTYPE='cf_sql_date' for date type value (mysql)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/LDEV2549.cfm",
					forms:	{scene: "date", db: "mysql"}
				);
				expect(2).toBe(local.result.filecontent);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function isNotSupported( db ) {
		// getting the credentials from the environment variables
		return ( structCount( server.getDatasource( arguments.db) ) eq 0 );
	}
}