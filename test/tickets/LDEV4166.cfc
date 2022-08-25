component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" skip="true" {

	function beforeAll(){
		server.LDEV4166_ended_CFML_Sessions = {};		
		server.LDEV4166_ended_JEE_Sessions = {};
	}

	function afterAll(){		
		structDelete(server, "LDEV4166_ended_CFML_Sessions");
		structDelete(server, "LDEV4166_ended_JEE_Sessions");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV4166", function() {
			it( title='cfml session - onSessionEnd with SessionInvalidate()', body=function( currentSpec ) {
				uri = createURI("LDEV4166");
				local.cfmlSessionId = _InternalRequest(
					template : "#uri#\cfml_session_invalidate\test_cfml_sessionend.cfm"
				);
				//dumpResult( "cfmlSessionId: " & cfmlSessionId.filecontent );
				expect( len( cfmlSessionId.filecontent ) ).toBeGT( 0 );
				// allow session to expire
				sleep(1001);
				// admin
					action="purgeExpiredSessions"
					type="server"
					password="#request.SERVERADMINPASSWORD#";
				
				expect( structKeyExists(server.LDEV4166_ended_CFML_Sessions, trim(cfmlSessionId.filecontent)) ).toBeTrue();
			});

			it( title='jee session - onSessionEnd with SessionInvalidate()', body=function( currentSpec ) {
				uri = createURI("LDEV4166");
				local.j2eeSessionId = _InternalRequest(
					template : "#uri#\jee_session_invalidate\test_jee_sessionend.cfm"
				);
				//dumpResult( "j2eeSessionId: " & j2eeSessionId.filecontent );
				expect( len( j2eeSessionId.filecontent ) ).toBeGT( 0 );
				// allow session to expire
				sleep(1001);
				// admin
					action="purgeExpiredSessions"
					type="server"
					password="#request.SERVERADMINPASSWORD#";
				
				expect( structKeyExists(server.LDEV4166_ended_JEE_Sessions, trim(j2eeSessionId.filecontent)) ).toBeTrue();
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}