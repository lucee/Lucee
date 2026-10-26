component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function beforeAll(){
		_reset();
	}

	function afterAll(){
		//systemOutput("ended sessionids:" & structKeyList(server.LDEV3478_endedSessions), true);
		structDelete(server, "LDEV3478");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV3478 - cfml sessions ", function() {

			it( title='cfml session - onSessionEnd with sessionRotate()', body=function( currentSpec ) {
				_reset('cfml');
				var uri = createURI("LDEV3478");
				var cfmlSessionId = _InternalRequest(
					template : "#uri#/cfml_session_rotate/test_cfml_sessionend.cfm"
				);
				_dumpSessions("before");
				_dumpResult( cfmlSessionId );
				expect( len( cfmlSessionId.fileContent ) ).toBeGT( 0 );

				var _cookies = _getCookies( cfmlSessionId, "cfid" );
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
				var appName = listFirst( trim( cfmlSessionId.fileContent ), '-' ) & "-";
				expect( _getSessionCount( appName ) ).toBe( 1 );
				// allow session to expire

				sleep( 1001 );
				admin
					action="purgeExpiredSessions"
					type="server"
					password="#request.SERVERADMINPASSWORD#";

				// let's check first that the session actually ended!
				expect( _getSessionCount( appName ) ).toBe( 0 );
				expect( structKeyExists( server.LDEV3478.ended_CFML_Sessions, trim( cfmlSessionId.fileContent ) ) ).toBeTrue();
			});

			it( title='LDEV-5271 cfml session - onSessionEnd with sessionRotate() in onSessionStart', body=function( currentSpec ) {
				_reset('cfml');
				var uri = createURI("LDEV3478");
				var cfmlSessionId = _InternalRequest(
					template : "#uri#/cfml_session_rotate/test_cfml_sessionend.cfm",
					url: {
						rotateOnSessionStart: "onSessionStart"
					}
				);
				_dumpResult( cfmlSessionId );
				expect( len( cfmlSessionId.fileContent ) ).toBeGT( 0 );
				var _cookies = _getCookies( cfmlSessionId, "cfid" );
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
				var appName = listFirst( trim( cfmlSessionId.fileContent ), '-' ) & "-";
				expect( _getSessionCount( appName ) ).toBe( 1 );
				// allow session to expire
				_dumpSessions("before");
				sleep( 1001 );
				admin
					action="purgeExpiredSessions"
					type="server"
					password="#request.SERVERADMINPASSWORD#";
				_dumpSessions("post purge");

				expect( _getSessionCount( appName ) ).toBe( 0 );
				expect( structKeyExists( server.LDEV3478.ended_CFML_Sessions, trim( cfmlSessionId.fileContent ) ) ).toBeTrue();
			});
		});

		describe( "Test suite for LDEV3478 - jee sessions ", function() {

			it( title='jee session - onSessionEnd with sessionRotate()', body=function( currentSpec ) {
				_reset('jee');
				var uri = createURI("LDEV3478");
				var result = _InternalRequest(
					template : "#uri#/jee_session_rotate/test_jee_sessionend.cfm"
				);
				_dumpSessions("before");
				_dumpResult( result );
				var data = deserializeJSON( trim( result.fileContent ) );
				var appName = data.applicationName;

				// sessionRotate() keeps the jee session, but gives it a new id
				expect( data.initialSessionId ).notToBeEmpty();
				expect( data.sessionId ).notToBeEmpty();
				expect( data.sessionId ).notToBe( data.initialSessionId );
				expect( result.session.sessionid ).toBe( data.sessionId );
				var _cookies = _getJSessionIdCookies( result );
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#serializeJson( result.headers[ "Set-Cookie" ] ?: [] )#]" );
				expect( listRest( listFirst( _cookies[ 1 ], ";" ), "=" ) ).toBe( data.sessionId );

				expect( structKeyExists( server.LDEV3478.start_JEE_Sessions, data.initialSessionId ) ).toBeTrue();
				// rotating ends the session under its old id
				expect( structKeyExists( server.LDEV3478.ended_JEE_Sessions, data.initialSessionId ) ).toBeTrue();
				expect( structKeyExists( server.LDEV3478.ended_JEE_Sessions, data.sessionId ) ).toBeFalse();
				expect( _getSessionCount( appName ) ).toBe( 1 );

				// allow session to expire
				sleep( 1001 );
				admin
					action="purgeExpiredSessions"
					type="server"
					password="#request.SERVERADMINPASSWORD#";
				_dumpSessions("post purge");

				// the rotated session ends on expiry
				expect( _getSessionCount( appName ) ).toBe( 0 );
				expect( structKeyExists( server.LDEV3478.ended_JEE_Sessions, data.sessionId ) ).toBeTrue();
				expect( structKeyExists( server.LDEV3478.ended_JEE_Sessions, data.initialSessionId ) ).toBeTrue();
			});

			it( title='LDEV-5271 jee session - onSessionEnd with sessionRotate() in onSessionStart', body=function( currentSpec ) {
				_reset('jee');
				var uri = createURI("LDEV3478");
				var result = _InternalRequest(
					template : "#uri#/jee_session_rotate/test_jee_sessionend.cfm",
					url: {
						rotateOnSessionStart: "onSessionStart"
					}
				);
				_dumpResult( result );
				var data = deserializeJSON( trim( result.fileContent ) );
				var appName = data.applicationName;

				// rotated in onSessionStart, the template only sees the new id
				expect( data.sessionId ).notToBeEmpty();
				expect( data.initialSessionId ).toBe( data.sessionId );
				expect( result.session.sessionid ).toBe( data.sessionId );
				var _cookies = _getJSessionIdCookies( result );
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#serializeJson( result.headers[ "Set-Cookie" ] ?: [] )#]" );
				expect( listRest( listFirst( _cookies[ 1 ], ";" ), "=" ) ).toBe( data.sessionId );

				// onSessionStart saw the session before and after sessionRotate()
				var startedIds = structKeyArray( server.LDEV3478.start_JEE_Sessions );
				expect( len( startedIds ) ).toBe( 2, "started sessions [#serializeJson( startedIds )#]" );
				expect( arrayFind( startedIds, data.sessionId ) ).toBeGT( 0 );
				arrayDelete( startedIds, data.sessionId );
				var preRotateId = startedIds[ 1 ];
				expect( preRotateId ).notToBe( data.sessionId );
				expect( structKeyExists( server.LDEV3478.ended_JEE_Sessions, data.sessionId ) ).toBeFalse();
				expect( _getSessionCount( appName ) ).toBe( 1 );

				// allow session to expire
				_dumpSessions("before");
				sleep( 1001 );
				admin
					action="purgeExpiredSessions"
					type="server"
					password="#request.SERVERADMINPASSWORD#";
				_dumpSessions("post purge");

				expect( _getSessionCount( appName ) ).toBe( 0 );
				expect( structKeyExists( server.LDEV3478.ended_JEE_Sessions, data.sessionId ) ).toBeTrue();
			});
		});
	}

	private numeric function _getSessionCount( applicationName ){
		var sess = getPageContext().getCFMLFactory().getScopeContext().getAllCFSessionScopes();
		if ( structKeyExists( sess, arguments.applicationName ) )
			return len( sess[ arguments.applicationName ] );
		else
			return 0;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function _dumpSessions( moment ){
		return;
		systemOutput("---#moment#", true);
		for (var type in server.LDEV3478 ){
			systemOutput( type & ": " & server.LDEV3478[type].toJson(), true );
		}
	}

	private function _reset( type="" ){
		//systemOutput( "", true );
		server.LDEV3478 = {};
		if ( arguments.type eq "cfml" ){
			server.LDEV3478["start_CFML_Sessions"] = {};
			server.LDEV3478["ended_CFML_Sessions"] = {};
		} else if ( arguments.type eq "jee" ){
			server.LDEV3478["start_JEE_Sessions"] = {};
			server.LDEV3478["ended_JEE_Sessions"] = {};
		}
	}

	private function _getCookies( result, name ){
		var headers = result.headers[ "Set-Cookie" ] ?: [];
		var matches = [];
		for ( var header in headers ){
			if ( listFirst( header, "=" ) eq arguments.name )
				arrayAppend( matches, header );
		}
		matches = [ "remove this to test LDEV-1105, duplicate cfid cookies" ];
		return matches;
	}

	private function _getJSessionIdCookies( result ){
		var headers = result.headers[ "Set-Cookie" ] ?: [];
		if ( isSimpleValue( headers ) ) headers = [ headers ];
		var matches = [];
		for ( var header in headers ){
			if ( listFirst( header, "=" ) eq "jsessionid" )
				arrayAppend( matches, header );
		}
		return matches;
	}

	private function _dumpResult( result ){
		return;
		systemOutput( result.headers[ "Set-Cookie" ] ?: "[]", true );
	}
}