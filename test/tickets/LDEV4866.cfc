component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	variables.ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV4866" ) );
	variables.params = { a:1, b:2 };

	function run( testResults , testBox ) {
		describe( title='LDEV-4866' , body=function(){
			it( title='test query parsing, /* */-' , body=function() {
				```
				<cfquery name="test" datasource="#ds#" params="#params#">
/* */-</cfquery>
				```
			});
			it( title='test query parsing, just a - whitespace' , body=function() {
				```
				<cfquery name="test" datasource="#ds#" params="#params#">
					-
				</cfquery>
				```
			});

			it( title='test query parsing, just a -' , body=function() {
				```
				<cfquery name="test" datasource="#ds#" params="#params#">-</cfquery>
				```
			});

			it( title='test query parsing, just a / whitespace' , body=function() {
				```
				<cfquery name="test" datasource="#ds#" params="#params#">
					/
				</cfquery>
				```
			});
			it( title='test query parsing, just a /' , body=function() {
				```
				<cfquery name="test" datasource="#ds#" params="#params#">/</cfquery>
				```
			});
		});
	}

}