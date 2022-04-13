component extends="org.lucee.cfml.test.LuceeTestCase" labels="h2,orm" {
	function beforeAll(){
		variables.uri = createURI( "LDEV3659" );
		cleanup("mssql");
		cleanup("h2");
	}

	function afterAll(){
		cleanup("mssql");
		cleanup("h2");
	}

	private function cleanUp(db){
		if (!isDatasourceNotConfigured( arguments.db )){
			queryExecute(sql="DROP TABLE IF EXISTS Persons",
				options: {
					datasource: getDatasource( arguments.db )
			});
		}
	}
	
	function run( testResults, testBox ) {
		describe("Second Testcase for LDEV-3659", function() {
			it( title="LDEV-3659 -- mixed transactions with ORM and cfquery H2", skip="#isDatasourceNotConfigured('h2')#", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/index.cfm",
					form: {
						dbfile: dbfile,
						db: "h2"
					}
				);
				expect( trim( result.filecontent ) ).toBe( "Michael Born" );
			});
			it( title="LDEV-3659 -- transactions with ORM and cfquery MSSQL", skip="#isDatasourceNotConfigured('mssql')#", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/index.cfm",
					form: {
						dbfile: dbfile,
						db: "mssql"
					}
				);
				expect( trim( result.filecontent ) ).toBe( "Michael Born" );
			});
		});
	}

	private struct function getDatasource( required string db ) {
		switch ( arguments.db ){
			case "h2":
				if (! structKeyExists( variables, "dbfile" ) )
					variables.dbfile = "#getDirectoryFromPath( getCurrentTemplatePath() )#/datasource/db";
				return server.getDatasource( "h2", variables.dbfile );
				break;
			case "mssql":
				return server.getDatasource("mssql");
				break;
			default:
				throw "db #arguments.db# no yet supported";
		}
	}
	
	private boolean function isDatasourceNotConfigured( db ){
		return !structCount( getDatasource( arguments.db ) );
	} 

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}