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
		describe( title="LDEV-6388 mock AI HTTP endpoint integration", skip=skipAimock, body=function() {

			describe( title="OpenAI (mock-llm)", body=function() {

				it( title="registers and exposes the mock OpenAI connection", body=function() {
					expect( AIHas( variables.connections.openai ) ).toBeTrue();
				});

				it( title="echoes the user message via inquiryAISession", body=function() {
					resetAimockConfig();
					var ais = createAISession(
						name=variables.connections.openai,
						systemMessage="You are a test assistant"
					);
					var answer = inquiryAISession( ais, "Hello LDEV-6388" );
					expect( answer ).toBe( "Hello LDEV-6388" );
				});

				it( title="parses multipart text content blocks from the mock response", body=function() {
					configureMultipartResponse();
					var ais = createAISession( name=variables.connections.openai );
					var answer = inquiryAISession( ais, "multipart-test" );
					expect( answer ).toBe( "Line one. Line two." );
					resetAimockConfig();
				});

			});

			describe( title="Claude (aimock)", body=function() {

				it( title="registers and exposes the mock Claude connection", body=function() {
					expect( AIHas( variables.connections.claude ) ).toBeTrue();
				});

				it( title="echoes the user message via inquiryAISession", body=function() {
					var ais = createAISession(
						name=variables.connections.claude,
						systemMessage="You are a test assistant"
					);
					var answer = inquiryAISession( ais, "Hello LDEV-6388" );
					expect( answer ).toBe( "Hello LDEV-6388" );
				});

			});

			describe( title="Gemini (aimock)", body=function() {

				it( title="registers and exposes the mock Gemini connection", body=function() {
					expect( AIHas( variables.connections.gemini ) ).toBeTrue();
				});

				it( title="echoes the user message via inquiryAISession", body=function() {
					var ais = createAISession(
						name=variables.connections.gemini,
						systemMessage="You are a test assistant"
					);
					var answer = inquiryAISession( ais, "Hello LDEV-6388" );
					expect( answer ).toBe( "Hello LDEV-6388" );
				});

			});

		});
	}

	private void function resetAimockConfig() {
		cfhttp(
			url="http://#variables.aimock.server#:#variables.aimock.port#/config",
			method="DELETE",
			timeout="5",
			throwOnError=true
		);
	}

	private void function configureMultipartResponse() {
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
