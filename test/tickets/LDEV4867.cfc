component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	variables.ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV4867" ) );
	variables.params = { a:1, b:2 };

	function run( testResults , testBox ) {
		describe( title='LDEV-4867' , body=function(){
			it( title='test query parsing, removing comments' , body=function() {
				local.result = doQuery("
					-- foo
					/* bar */
						SELECT 'test'
				");
				//systemOutput( local.result.sql, true );
				expect ( local.result.sql ).toInclude( "-- foo" );
				expect ( local.result.sql ).toInclude( "/* bar */" );
			});

			it( title='test query parsing, with a ? in a comment' , body=function() {
				local.result = doQuery("
					-- foo
					/* bar? */
						SELECT 'test'
				");
				//systemOutput( local.result.sql, true );
				expect ( local.result.sql ).toInclude("-- foo");
				expect ( local.result.sql ).toInclude("/* bar? */");
			});

			it( title='test query parsing, with a ? in a comment' , body=function() {
				local.result = doQuery("-- foo
/* bar? */
SELECT 'test'");
				//systemOutput( local.result.sql, true );
				expect ( local.result.sql ).toInclude("-- foo");
				expect ( local.result.sql ).toInclude("/* bar? */");
			});

			it( title='test query parsing, with a ? in a comment' , body=function() {
				local.result = doQuery("-- foo ? :do
/* bar? :*/
SELECT 'test'");
				//systemOutput( local.result.sql, true );
				expect ( local.result.sql ).toInclude("-- foo ? :do");
				expect ( local.result.sql ).toInclude("/* bar? :*/");
			});
		});
	}

	private function doQuery( sql ){
		try {
			query name="local.test" datasource="#ds#" params="#params#" dbtype="parseOnly" result="local.result" {
				echo( sql );
			}
		} catch (e) {
			 if ( e.stackTrace.indexOf("lucee.runtime.exp.DatabaseException:") neq 0 )
				rethrow;
			systemOutput(e.stackTrace, true);
		}
		return result;
	}

}