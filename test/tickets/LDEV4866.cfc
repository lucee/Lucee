component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	variables.ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV4866" ) );
	variables.params = { a:1, b:2 };

	function run( testResults , testBox ) {
		describe( title='LDEV-4866' , body=function(){
			it( title='test query parsing, /* */-' , body=function() {
				doQuery("#chr(13)# /* */- ");
				doQuery("#chr(13)# /* */- ");
				doQuery("/* */-");
			});
			it( title='test query parsing, just a - whitespace' , body=function() {
				doQuery("#chr(9)# - #chr(13)# ");
			});

			it( title='test query parsing, just a -' , body=function() {
				doQuery("-");
			});

			it( title='test query parsing, just a / whitespace' , body=function() {
				doQuery("#chr(9)# / #chr(13)# ");
			});
			it( title='test query parsing, just a /' , body=function() {
				doQuery("/");
			});
		});
	}

	private function doQuery(sql){
		try {
			query name="test" datasource="#ds#" params="#params#" {
				echo( sql );
			}
		} catch (e) {
			 if ( e.stackTrace.indexOf("lucee.runtime.exp.DatabaseException:") neq 0 )
				rethrow;
		}
	}

}