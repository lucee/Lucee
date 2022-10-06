component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {


	function beforeAll(){
		server.LDEV3264_endedSessions = {};		
	}

	function afterAll(){		
		//systemOutput("ended sessionids:" & structKeyList(server.LDEV3264_endedSessions), true);
		structDelete(server, "LDEV3264_endedSessions");
		
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2308", function() {
			it( title='check onSessionEnd with cfml session', body=function( currentSpec ) {
				uri = createURI("LDEV3264");
				local.cfmlSessionId = _InternalRequest(
					template : "#uri#\cfml-session\testOnSessionEnd.cfm"
				);
				//dumpResult( "cfmlSessionId: " & cfmlSessionId.filecontent );
				expect( len( cfmlSessionId.filecontent ) ).toBeGT( 0 );
				// allow session to expire
				sleep(1001);
				admin
					action="purgeExpiredSessions"
					type="server"
					password="#request.SERVERADMINPASSWORD#";
				
				local.result = _InternalRequest(
					template : "#uri#\cfml-session\testOnSessionEnd.cfm",
					url: {
						dumpEndedSessions: true,
						check: trim(cfmlSessionId.filecontent)
					}
				);
				//dumpResult( result.filecontent );
				expect( trim( result.filecontent ) ).toBeTrue();
			});

			it( title='check onSessionEnd with cfml session', body=function( currentSpec ) {
				uri = createURI("LDEV3264");
				local.j2eeSessionId = _InternalRequest(
					template : "#uri#\j2ee-session\testOnSessionEnd.cfm"
				);
				//dumpResult( "j2eeSessionId: " & j2eeSessionId.filecontent );
				expect( len( j2eeSessionId.filecontent ) ).toBeGT( 0 );
				// allow session to expire
				sleep(1001);
				admin
					action="purgeExpiredSessions"
					type="server"
					password="#request.SERVERADMINPASSWORD#";
				
				local.result = _InternalRequest(
					template : "#uri#\j2ee-session\testOnSessionEnd.cfm",
					url: {
						dumpEndedSessions: true,
						check: trim(j2eeSessionId.filecontent)
					}
				);
				//dumpResult( result.filecontent );
				expect( trim( result.filecontent ) ).toBeTrue();
			});
		});
	}

	private function dumpResult(r){
//		systemOutput("---", true);
		systemOutput(r, true);
//		systemOutput("---", true);
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}





