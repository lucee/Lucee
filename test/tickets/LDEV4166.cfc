component extends="org.lucee.cfml.test.LuceeTestCase" labels="session" {

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
				var uri = createURI("LDEV4166");
				var cfmlSessionId = _InternalRequest(
					template : "#uri#/cfml_session_invalidate/test_cfml_sessionend.cfm"
				);
				//dumpResult( "cfmlSessionId: " & cfmlSessionId.filecontent );
				expect( len( cfmlSessionId.filecontent ) ).toBeGT( 0 );

				var appName = listFirst( trim( cfmlSessionId.filecontent ), '-' ) & "-" ;

				expect( getSessionCount( appName ) ).toBe( 0 );
				expect( structKeyExists( server.LDEV4166_ended_CFML_Sessions, trim( cfmlSessionId.filecontent ) ) ).toBeTrue();
			});

			it( title='jee session - onSessionEnd with SessionInvalidate()', body=function( currentSpec ) {
				var uri = createURI("LDEV4166");
				var j2eeSessionId = _InternalRequest(
					template : "#uri#/jee_session_invalidate/test_jee_sessionend.cfm"
				);
				//dumpResult( "j2eeSessionId: " & j2eeSessionId.filecontent );
				expect( len( j2eeSessionId.filecontent ) ).toBeGT( 0 );

				var appName = listFirst( trim( j2eeSessionId.filecontent ), '-' ) & "-" ;

				expect( getSessionCount( appName ) ).toBe( 0 );
				expect( structKeyExists( server.LDEV4166_ended_JEE_Sessions, trim( j2eeSessionId.filecontent ) ) ).toBeTrue();
			});
		});
	}

	private numeric function getSessionCount( applicationName ){
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
}
