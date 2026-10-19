component extends="org.lucee.cfml.test.LuceeTestCase" labels="ai" {

	function beforeAll() {
		variables.resolver = createObject( "java", "lucee.runtime.ai.AIModelResolver" );
		variables.available = [
			"gpt-4o",
			"gpt-4.1",
			"gpt-5",
			"gpt-5-mini",
			"gpt-5.1",
			"gpt-5.1-codex",
			"text-embedding-3-large",
			"whisper-1",
			"claude-sonnet-4-20250514",
			"claude-sonnet-4-5-20250929",
			"claude-opus-4-1-20250805",
			"claude-haiku-4-5-20251001",
			"llama-3.3-70b-versatile",
			"llama-3.1-8b-instant",
			"gemini-2.0-flash",
			"gemini-2.5-flash",
			"gemini-2.5-flash-lite",
			"gemini-2.5-pro"
		];
	}

	function afterAll() {
		for ( var name in [ "ldev6618-openai", "ldev6618-claude" ] ) {
			try {
				admin action="removeAIConnection" type="server" password="#server.SERVERADMINPASSWORD#" name="#name#";
			}
			catch ( e ) {}
		}
	}

	private function skipAimock() {
		return structCount( server.getTestService( "aimock" ) ) == 0;
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6618 AI connection without model: pattern matching", function() {

			it( "glob matching is case insensitive and supports * and ?", function() {
				expect( resolver.matches( "gpt-5*", "gpt-5" ) ).toBeTrue();
				expect( resolver.matches( "gpt-5*", "GPT-5.1-mini" ) ).toBeTrue();
				expect( resolver.matches( "gpt-5*", "gpt-4o" ) ).toBeFalse();
				expect( resolver.matches( "*sonnet*", "claude-sonnet-4-5-20250929" ) ).toBeTrue();
				expect( resolver.matches( "gemini-*-flash", "gemini-2.5-flash" ) ).toBeTrue();
				expect( resolver.matches( "gemini-*-flash", "gemini-2.5-flash-lite" ) ).toBeFalse();
				expect( resolver.matches( "gpt-?o", "gpt-4o" ) ).toBeTrue();
				expect( resolver.matches( "gpt-4", "gpt-4o" ) ).toBeFalse();
				expect( resolver.matches( "gpt-4", "gpt-4" ) ).toBeTrue();
			});

			it( "detects models that are not for chat", function() {
				expect( resolver.isNonChat( "text-embedding-3-large" ) ).toBeTrue();
				expect( resolver.isNonChat( "whisper-1" ) ).toBeTrue();
				expect( resolver.isNonChat( "gpt-5" ) ).toBeFalse();
				expect( resolver.isNonChat( "deepseek-chat" ) ).toBeFalse();
			});

			it( "modelPreference accepts a list or an array", function() {
				expect( arrayLen( resolver.toPatterns( "gpt-5*, claude-sonnet* ,," ) ) ).toBe( 2 );
				expect( arrayLen( resolver.toPatterns( [ "gpt-5*", "", "llama-3.3-70b*" ] ) ) ).toBe( 2 );
				expect( arrayLen( resolver.toPatterns( "" ) ) ).toBe( 0 );
			});
		});

		describe( "LDEV-6618 AI connection without model: selection", function() {

			it( "picks the highest version matching a pattern, base model before variants", function() {
				expect( resolver.best( available, "gpt-5*", false ) ).toBe( "gpt-5.1" );
				expect( resolver.best( available, "gpt-4*", false ) ).toBe( "gpt-4.1" );
				expect( resolver.best( available, "claude-sonnet*", false ) ).toBe( "claude-sonnet-4-5-20250929" );
				expect( resolver.best( available, "gemini-*", false ) ).toBe( "gemini-2.5-pro" );
				expect( resolver.best( available, "gemini-*-flash", false ) ).toBe( "gemini-2.5-flash" );
				expect( resolver.best( available, "llama-3.3-70b*", false ) ).toBe( "llama-3.3-70b-versatile" );
				expect( isNull( resolver.best( available, "mistral*", false ) ) ).toBeTrue();
			});

			it( "the first preference with a match wins", function() {
				expect( resolver.select( available, [ "mistral*", "claude-sonnet*", "gpt-5*" ], [] ) ).toBe( "claude-sonnet-4-5-20250929" );
				expect( resolver.select( available, [ "llama-3.3-70b*" ], [ "gpt-*" ] ) ).toBe( "llama-3.3-70b-versatile" );
			});

			it( "an exact model name in the preference works", function() {
				expect( resolver.select( available, [ "gpt-5-mini" ], [] ) ).toBe( "gpt-5-mini" );
			});

			it( "the built-in fallback is used when no preference matches", function() {
				expect( resolver.select( available, [ "mistral*" ], [ "gpt-*" ] ) ).toBe( "gpt-5.1" );
			});

			it( "the built-in fallback skips non chat models", function() {
				expect( resolver.select( [ "text-embedding-3-large", "gpt-4o", "gpt-4.1-mini" ], [], [ "*" ] ) ).toBe( "gpt-4.1-mini" );
				expect( resolver.select( available, [], [ "claude-sonnet-*", "claude-*" ] ) ).toBe( "claude-sonnet-4-5-20250929" );
			});

			it( "without a match the first chat model of the provider is used", function() {
				expect( resolver.select( [ "whisper-1", "qwen3:8b", "gemma3" ], [], [ "gpt-*" ] ) ).toBe( "qwen3:8b" );
			});

			it( "returns null when nothing fits", function() {
				expect( isNull( resolver.select( [ "whisper-1" ], [], [] ) ) ).toBeTrue();
				expect( isNull( resolver.select( [], [], [] ) ) ).toBeTrue();
			});

			it( "when the models can not be listed, explicit names are used", function() {
				expect( resolver.select( javaCast( "null", "" ), [ "gpt-5*", "gpt-5-mini" ], [] ) ).toBe( "gpt-5-mini" );
				expect( resolver.select( javaCast( "null", "" ), [], [ "claude-*", "sonar" ] ) ).toBe( "sonar" );
				expect( isNull( resolver.select( javaCast( "null", "" ), [ "gpt-5*" ], [ "gpt-*" ] ) ) ).toBeTrue();
			});
		});

		describe( "LDEV-6618 AI connection without model: ranking", function() {

			it( "date and snapshot parts are not part of the version", function() {
				expect( arrayToList( resolver.version( "gpt-4o-2024-11-20" ) ) ).toBe( "4" );
				expect( arrayToList( resolver.version( "gpt-4o" ) ) ).toBe( "4" );
				expect( arrayToList( resolver.version( "gpt-5.4" ) ) ).toBe( "5,400000" );
				expect( arrayToList( resolver.version( "grok-4.20-0309-reasoning" ) ) ).toBe( "4,200000" );
				expect( arrayToList( resolver.version( "claude-sonnet-4-5-20250929" ) ) ).toBe( "4,5" );
				expect( arrayToList( resolver.version( "gpt-4-0613" ) ) ).toBe( "4" );
				expect( resolver.undated( "gpt-4o-2024-11-20" ) ).toBe( "gpt-4o" );
				expect( resolver.isDated( "gpt-4o-2024-11-20" ) ).toBeTrue();
				expect( resolver.isDated( "gpt-4o" ) ).toBeFalse();
			});

			it( "the undated alias wins over a dated snapshot of the same model", function() {
				var models = [ "gpt-4o-2024-08-06", "gpt-4o-2024-11-20", "gpt-4o", "gpt-4o-mini", "gpt-4o-mini-2024-07-18" ];
				expect( resolver.best( models, "gpt-*", true ) ).toBe( "gpt-4o" );
				// without the alias, the newest snapshot of the base model wins
				expect( resolver.best( [ "gpt-4o-2024-08-06", "gpt-4o-2024-11-20", "gpt-4o-mini" ], "gpt-*", true ) ).toBe( "gpt-4o-2024-11-20" );
			});

			it( "gpt-5 ranks before gpt-4.1 and gpt-4o", function() {
				var models = [ "gpt-4o-2024-11-20", "gpt-4o", "gpt-4.1", "gpt-4.1-2025-04-14", "gpt-5.4", "gpt-5.4-2026-03-05", "gpt-5.4-mini", "gpt-5.4-pro", "gpt-image-1.5", "gpt-realtime", "gpt-audio" ];
				expect( resolver.select( models, [], [ "gpt-*" ] ) ).toBe( "gpt-5.4" );
				expect( resolver.best( [ "gpt-4o", "gpt-4.1" ], "gpt-*", true ) ).toBe( "gpt-4.1" );
			});

			it( "a number after a dot is a decimal fraction", function() {
				expect( resolver.best( [ "grok-4.20-0309-non-reasoning", "grok-4.3", "grok-4.7", "grok-4.6" ], "grok-*", true ) ).toBe( "grok-4.7" );
				expect( resolver.best( [ "gemini-2.5-flash", "gemini-3.8-flash", "gemini-3-flash-preview" ], "gemini-*-flash", true ) ).toBe( "gemini-3.8-flash" );
				expect( resolver.best( [ "claude-sonnet-4-5-20250929", "claude-sonnet-4-6", "claude-sonnet-5" ], "claude-sonnet-*", true ) ).toBe( "claude-sonnet-5" );
			});

			it( "grok: non-reasoning wins over reasoning", function() {
				var models = [ "grok-4.20-0309-reasoning", "grok-4.20-0309-non-reasoning", "grok-4.20-multi-agent-0309", "grok-4-1-fast-reasoning", "grok-4-1-fast-non-reasoning", "grok-imagine-image" ];
				expect( resolver.specialVariants( "grok-4.20-0309-reasoning" ) ).toBe( 1 );
				expect( resolver.specialVariants( "grok-4.20-0309-non-reasoning" ) ).toBe( 0 );
				expect( resolver.select( models, [], [ "grok-*" ] ) ).toBe( "grok-4.20-0309-non-reasoning" );
				// "grok-*-reasoning" also matches "-non-reasoning", an exact name selects the reasoning model
				expect( resolver.select( models, [ "grok-*-reasoning" ], [ "grok-*" ] ) ).toBe( "grok-4.20-0309-non-reasoning" );
				expect( resolver.select( models, [ "grok-4.20-0309-reasoning" ], [ "grok-*" ] ) ).toBe( "grok-4.20-0309-reasoning" );
			});

			it( "an empty model list (provider without model listing, e.g. Perplexity) still uses the fallback", function() {
				expect( resolver.select( [], [], [ "sonar" ] ) ).toBe( "sonar" );
				expect( resolver.select( [], [ "sonar-pro" ], [ "sonar" ] ) ).toBe( "sonar-pro" );
			});

			it( "detects provider errors about an unsupported temperature", function() {
				var support = createObject( "java", "lucee.runtime.ai.AIEngineSupport" );
				expect( support.isTemperatureRejected( "temperature is deprecated for this model." ) ).toBeTrue();
				expect( support.isTemperatureRejected( "Unsupported value: 'temperature' does not support 0.7 with this model. Only the default (1) value is supported." ) ).toBeTrue();
				expect( support.isTemperatureRejected( "Incorrect API key provided" ) ).toBeFalse();
				expect( support.isTemperatureRejected( "max_tokens: field required" ) ).toBeFalse();
			});
		});

		describe( title="LDEV-6618 AI connection without model: with a mock provider", skip=skipAimock(), body=function() {

			it( "AIGetModels lists the models of an endpoint", function() {
				var mockURL = getMockURL();
				createConnection( "ldev6618-openai", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: mockURL & "/v1/", secretKey: "mock", model: "gpt-4" } );
				var models = AIGetModels( "ldev6618-openai" );
				expect( isQuery( models ) ).toBeTrue();
				expect( models.recordCount ).toBeGT( 0 );
				expect( queryColumnData( models, "name" ) ).toInclude( "gpt-4" );
			});

			it( "without a model, the modelPreference resolves the model", function() {
				var mockURL = getMockURL();
				createConnection( "ldev6618-openai", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: mockURL & "/v1/", secretKey: "mock", modelPreference: "mistral*,gpt-4*" } );
				var models = queryColumnData( AIGetModels( "ldev6618-openai" ), "name" );
				var expected = createObject( "java", "lucee.runtime.ai.AIModelResolver" ).best( models, "gpt-4*", false );
				expect( AIGetMetaData( "ldev6618-openai" ).model ).toBe( expected );
			});

			it( "an empty model without a preference uses the built-in fallback (Claude)", function() {
				var mockURL = getMockURL();
				createConnection( "ldev6618-claude", "lucee.runtime.ai.anthropic.ClaudeEngine", { type: "other", url: mockURL & "/v1/", apiKey: "mock", model: "" } );
				var models = queryColumnData( AIGetModels( "ldev6618-claude" ), "name" );
				var expected = createObject( "java", "lucee.runtime.ai.AIModelResolver" ).select( models, [], [ "claude-sonnet-*", "claude-opus-*", "claude-*" ] );
				expect( AIGetMetaData( "ldev6618-claude" ).model ).toBe( expected );
				expect( AIGetMetaData( "ldev6618-claude" ).model ).notToBe( "" );
			});

			it( "a session can be created and used without a model", function() {
				var mockURL = getMockURL();
				createConnection( "ldev6618-openai", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: mockURL & "/v1/", secretKey: "mock", model: "" } );
				var model = AIGetMetaData( "ldev6618-openai" ).model;
				expect( model ).notToBe( "" );
				expect( queryColumnData( AIGetModels( "ldev6618-openai" ), "name" ) ).toInclude( model );
				var sess = createAISession( name="ldev6618-openai" );
				expect( inquiryAISession( sess, "ping" ) ).toBe( "ping" );
			});

			it( "auto is not a keyword, it is used as model name", function() {
				var mockURL = getMockURL();
				createConnection( "ldev6618-openai", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: mockURL & "/v1/", secretKey: "mock", model: "auto" } );
				expect( AIGetMetaData( "ldev6618-openai" ).model ).toBe( "auto" );
			});
		});

		describe( title="LDEV-6618 AI connection without model: real providers (only with API keys)", body=function() {

			it( title="Groq", skip=isEmpty( getKey( "GROQ_API_KEY" ) ), body=function() {
				createConnection( "ldev6618-groq", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: "https://api.groq.com/openai/v1/", secretKey: getKey( "GROQ_API_KEY" ) } );
				try {
					var model = AIGetMetaData( "ldev6618-groq" ).model;
					expect( model ).notToBe( "" );
					expect( queryColumnData( AIGetModels( "ldev6618-groq" ), "name" ) ).toInclude( model );
				}
				finally {
					admin action="removeAIConnection" type="server" password="#server.SERVERADMINPASSWORD#" name="ldev6618-groq";
				}
			});

			it( title="OpenRouter", skip=isEmpty( getKey( "OPENROUTER_API_KEY" ) ), body=function() {
				createConnection( "ldev6618-openrouter", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: "https://openrouter.ai/api/v1/", secretKey: getKey( "OPENROUTER_API_KEY" ) } );
				try {
					var model = AIGetMetaData( "ldev6618-openrouter" ).model;
					expect( model ).notToBe( "" );
					expect( queryColumnData( AIGetModels( "ldev6618-openrouter" ), "name" ) ).toInclude( model );
				}
				finally {
					admin action="removeAIConnection" type="server" password="#server.SERVERADMINPASSWORD#" name="ldev6618-openrouter";
				}
			});
		});
	}

	private function getMockURL() {
		var cfg = server.getTestService( "aimock" );
		return "http://#cfg.server#:#cfg.multiPort ?: 4010#";
	}

	private function getKey( required string name ) {
		return server.system.environment[ arguments.name ] ?: ( server.system.properties[ arguments.name ] ?: "" );
	}

	private function createConnection( required string name, required string class, required struct custom ) {
		admin
			action="updateAIConnection"
			type="server"
			password="#server.SERVERADMINPASSWORD#"
			name="#arguments.name#"
			class="#arguments.class#"
			custom="#arguments.custom#";
	}
}
