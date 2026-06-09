component extends="org.lucee.cfml.test.LuceeTestCase" {

	function skipAimock() {
		return structCount( server.getTestService( "aimock" ) ) == 0;
	}

	function run( testResults, testBox ) {
		describe( title="Test suite for InquiryAISession()", skip=skipAimock, body=function() {

			it( title="echoes the user message from the mock endpoint", body=function() {
				var ais = createAISession( name="aimock-openai" );
				expect( inquiryAISession( ais, "Hello from InquiryAISession" ) ).toBe( "Hello from InquiryAISession" );
			});

			it( title="supports follow-up questions in the same session", body=function() {
				var ais = createAISession( name="aimock-openai" );
				inquiryAISession( ais, "first" );
				expect( inquiryAISession( ais, "second" ) ).toBe( "second" );
			});

		});
	}

}
