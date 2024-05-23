component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" skip=true {

	variables.ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV4866" ) );

	function run( testResults , testBox ) {
		describe( title='LDEV-4866' , body=function(){
			it( title='test query parsing, just a -' , body=function() {
				```
				<cfquery name="test" datasource="#ds#">
					-
				</cfquery>
				```
			});

			it( title='test query parsing, just a /' , body=function() {
				```
				<cfquery name="test" datasource="#ds#">
					/
				</cfquery>
				```
			});
		});
	}

}