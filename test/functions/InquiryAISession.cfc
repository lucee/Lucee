component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.aimock = server.getTestService( "aimock" );
		variables.connections = structKeyExists( variables.aimock, "connections" ) ? variables.aimock.connections : {
			openai: "aimock-openai",
			claude: "aimock-claude",
			gemini: "aimock-gemini"
		};
	}

	function skipAimock() {
		return structCount( server.getTestService( "aimock" ) ) == 0;
	}

	function run( testResults, testBox ) {
		describe( title="Test suite for InquiryAISession()", skip=skipAimock, body=function() {

			describe( title="OpenAI (aimock-openai)", body=function() {

				it( title="echoes the user message from the mock endpoint", body=function() {
					var ais = createAISession( name=variables.connections.openai );
					expect( inquiryAISession( ais, "Hello from InquiryAISession" ) ).toBe( "Hello from InquiryAISession" );
				});

				it( title="supports follow-up questions in the same session", body=function() {
					var ais = createAISession( name=variables.connections.openai );
					inquiryAISession( ais, "first" );
					expect( inquiryAISession( ais, "second" ) ).toBe( "second" );
				});

				it( title="concatenates multipart text blocks from the mock response", body=function() {
					configureOpenAIMultipartResponse();
					var ais = createAISession( name=variables.connections.openai );
					expect( inquiryAISession( ais, "multipart-test" ) ).toBe( "Line one. Line two." );
					resetOpenAIMockConfig();
				});

			});

			describe( title="Claude (aimock-claude)", body=function() {

				it( title="echoes the user message from the mock endpoint", body=function() {
					var ais = createAISession( name=variables.connections.claude );
					expect( inquiryAISession( ais, "Hello from InquiryAISession" ) ).toBe( "Hello from InquiryAISession" );
				});

				it( title="concatenates thinking and text blocks from the mock response", body=function() {
					var ais = createAISession( name=variables.connections.claude );
					expect( inquiryAISession( ais, "claude-thinking" ) ).toBe( "Let me consider...Final answer." );
				});

			});

		});
	}

	private void function resetOpenAIMockConfig() {
		cfhttp(
			url="http://#variables.aimock.server#:#variables.aimock.port#/config",
			method="DELETE",
			timeout="5",
			throwOnError=true
		);
	}

	private void function configureOpenAIMultipartResponse() {
		var config = {
			"rules": [
				{
					"path": "/v1/chat/completions",
					"method": "POST",
					"match": "@",
					"response": {
						"status": 200,
						"content": '{"choices":[{"message":{"role":"assistant","content":"{{jmes request body.messages[-1].content}}"},"finish_reason":"stop"}]}'
					}
				},
				{
					"path": "/v1/chat/completions",
					"method": "POST",
					"match": "contains(body.messages[-1].content, 'multipart-test')",
					"response": {
						"status": 200,
						"content": '{"choices":[{"message":{"role":"assistant","content":[{"type":"text","text":"Line one."},{"type":"text","text":" Line two."}]},"finish_reason":"stop"}]}'
					}
				}
			]
		};

		cfhttp(
			url="http://#variables.aimock.server#:#variables.aimock.port#/config",
			method="POST",
			timeout="5",
			throwOnError=true
		) {
			cfhttpparam( type="header", name="Content-Type", value="application/json" );
			cfhttpparam( type="body", value=serializeJSON( config ) );
		}
	}

}
