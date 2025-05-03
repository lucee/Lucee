component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	variables.ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV4867" ) );

	function run( testResults , testBox ) {
		describe( title='LDEV-4867' , body=function(){
			it( title='test query parsing, removing comments' , body=function() {
				```
				<cfquery name="local.test" datasource="#ds#" result="local.result">
					-- foo
					/* bar */
						SELECT 'test'
				</cfquery>
				```
				//systemOutput( local.result.sql, true );
				expect ( local.result.sql ).toInclude( "-- foo" );
				expect ( local.result.sql ).toInclude( "/* bar */" );
			});

			it( title='test query parsing, with a ? in a comment' , body=function() {
				```
				<cfquery name="local.test" datasource="#ds#" result="local.result">
					-- foo
					/* bar? */
						SELECT 'test'
				</cfquery>
				```

				//systemOutput( local.result.sql, true );
				expect ( local.result.sql ).toInclude("-- foo");
				expect ( local.result.sql ).toInclude("/* bar? */");
			});
		});
	}

}