component extends="org.lucee.cfml.test.LuceeTestCase" {

	function skipAimock() {
		return structCount( server.getTestService( "aimock" ) ) == 0;
	}

	function run( testResults, testBox ) {
		describe( title="Test suite for SerializeAISession()", skip=skipAimock, body=function() {

			it( title="serializes a session after an inquiry", body=function() {
				var ais = createAISession( name="aimock-openai" );
				inquiryAISession( ais, "serialize-me" );
				var json = SerializeAISession( ais );
				expect( isJson( json ) ).toBeTrue();
				expect( structKeyExists( deserializeJSON( json ), "history" ) ).toBeTrue();
			});

			it( title="serialized session can be restored with LoadAISession", body=function() {
				var ais = createAISession( name="aimock-openai", systemMessage="test" );
				inquiryAISession( ais, "round-trip" );
				var loaded = LoadAISession( "aimock-openai", SerializeAISession( ais ) );
				expect( inquiryAISession( loaded, "again" ) ).toBe( "again" );
			});

		});
	}

}
