component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	// LDEV-6596: with storage that returns copies (Redis, Memcached, datasource) and sessionCluster=true,
	// the scope kept its init timestamp after storing, so the next request merged its own last write as
	// a "foreign" change and the stale storage copy won on equal timestamps -> writes / CSRF tokens lost

	function run( testResults, testBox ) {
		describe( "LDEV-6596: session writes with copy-returning session storage and sessionCluster=true", function() {

			it( "CSRF token generated on a session's 2nd request verifies", function() {
				expect( countLost( "token" ) ).toBe( 0 );
			});

			it( "CSRF token after structClear( session ) in an established session verifies", function() {
				expect( countLost( "clear" ) ).toBe( 0 );
			});

			it( "in-place write session.box.v = x on a session's 2nd request is kept", function() {
				expect( countLost( "write" ) ).toBe( 0 );
			});
		});
	}

	private numeric function countLost( required string scenario, numeric rounds = 20 ) {
		var uri = createURI( "LDEV6596/h2" ) & "/index.cfm";
		var lost = 0;
		for ( var i = 1; i <= arguments.rounds; i++ ) {
			var s = deserializeJSON( _InternalRequest( template: uri ).filecontent.trim() );
			var cookies = { cfid: s.cfid, cftoken: s.cftoken };
			if ( arguments.scenario == "clear" ) {
				_InternalRequest( template: uri, url: "write=a", cookies: cookies );
			}
			if ( arguments.scenario == "write" ) {
				var v = "v" & i;
				_InternalRequest( template: uri, url: "write=" & v, cookies: cookies );
				if ( _InternalRequest( template: uri, url: "read=1", cookies: cookies ).filecontent.trim() != v ) lost++;
				continue;
			}
			var r1 = _InternalRequest( template: uri, url: arguments.scenario == "clear" ? "token=1&clear=1" : "token=1", cookies: cookies );
			var data = deserializeJSON( r1.filecontent.trim() );
			var r2 = _InternalRequest( template: uri, url: "verify=" & urlEncodedFormat( data.token ), cookies: cookies );
			if ( r2.filecontent.trim() != "valid" ) lost++;
		}
		systemOutput( "LDEV-6596 #arguments.scenario#: lost #lost#/#arguments.rounds#", true );
		return lost;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
