component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	// LDEV-XXXX: with cache-backed session storage and sessionCluster=true, a CSRF token
	// generated after structClear( session ) was not persisted

	function run( testResults, testBox ) {
		describe( "LDEV-XXXX: CSRF token after structClear( session ) with cache session storage", function() {

			it( "token generated in a new session (onSessionStart clears session) verifies, sessionCluster=true", function() {
				expect( countLost( "cluster", false ) ).toBe( 0 );
			});

			it( "token generated after structClear( session ) in an existing session verifies, sessionCluster=true", function() {
				expect( countLost( "cluster", true ) ).toBe( 0 );
			});

			it( "control: sessionCluster=false", function() {
				expect( countLost( "nocluster", false ) ).toBe( 0 );
				expect( countLost( "nocluster", true ) ).toBe( 0 );
			});
		});
	}

	private numeric function countLost( required string app, required boolean clearInRequest, numeric rounds = 20 ) {
		var uri = createURI( "LDEVXXXX/" & arguments.app );
		var lost = 0;
		for ( var i = 1; i <= arguments.rounds; i++ ) {
			var cookies = {};
			if ( arguments.clearInRequest ) {
				var s = deserializeJSON( _InternalRequest( template: "#uri#/index.cfm" ).filecontent.trim() );
				cookies = { cfid: s.cfid, cftoken: s.cftoken };
			}
			var r1 = _InternalRequest( template: "#uri#/index.cfm", url: arguments.clearInRequest ? "token=1&clear=1" : "token=1", cookies: cookies );
			var data = deserializeJSON( r1.filecontent.trim() );
			var r2 = _InternalRequest( template: "#uri#/index.cfm", url: "verify=" & urlEncodedFormat( data.token ), cookies: { cfid: data.cfid, cftoken: data.cftoken } );
			if ( r2.filecontent.trim() != "valid" ) lost++;
		}
		systemOutput( "LDEV-XXXX #arguments.app# clear=#arguments.clearInRequest#: lost #lost#/#arguments.rounds#", true );
		return lost;
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
