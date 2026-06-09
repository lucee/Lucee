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

			it( title="serializes and restores multipart answer history", body=function() {
				var data = {
					temperature: 0.7,
					limit: 50,
					connectionTimeout: 2000,
					socketTimeout: 20000,
					history: [
						{
							question: "show image",
							answer: [
								{ type: "text", contenttype: "text/plain", content: "Caption:" },
								{ type: "binary", contenttype: "image/png", content: toBase64( "png-bytes" ) }
							]
						}
					]
				};
				var loaded = LoadAISession( "aimock-openai", data );
				var json = SerializeAISession( loaded );
				var restored = deserializeJSON( json );
				expect( isArray( restored.history[ 1 ].answer ) ).toBeTrue();
				expect( restored.history[ 1 ].answer[ 1 ].content ).toBe( "Caption:" );
				expect( restored.history[ 1 ].answer[ 2 ].type ).toBe( "binary" );
				var roundTrip = LoadAISession( "aimock-openai", json );
				expect( inquiryAISession( roundTrip, "continued" ) ).toBe( "continued" );
			});

		});
	}

}
