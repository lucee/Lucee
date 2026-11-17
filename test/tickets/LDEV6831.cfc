component extends="org.lucee.cfml.test.LuceeTestCase" labels="ai" {

	function run( testResults, testBox ) {
		describe( "LDEV-6831 AI connection settings the engine does not know are not sent to the provider", function() {

			it( title="unknown settings are dropped, known provider parameters are kept (OpenAI)", body=function() {
				var params = toProviderParameters( "openai", {
					"temprature": 0.5,
					"maxTokensPerSession": 1000,
					"top_p": 0.9,
					"max_completion_tokens": 200
				} );
				expect( params ).notToHaveKey( "temprature" );
				expect( params ).notToHaveKey( "maxTokensPerSession" );
				expect( params.top_p ).toBe( 0.9 );
				expect( params.max_completion_tokens ).toBe( 200 );
				expect( structCount( params ) ).toBe( 2 );
			});

			it( title="unknown settings are dropped, known provider parameters are kept (Claude)", body=function() {
				var params = toProviderParameters( "claude", { "top_k": 5, "foo": "bar" } );
				expect( params.top_k ).toBe( 5 );
				expect( params ).notToHaveKey( "foo" );
			});

			it( title="unknown settings are dropped, known provider parameters are kept (Gemini)", body=function() {
				var params = toProviderParameters( "gemini", { "safetySettings": [], "maxTokensPerRequest": 10 } );
				expect( params ).toHaveKey( "safetySettings" );
				expect( params ).notToHaveKey( "maxTokensPerRequest" );
			});

			it( title="entries of the passthrough struct are always sent", body=function() {
				var params = toProviderParameters( "gemini", {
					"passthrough": { "someNewParam": "x", "top_p": 0.1 },
					"top_p": 0.5
				} );
				expect( params.someNewParam ).toBe( "x" );
				// explicit passthrough wins, top_p is not a Gemini root parameter anyway
				expect( params.top_p ).toBe( 0.1 );
				expect( params ).notToHaveKey( "passthrough" );
			});

			it( title="nothing left means nothing is sent", body=function() {
				expect( toProviderParameters( "openai", {} ) ).toBeEmpty();
			});

		});
	}

	private struct function toProviderParameters( required string engine, required struct remaining ) {
		var classes = {
			"openai": "lucee.runtime.ai.openai.OpenAIEngine",
			"claude": "lucee.runtime.ai.anthropic.ClaudeEngine",
			"gemini": "lucee.runtime.ai.google.GeminiEngine"
		};
		var allowed = createObject( "java", classes[ arguments.engine ] ).PROVIDER_PARAMETERS;
		return createObject( "java", "lucee.runtime.ai.AIUtil" ).toProviderParameters( "ldev6831-" & arguments.engine, arguments.remaining, allowed );
	}

}
