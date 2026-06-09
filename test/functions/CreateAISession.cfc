component extends="org.lucee.cfml.test.LuceeTestCase" {

	function skipAimock() {
		return structCount( server.getTestService( "aimock" ) ) == 0;
	}

	function run( testResults, testBox ) {
		describe( title="Test suite for CreateAISession()", skip=skipAimock, body=function() {

			it( title="creates a session for the mock connection", body=function() {
				var ais = createAISession( name="aimock-openai", systemMessage="You are a test assistant" );
				expect( isObject( ais ) ).toBeTrue();
			});

			it( title="accepts conversation history limit", body=function() {
				var ais = createAISession( name="aimock-openai", conversationHistoryLimit=3 );
				expect( isObject( ais ) ).toBeTrue();
			});

		});
	}

}
