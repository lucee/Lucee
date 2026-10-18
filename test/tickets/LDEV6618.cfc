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
		describe( "LDEV-6618 model auto: pattern matching", function() {

			it( "isAuto", function() {
				expect( resolver.isAuto( "auto" ) ).toBeTrue();
				expect( resolver.isAuto( " AUTO " ) ).toBeTrue();
				expect( resolver.isAuto( "gpt-5" ) ).toBeFalse();
				expect( resolver.isAuto( "" ) ).toBeFalse();
			});

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

		describe( "LDEV-6618 model auto: selection", function() {

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
				expect( resolver.select( available, [ "mistral*", "claude-sonnet*", "gpt-5*" ], "", [] ) ).toBe( "claude-sonnet-4-5-20250929" );
				expect( resolver.select( available, [ "llama-3.3-70b*" ], "", [ "gpt-*" ] ) ).toBe( "llama-3.3-70b-versatile" );
			});

			it( "an exact model name in the preference works", function() {
				expect( resolver.select( available, [ "gpt-5-mini" ], "", [] ) ).toBe( "gpt-5-mini" );
			});

			it( "the provider auto model is used when no preference matches", function() {
				expect( resolver.select( available, [ "mistral*" ], "openrouter/auto", [ "gpt-*" ] ) ).toBe( "openrouter/auto" );
				expect( resolver.select( available, [ "gpt-5*" ], "openrouter/auto", [] ) ).toBe( "gpt-5.1" );
			});

			it( "the built-in fallback skips non chat models", function() {
				expect( resolver.select( [ "text-embedding-3-large", "gpt-4o", "gpt-4.1-mini" ], [], "", [ "*" ] ) ).toBe( "gpt-4.1-mini" );
				expect( resolver.select( available, [], "", [ "claude-sonnet-*", "claude-*" ] ) ).toBe( "claude-sonnet-4-5-20250929" );
			});

			it( "without a match the first chat model of the provider is used", function() {
				expect( resolver.select( [ "whisper-1", "qwen3:8b", "gemma3" ], [], "", [ "gpt-*" ] ) ).toBe( "qwen3:8b" );
			});

			it( "returns null when nothing fits", function() {
				expect( isNull( resolver.select( [ "whisper-1" ], [], "", [] ) ) ).toBeTrue();
				expect( isNull( resolver.select( [], [], "", [] ) ) ).toBeTrue();
			});

			it( "when the models can not be listed, explicit names are used", function() {
				expect( resolver.select( javaCast( "null", "" ), [ "gpt-5*", "gpt-5-mini" ], "", [] ) ).toBe( "gpt-5-mini" );
				expect( resolver.select( javaCast( "null", "" ), [], "", [ "claude-*", "sonar" ] ) ).toBe( "sonar" );
				expect( resolver.select( javaCast( "null", "" ), [ "gpt-5*" ], "openrouter/auto", [] ) ).toBe( "openrouter/auto" );
				expect( isNull( resolver.select( javaCast( "null", "" ), [ "gpt-5*" ], "", [ "gpt-*" ] ) ) ).toBeTrue();
			});
		});

		describe( title="LDEV-6618 model auto: with a mock provider", skip=skipAimock(), body=function() {

			it( "AIGetModels lists the models of an endpoint", function() {
				var mockURL = getMockURL();
				createConnection( "ldev6618-openai", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: mockURL & "/v1/", secretKey: "mock", model: "gpt-4" } );
				var models = AIGetModels( "ldev6618-openai" );
				expect( isQuery( models ) ).toBeTrue();
				expect( models.recordCount ).toBeGT( 0 );
				expect( queryColumnData( models, "name" ) ).toInclude( "gpt-4" );
				expect( LuceeAIGetModels( "ldev6618-openai" ).recordCount ).toBe( models.recordCount );
			});

			it( "model auto with a modelPreference resolves the model", function() {
				var mockURL = getMockURL();
				createConnection( "ldev6618-openai", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: mockURL & "/v1/", secretKey: "mock", model: "auto", modelPreference: "mistral*,gpt-4*" } );
				var models = queryColumnData( AIGetModels( "ldev6618-openai" ), "name" );
				var expected = createObject( "java", "lucee.runtime.ai.AIModelResolver" ).best( models, "gpt-4*", false );
				expect( AIGetMetaData( "ldev6618-openai" ).model ).toBe( expected );
			});

			it( "model auto without a preference uses the built-in fallback (Claude)", function() {
				var mockURL = getMockURL();
				createConnection( "ldev6618-claude", "lucee.runtime.ai.anthropic.ClaudeEngine", { type: "other", url: mockURL & "/v1/", apiKey: "mock", model: "auto" } );
				var models = queryColumnData( AIGetModels( "ldev6618-claude" ), "name" );
				var expected = createObject( "java", "lucee.runtime.ai.AIModelResolver" ).select( models, [], "", [ "claude-sonnet-*", "claude-opus-*", "claude-*" ] );
				expect( AIGetMetaData( "ldev6618-claude" ).model ).toBe( expected );
				expect( AIGetMetaData( "ldev6618-claude" ).model ).notToBe( "auto" );
			});
		});

		describe( title="LDEV-6618 model auto: real providers (only with API keys)", body=function() {

			it( title="Groq", skip=isEmpty( getKey( "GROQ_API_KEY" ) ), body=function() {
				createConnection( "ldev6618-groq", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: "https://api.groq.com/openai/v1/", secretKey: getKey( "GROQ_API_KEY" ), model: "auto" } );
				try {
					var model = AIGetMetaData( "ldev6618-groq" ).model;
					expect( model ).notToBe( "auto" );
					expect( queryColumnData( AIGetModels( "ldev6618-groq" ), "name" ) ).toInclude( model );
				}
				finally {
					admin action="removeAIConnection" type="server" password="#server.SERVERADMINPASSWORD#" name="ldev6618-groq";
				}
			});

			it( title="OpenRouter", skip=isEmpty( getKey( "OPENROUTER_API_KEY" ) ), body=function() {
				createConnection( "ldev6618-openrouter", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: "https://openrouter.ai/api/v1/", secretKey: getKey( "OPENROUTER_API_KEY" ), model: "auto" } );
				try {
					expect( AIGetMetaData( "ldev6618-openrouter" ).model ).toBe( "openrouter/auto" );
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
