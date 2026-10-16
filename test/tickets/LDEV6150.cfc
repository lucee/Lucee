component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-6150 - HTTP connection pool stats in getSystemMetrics()", function() {

			it( title="getSystemMetrics returns HTTP pool totals", body=function() {
				// hit a few different hosts to populate the pool
				cfhttp( url="https://update.lucee.org", method="head", timeout=10, result="local.res" );
				cfhttp( url="https://cdn.lucee.org", method="head", timeout=10, result="local.res" );
				cfhttp( url="https://ext.lucee.org", method="head", timeout=10, result="local.res" );

				var metrics = getSystemMetrics();

				expect( metrics ).toHaveKey( "activeHttpConnections" );
				expect( metrics ).toHaveKey( "idleHttpConnections" );
				expect( metrics ).toHaveKey( "waitingForHttpConn" );
				expect( metrics ).toHaveKey( "maxHttpConnections" );
				expect( metrics ).toHaveKey( "httpConnections" );

				expect( metrics.activeHttpConnections ).toBeNumeric();
				expect( metrics.idleHttpConnections ).toBeNumeric();
				expect( metrics.waitingForHttpConn ).toBeNumeric();
				expect( metrics.maxHttpConnections ).toBeGTE( 1 );
			});

			it( title="getSystemMetrics returns per-route HTTP breakdown", body=function() {
				// hit a few different hosts to populate the pool
				cfhttp( url="https://update.lucee.org", method="head", timeout=10, result="local.res" );
				cfhttp( url="https://cdn.lucee.org", method="head", timeout=10, result="local.res" );
				cfhttp( url="https://ext.lucee.org", method="head", timeout=10, result="local.res" );

				var metrics = getSystemMetrics();
				var routes = metrics.httpConnections;

				expect( structCount( routes ) ).toBeGTE( 3, "Should have at least 3 routes after hitting 3 hosts" );

				for ( var key in routes ) {
					var route = routes[ key ];
					expect( route ).toHaveKey( "activeHttpConnections" );
					expect( route ).toHaveKey( "idleHttpConnections" );
					expect( route ).toHaveKey( "waitingForHttpConn" );
					expect( route ).toHaveKey( "max" );
					expect( route ).toHaveKey( "route" );
					expect( route.max ).toBeGTE( 1 );
				}
			});

			it( title="idle connections increase after pooled requests", body=function() {
				// make pooled requests to multiple hosts
				cfhttp( url="https://update.lucee.org", method="head", timeout=10, pooling=true, result="local.res" );
				cfhttp( url="https://cdn.lucee.org", method="head", timeout=10, pooling=true, result="local.res" );
				cfhttp( url="https://ext.lucee.org", method="head", timeout=10, pooling=true, result="local.res" );

				var metrics = getSystemMetrics();
				// after requests complete, connections should be idle in the pool
				expect( metrics.idleHttpConnections ).toBeGTE( 3, "Should have at least 3 idle HTTP connections after pooled requests to 3 hosts" );
			});
		});
	}

}
