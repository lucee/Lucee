component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {


	function beforeAll(){
		server.LDEV3264_endedSessions = {};
		systemOutput("", true);
	}

	function afterAll(){
		systemOutput("-------------------------" , true);
		structDelete(server, "LDEV3264_endedSessions");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2308", function() {
			it( title='check onSessionEnd with cfml session', body=function( currentSpec ) {
				uri = createURI("LDEV3264");
				local.cfmlSessionId = _InternalRequest(
					template : "#uri#\cfml-session\testOnSessionEnd.cfm"
				);
				debug(local.cfmlSessionId );
				expect( len( local.cfmlSessionId.filecontent ) ).toBeGT( 0 );
				sleep(61000);
				local.result = _InternalRequest(
					template : "#uri#\cfml-session\testOnSessionEnd.cfm",
					url: {
						dumpEndedSessions: true,
						check: trim(cfmlSessionId.filecontent)
					}
				);
				debug(local.result );
				expect( trim( result.filecontent ) ).toBeTrue();
			});

			it( title='check onSessionEnd with cfml session', body=function( currentSpec ) {
				uri = createURI("LDEV3264");
				local.j2eeSessionId = _InternalRequest(
					template : "#uri#\j2ee-session\testOnSessionEnd.cfm"
				);
				debug( local.j2eeSessionId );
				expect( len( local.j2eeSessionId.filecontent ) ).toBeGT( 0 );
				sleep(61000);
				local.result = _InternalRequest(
					template : "#uri#\j2ee-session\testOnSessionEnd.cfm",
					url: {
						dumpEndedSessions: true,
						check: trim(j2eeSessionId.filecontent)
					}
				);
				debug( local.result );
				expect( trim( result.filecontent ) ).toBeTrue();
			});
		});
	}

	private function dumpResult(r){
		systemOutput("", true);
		systemOutput(r.filecontent, true);
		systemOutput("", true);
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
}





