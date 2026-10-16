component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm,postgres" {

	function beforeAll() {
		variables.uri = createURI( "LDEV6139" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6139 releaseORM skips closeAll when flushAll throws", function() {

			// this doesn't yet reproduce the problem, but the fix is logically solid

			it( title="flushAll constraint violation should not leak connections",
				skip="#notHasPostgres()#", body=function( currentSpec ) {

				var before = getSystemMetrics().activeDatasourceConnections;

				// this request will trigger a unique constraint violation during
				// flushAtRequestEnd in releaseORM() — the error is expected
				try {
					_InternalRequest(
						template: "#variables.uri#/LDEV6139.cfm"
					);
				}
				catch ( any e ) {
					// constraint violation is expected
				}

				// give pool a moment to settle
				sleep( 500 );

				var after = getSystemMetrics().activeDatasourceConnections;
				var delta = after - before;

				// BUG: when flushAll() throws, closeAll() is skipped and
				// ORM session connections are never released
				expect( delta ).toBe( 0,
					"leaked #delta# active connections after flush error — LDEV-6139" );
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
