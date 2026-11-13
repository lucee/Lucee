component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function beforeAll() {
		server.LDEV3320_ended = {};
	}

	function afterAll() {
		structDelete( server, "LDEV3320_ended" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3320 sessions must not hold the Application.cfc instance", function() {

			for ( var sessionType in [ "cfml", "jee" ] ) {

				it( title="#sessionType# session does not reference the Application.cfc instance", data={ sessionType: sessionType }, body=function( data ) {
					var result = _request( data.sessionType, "inspect.cfm" );
					expect( result.sessionId ).notToBeEmpty();
					expect( result.componentFields ).toBeEmpty( "session holds a component: " & arrayToList( result.componentFields ) );
				});

				it( title="#sessionType# session - onSessionEnd is called with a fresh instance on sessionInvalidate()", data={ sessionType: sessionType }, body=function( data ) {
					var result = _request( data.sessionType, "invalidate.cfm" );
					expect( result.ended ).notToBeEmpty( "onSessionEnd was not called" );
					expectSessionEnded( result.ended, data.sessionType, result.applicationName );
				});

				it( title="#sessionType# session - onSessionEnd is called with a fresh instance on session timeout", data={ sessionType: sessionType }, body=function( data ) {
					var result = _request( data.sessionType, "inspect.cfm" );
					var sessionId = result.sessionId;
					var applicationName = result.applicationName;
					expect( structKeyExists( server.LDEV3320_ended, sessionId ) ).toBeFalse();

					sleep( 1001 );
					admin
						action="purgeExpiredSessions"
						type="server"
						password="#request.SERVERADMINPASSWORD#";

					expect( structKeyExists( server.LDEV3320_ended, sessionId ) ).toBeTrue( "onSessionEnd was not called for [#sessionId#]" );
					expectSessionEnded( server.LDEV3320_ended[ sessionId ], data.sessionType, applicationName );
				});
			}
		});
	}

	private function expectSessionEnded( required struct ended, required string sessionType, required string applicationName ) {
		expect( arguments.ended.calls ).toBe( 1, "onSessionEnd should run exactly once" );
		expect( arguments.ended.hasSessionData ).toBeTrue( "SessionScope argument is missing the session data" );
		expect( arguments.ended.applicationName ).toBe( arguments.applicationName );
		expect( arguments.ended.applicationMarker ).toBe( "application-scope-#arguments.sessionType#" );
		expect( arguments.ended.thisMarker ).toBe( "this-scope-#arguments.sessionType#" );
	}

	private struct function _request( required string sessionType, required string template ) {
		var result = _InternalRequest(
			template: "#createURI( "LDEV3320" )#/#arguments.sessionType#/#arguments.template#"
		);
		expect( len( result.filecontent ) ).toBeGT( 0 );
		return deserializeJSON( result.filecontent );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
