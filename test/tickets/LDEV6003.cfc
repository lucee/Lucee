component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function beforeAll() {
		variables.uri = createURI( "LDEV6003" );
		// Pre-cleanup so we can inspect data afterwards if test fails
		if ( !notHasMssql() ) {
			queryExecute( sql="DROP TABLE IF EXISTS LDEV6003", options: {
				datasource: server.getDatasource( "mssql" )
			});
			queryExecute( sql="
				CREATE TABLE LDEV6003 (
					pk int IDENTITY(1,1) NOT NULL,
					recordDate datetime DEFAULT (getdate()),
					someInt int NULL,
					someDate datetime NULL,
					PRIMARY KEY (pk)
				)
			", options: {
				datasource: server.getDatasource( "mssql" )
			});
		}
	}

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-6003", function() {

			// This test only fails with Ortus ORM extension, passes with the older Lucee ORM
			it( title="entitySave with null datetime property should not throw cast error", skip="#notHasMssql()#", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template: "#uri#/test.cfm"
				);
				expect( result.filecontent.trim() ).toBe( "success" );
			});

		});
	}

	private function notHasMssql() {
		return structCount( server.getDatasource( "mssql" ) ) == 0 || structCount( server.getTestService( "orm" ) ) == 0;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
