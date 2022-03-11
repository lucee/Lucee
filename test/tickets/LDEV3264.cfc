component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2308", function() {
			it( title='check onSessionEnd with cfml session', body=function( currentSpec ) {
				uri = createURI("LDEV3264");
				local.cfmlSessionId = _InternalRequest(
					template : "#uri#\cfml-session\testOnSessionEnd.cfm"
				);
				debug(local.cfmlSessionId );
				expect( len( local.cfmlSessionId.filecontent ) ).toBeGT( 0 );
				sleep(5000);
				local.result = _InternalRequest(
					template : "#uri#\cfml-session\testOnSessionEnd.cfm",
					url: {
						dumpEndedSessions: true
					}
				);
				debug(local.result );
				expect( trim( result.filecontent ) ).toBe( trim( cfmlSessionId.fileContent ) );
			});

			it( title='check onSessionEnd with cfml session', body=function( currentSpec ) {
				uri = createURI("LDEV3264");
				local.j2eeSessionId = _InternalRequest(
					template : "#uri#\j2ee-session\testOnSessionEnd.cfm"
				);
				debug( local.j2eeSessionId );
				expect( len( local.j2eeSessionId.filecontent ) ).toBeGT( 0 );
				sleep(5000);
				local.result = _InternalRequest(
					template : "#uri#\j2ee-session\testOnSessionEnd.cfm",
					url: {
						dumpEndedSessions: true
					}
				);
				debug( local.result );
				expect( trim( result.filecontent ) ).toBe( trim( j2eeSessionId.filecontent ) );
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





