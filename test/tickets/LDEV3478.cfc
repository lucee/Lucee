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
				var j2eeSessionId = _InternalRequest(
					template : "#uri#/jee_session_rotate/test_jee_sessionend.cfm"
				);
				_dumpSessions("before");
				_dumpResult( j2eeSessionId );
				expect( len( j2eeSessionId.fileContent ) ).toBeGT( 0 );
				var _cookies = _getCookies( j2eeSessionId, "cfid" );
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
				var appName = listFirst( trim( j2eeSessionId.fileContent ), '-' ) & "-";
				expect( _getSessionCount( appName ) ).toBe( 1 );
				// allow session to expire
				sleep( 1001 );
				admin
					action="purgeExpiredSessions"
					type="server"
					password="#request.SERVERADMINPASSWORD#";
				expect( _getSessionCount( appName ) ).toBe( 0 );
				expect( structKeyExists( server.LDEV3478.ended_JEE_Sessions, trim( j2eeSessionId.fileContent ) ) ).toBeTrue();
			});

			it( title='LDEV-5271 jee session - onSessionEnd with sessionRotate() in onSessionStart', body=function( currentSpec ) {
				_reset('jee');
				var uri = createURI("LDEV3478");
				var j2eeSessionId = _InternalRequest(
					template : "#uri#/jee_session_rotate/test_jee_sessionend.cfm",
					url: {
						rotateOnSessionStart: "onSessionStart"
					}
				);
				_dumpResult( j2eeSessionId );
				expect( len( j2eeSessionId.fileContent ) ).toBeGT( 0 );
				var _cookies = _getCookies( j2eeSessionId, "cfid" );
				expect( len( _cookies ) ).toBe( 1, "cookies returned [#_cookies.toJson()#]" );
				var appName = listFirst( trim( j2eeSessionId.fileContent ), '-' ) & "-";
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
				expect( structKeyExists( server.LDEV3478.ended_JEE_Sessions, trim( j2eeSessionId.fileContent ) ) ).toBeTrue();
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
		return matches;
	}

	private function _dumpResult( result ){
		return;
		systemOutput( result.headers[ "Set-Cookie" ] ?: "[]", true );
	}
}