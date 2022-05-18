component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function beforeAll(){
		server.LDEV3478_ended_CFML_Sessions = {};		
		server.LDEV3478_ended_JEE_Sessions = {};		
	}

	function afterAll(){		
		//systemOutput("ended sessionids:" & structKeyList(server.LDEV3478_endedSessions), true);
		structDelete(server, "LDEV3478_ended_CFML_Sessions");
		structDelete(server, "LDEV3478_ended_JEE_Sessions");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV3478", function() {
			it( title='cfml session - onSessionEnd with sessionRotate()', body=function( currentSpec ) {
				uri = createURI("LDEV3478");
				local.cfmlSessionId = _InternalRequest(
					template : "#uri#\cfml_session_rotate\test_cfml_sessionend.cfm"
				);
				//dumpResult( "cfmlSessionId: " & cfmlSessionId.filecontent );
				expect( len( cfmlSessionId.filecontent ) ).toBeGT( 0 );
				// allow session to expire
				sleep(1001);
				admin
					action="purgeExpiredSessions"
					type="server"
					password="#request.SERVERADMINPASSWORD#";
				
				//dumpResult( result.filecontent );
				expect( structKeyExists(server.LDEV3478_ended_CFML_Sessions, trim(cfmlSessionId.filecontent)) ).toBeTrue();
			});

			it( title='jee session - onSessionEnd with sessionRotate()', body=function( currentSpec ) {
				uri = createURI("LDEV3478");
				local.j2eeSessionId = _InternalRequest(
					template : "#uri#\jee_session_rotate\test_jee_sessionend.cfm"
				);
				//dumpResult( "j2eeSessionId: " & j2eeSessionId.filecontent );
				expect( len( j2eeSessionId.filecontent ) ).toBeGT( 0 );
				// allow session to expire
				sleep(1001);
				admin
					action="purgeExpiredSessions"
					type="server"
					password="#request.SERVERADMINPASSWORD#";
				
				// dumpResult( result.filecontent );
				expect( structKeyExists(server.LDEV3478_ended_JEE_Sessions, trim(j2eeSessionId.filecontent)) ).toBeTrue();
			});
		});
	}

// 	private function dumpResult(r){
// //		systemOutput("---", true);
// 		systemOutput(r, true);
// //		systemOutput("---", true);
// 	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}