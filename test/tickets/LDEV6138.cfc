component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm,postgres" {

	function beforeAll() {
		variables.uri = createURI( "LDEV6138" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6138 releaseConnection leaks unmanaged connections", function() {

			it( title="ORM operations inside transaction should not leak connections",
				skip="#notHasPostgres()#", body=function( currentSpec ) {
				var result = _InternalRequest(
					template: "#variables.uri#/LDEV6138.cfm"
				);
				// delta of active connections: should be 0 (no leaked connections)
				// BUG: each transaction cycle leaks one connection, so delta will be ~iterations
				expect( val( trim( result.filecontent ) ) ).toBe( 0,
					"leaked #val( trim( result.filecontent ) )# active connections — LDEV-6138" );
			});

		});
	}

	private boolean function notHasPostgres() {
		return !structCount( server.getDatasource( "postgres" ) );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
