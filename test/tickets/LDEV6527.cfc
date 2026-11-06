component extends="org.lucee.cfml.test.LuceeTestCase" labels="ai" {

	variables.names = [ "ldev6527-openai", "ldev6527-claude", "ldev6527-gemini", "ldev6527-nostreamopts", "ldev6527-nousage", "ldev6527-down" ];

	function beforeAll() {
		variables.dir = getTempDirectory() & "ldev6527-" & createUUID() & "/";
		directoryCreate( variables.dir );
		variables.logFile = variables.dir & "ai.log";

		variables.handler = new LDEV6527.MockProvider();
		variables.handler.start();
		variables.base = "http://127.0.0.1:#variables.handler.getPort()#";

		var pw = server.SERVERADMINPASSWORD;
		createConnection( "ldev6527-openai", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: base & "/openai/v1/", secretKey: "mock", model: "gpt-mock" } );
		createConnection( "ldev6527-nostreamopts", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: base & "/openai/v1/", secretKey: "mock", model: "nostreamopts" } );
		createConnection( "ldev6527-nousage", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: base & "/openai/v1/", secretKey: "mock", model: "nousage" } );
		createConnection( "ldev6527-claude", "lucee.runtime.ai.anthropic.ClaudeEngine", { type: "other", url: base & "/claude/v1/", apiKey: "mock", model: "claude-mock" } );
		createConnection( "ldev6527-gemini", "lucee.runtime.ai.google.GeminiEngine", { type: "other", url: base & "/gemini/", apiKey: "mock", model: "gemini-mock" } );
		createConnection( "ldev6527-down", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: "http://127.0.0.1:1/v1/", secretKey: "mock", model: "gpt-mock" } );

		// write the ai log to a file we can read
		admin action="getLogSettings" type="server" password="#pw#" returnVariable="local.logs";
		variables.hadAILog = false;
		loop query="local.logs" {
			if ( local.logs.name == "ai" ) {
				variables.hadAILog = true;
				variables.origLog = { level: local.logs.level, appenderClass: local.logs.appenderClass, appenderArgs: local.logs.appenderArgs, layoutClass: local.logs.layoutClass, layoutArgs: local.logs.layoutArgs };
			}
		}
		admin action="updateLogSettings" type="server" password="#pw#" name="ai" level="info" appenderClass="resource" appenderArgs={ "path": variables.logFile } layoutClass="classic" layoutArgs={};
	}

	function afterAll() {
		var pw = server.SERVERADMINPASSWORD;
		if ( !isNull( variables.handler ) ) variables.handler.stop();
		for ( var name in variables.names ) {
			try {
				admin action="removeAIConnection" type="server" password="#pw#" name="#name#";
			}
			catch ( any e ) {
				systemOutput( "could not remove the AI connection [#name#]: #e.message#", true );
			}
		}
		if ( variables.hadAILog ) {
			admin action="updateLogSettings" type="server" password="#pw#" name="ai" level="#variables.origLog.level#" appenderClass="#variables.origLog.appenderClass#" appenderArgs=variables.origLog.appenderArgs layoutClass="#variables.origLog.layoutClass#" layoutArgs=variables.origLog.layoutArgs;
		}
		else {
			admin action="removeLogSetting" type="server" password="#pw#" name="ai";
		}
		if ( !isNull( variables.dir ) && directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-6527 normalizing the usage of the providers", function() {

			it( "OpenAI: prompt includes cached, completion includes reasoning", function() {
				var u = usageClass().fromOpenAI( { prompt_tokens: 20, completion_tokens: 5, total_tokens: 25, prompt_tokens_details: { cached_tokens: 8 }, completion_tokens_details: { reasoning_tokens: 2 } } );
				expect( [ u.input, u.output, u.total, u.cached, u.reasoning ] ).toBe( [ 20, 5, 25, 8, 2 ] );
			});

			it( "values the provider does not report are unknown, not 0", function() {
				var u = usageClass().fromOpenAI( { prompt_tokens: 20, completion_tokens: 5 } );
				expect( [ u.input, u.output, u.total, u.cached, u.reasoning ] ).toBe( [ 20, 5, 25, -1, -1 ] );
			});

			it( "Claude: cache tokens are reported separately and added to input", function() {
				var u = usageClass().fromClaude( { input_tokens: 25, cache_read_input_tokens: 10, cache_creation_input_tokens: 5, output_tokens: 15 } );
				expect( [ u.input, u.output, u.total, u.cached ] ).toBe( [ 40, 15, 55, 10 ] );
			});

			it( "Gemini: thinking tokens are added to output", function() {
				var u = usageClass().fromGemini( { promptTokenCount: 30, candidatesTokenCount: 10, thoughtsTokenCount: 4, totalTokenCount: 44, cachedContentTokenCount: 6 } );
				expect( [ u.input, u.output, u.total, u.cached, u.reasoning ] ).toBe( [ 30, 14, 44, 6, 4 ] );
			});

			it( "no usage or only zeros is not reported", function() {
				expect( isNull( usageClass().fromOpenAI( {} ) ) ).toBeTrue();
				expect( isNull( usageClass().fromOpenAI( { prompt_tokens: 0, completion_tokens: 0, total_tokens: 0 } ) ) ).toBeTrue();
				expect( isNull( usageClass().fromClaude( { input_tokens: 0, output_tokens: 0 } ) ) ).toBeTrue();
				expect( isNull( usageClass().fromGemini( {} ) ) ).toBeTrue();
			});
		});

		describe( "LDEV-6527 AIGetUsage", function() {

			it( "a new session has no usage", function() {
				var usage = AIGetUsage( createAISession( name="ldev6527-openai" ) );
				expect( usage.requests ).toBe( 0 );
				expect( usage.total ).toBe( 0 );
				expect( usage ).notToHaveKey( "last" );
			});

			it( "the aliases exist", function() {
				var sess = createAISession( name="ldev6527-openai" );
				expect( LuceeAIGetUsage( sess ).requests ).toBe( 0 );
				expect( aiSessionUsage( sess ).requests ).toBe( 0 );
			});

			it( "needs a session", function() {
				expect( function() {
					AIGetUsage( "ldev6527-openai" );
				}).toThrow();
			});

			it( title="OpenAI compatible, not streamed", body=function() {
				var sess = createAISession( name="ldev6527-openai" );
				expect( inquiryAISession( sess, "question" ) ).toBe( "Hello" );
				assertUsage( sess, { input: 20, output: 5, total: 25, cached: 8, reasoning: 2 }, "gpt-mock-2024", false );
			});

			it( title="OpenAI compatible, streamed (used to be 0)", body=function() {
				variables.handler.reset();
				var sess = createAISession( name="ldev6527-openai" );
				var parts = [];
				inquiryAISession( sess, "question", function( text ) {
					arrayAppend( parts, text );
				});
				expect( arrayToList( parts, "" ) ).toBe( "Hello" );
				assertUsage( sess, { input: 20, output: 5, total: 25, cached: 8, reasoning: 2 }, "gpt-mock-2024", true );
				var req = variables.handler.getRequests()[ 1 ].body;
				expect( req.stream_options.include_usage ).toBeTrue();
				expect( variables.handler.getErrors() ).toBeEmpty();
			});

			it( title="OpenAI compatible, a provider that rejects stream_options still streams", body=function() {
				variables.handler.reset();
				var sess = createAISession( name="ldev6527-nostreamopts" );
				var parts = [];
				inquiryAISession( sess, "question", function( text ) {
					arrayAppend( parts, text );
				});
				expect( arrayToList( parts, "" ) ).toBe( "Hello" );
				var usage = AIGetUsage( sess );
				expect( usage.requests ).toBe( 1 );
				expect( usage.unreportedRequests ).toBe( 1 );
				expect( variables.handler.getRequests() ).toHaveLength( 2 );
				expect( variables.handler.getRequests()[ 2 ].body ).notToHaveKey( "stream_options" );

				// it is remembered
				inquiryAISession( sess, "question", function() {} );
				expect( variables.handler.getRequests() ).toHaveLength( 3 );
				expect( variables.handler.getRequests()[ 3 ].body ).notToHaveKey( "stream_options" );
			});

			it( title="a provider without usage is counted as unreported, not as 0 tokens", body=function() {
				var sess = createAISession( name="ldev6527-nousage" );
				inquiryAISession( sess, "question" );
				var usage = AIGetUsage( sess );
				expect( usage.requests ).toBe( 1 );
				expect( usage.unreportedRequests ).toBe( 1 );
				expect( usage.total ).toBe( 0 );
				expect( usage.last ).notToHaveKey( "input" );
				expect( usage.last ).notToHaveKey( "total" );
				expect( usage.last.estimated ).toBeFalse();
			});

			it( title="Claude, not streamed", body=function() {
				var sess = createAISession( name="ldev6527-claude" );
				expect( inquiryAISession( sess, "question" ) ).toBe( "Hello" );
				assertUsage( sess, { input: 35, output: 15, total: 50, cached: 10 }, "claude-mock-1", false );
			});

			it( title="Claude, streamed (used to be 0)", body=function() {
				var sess = createAISession( name="ldev6527-claude" );
				var parts = [];
				inquiryAISession( sess, "question", function( text ) {
					arrayAppend( parts, text );
				});
				expect( arrayToList( parts, "" ) ).toBe( "Hello" );
				assertUsage( sess, { input: 35, output: 15, total: 50, cached: 10 }, "claude-mock-1", true );
			});

			it( title="Gemini, not streamed", body=function() {
				var sess = createAISession( name="ldev6527-gemini" );
				expect( inquiryAISession( sess, "question" ) ).toBe( "Hello" );
				assertUsage( sess, { input: 30, output: 14, total: 44, cached: 6, reasoning: 4 }, "gemini-mock-1", false );
			});

			it( title="Gemini, streamed", body=function() {
				var sess = createAISession( name="ldev6527-gemini" );
				var parts = [];
				inquiryAISession( sess, "question", function( text ) {
					arrayAppend( parts, text );
				});
				expect( arrayToList( parts, "" ) ).toBe( "Hello" );
				assertUsage( sess, { input: 30, output: 14, total: 44, cached: 6, reasoning: 4 }, "gemini-mock-1", true );
			});

			it( "the usage adds up over the calls of a session, also when the history is trimmed", function() {
				var sess = createAISession( name="ldev6527-openai", limit=1 );
				inquiryAISession( sess, "one" );
				inquiryAISession( sess, "two", function() {} );
				inquiryAISession( sess, "three" );
				var usage = AIGetUsage( sess );
				expect( usage.requests ).toBe( 3 );
				expect( usage.input ).toBe( 60 );
				expect( usage.output ).toBe( 15 );
				expect( usage.total ).toBe( 75 );
				expect( usage.cached ).toBe( 24 );
				expect( usage.reasoning ).toBe( 6 );
				expect( SerializeAISession( sess ) ).toInclude( "usage" );
			});

			it( "sessions do not share the usage", function() {
				var a = createAISession( name="ldev6527-openai" );
				var b = createAISession( name="ldev6527-openai" );
				inquiryAISession( a, "one" );
				expect( AIGetUsage( a ).requests ).toBe( 1 );
				expect( AIGetUsage( b ).requests ).toBe( 0 );
			});

			it( "a failed call is counted as failed and not as a request", function() {
				var sess = createAISession( name="ldev6527-down" );
				expect( function() {
					inquiryAISession( sess, "question" );
				}).toThrow();
				var usage = AIGetUsage( sess );
				expect( usage.failedRequests ).toBe( 1 );
				expect( usage.requests ).toBe( 0 );
				expect( usage.total ).toBe( 0 );
			});

			it( "SerializeAISession and LoadAISession carry the usage", function() {
				var sess = createAISession( name="ldev6527-openai" );
				inquiryAISession( sess, "one" );
				inquiryAISession( sess, "two" );
				var data = deserializeJSON( SerializeAISession( sess ) );
				expect( data.usage.requests ).toBe( 2 );
				expect( data.usage.total ).toBe( 50 );

				var loaded = LoadAISession( "ldev6527-openai", serializeJSON( data ) );
				expect( AIGetUsage( loaded ).requests ).toBe( 2 );
				expect( AIGetUsage( loaded ).total ).toBe( 50 );
				inquiryAISession( loaded, "three" );
				expect( AIGetUsage( loaded ).total ).toBe( 75 );
			});

			it( "old serialized data without usage can still be loaded", function() {
				var sess = createAISession( name="ldev6527-openai" );
				inquiryAISession( sess, "one" );
				var data = deserializeJSON( SerializeAISession( sess ) );
				structDelete( data, "usage" );
				var loaded = LoadAISession( "ldev6527-openai", serializeJSON( data ) );
				expect( AIGetUsage( loaded ).requests ).toBe( 0 );
			});
		});

		describe( "LDEV-6527 usage log", function() {

			it( "writes one parseable line per call to the ai log without the prompt", function() {
				var token = createUUID();
				var sess = createAISession( name="ldev6527-openai" );
				inquiryAISession( sess, "very private prompt #token#" );
				var line = findLine( "gen_ai.conversation.id=" & sess.getId() );
				expect( line ).toInclude( "gen_ai.provider.name=openai" );
				expect( line ).toInclude( "gen_ai.request.model=gpt-mock" );
				expect( line ).toInclude( "gen_ai.response.model=gpt-mock-2024" );
				expect( line ).toInclude( "gen_ai.usage.input_tokens=20" );
				expect( line ).toInclude( "gen_ai.usage.output_tokens=5" );
				expect( line ).toInclude( "gen_ai.usage.total_tokens=25" );
				expect( line ).toInclude( "gen_ai.usage.cache_read.input_tokens=8" );
				expect( line ).toInclude( "gen_ai.usage.reasoning.output_tokens=2" );
				expect( line ).toInclude( "gen_ai.request.stream=false" );
				expect( line ).toInclude( "ai.connection=ldev6527-openai" );
				expect( reFind( "duration_ms=\d+", line ) ).toBeGT( 0 );
				expect( fileRead( variables.logFile ) ).notToInclude( token );
			});

			it( "a call with the usage not reported is logged as such", function() {
				var sess = createAISession( name="ldev6527-nousage" );
				inquiryAISession( sess, "one" );
				var line = findLine( "gen_ai.conversation.id=" & sess.getId() );
				expect( line ).toInclude( "gen_ai.usage.reported=false" );
				expect( line ).notToInclude( "input_tokens" );
			});

			it( "a failed call is logged as WARN", function() {
				var sess = createAISession( name="ldev6527-down" );
				try {
					inquiryAISession( sess, "one" );
				}
				catch ( any e ) {}
				var line = findLine( "gen_ai.conversation.id=" & sess.getId() );
				expect( line ).toInclude( "status=failed" );
				expect( line ).toInclude( "error.type=" );
				expect( line ).toInclude( "WARN" );
			});
		});
	}

	private function assertUsage( required sess, required struct expected, required string model, required boolean streamed ) {
		var usage = AIGetUsage( arguments.sess );
		expect( usage.requests ).toBe( 1 );
		expect( usage.unreportedRequests ).toBe( 0 );
		for ( var key in [ "input", "output", "total", "cached", "reasoning" ] ) {
			expect( usage[ key ] ).toBe( arguments.expected[ key ] ?: 0, "total [#key#]" );
			if ( structKeyExists( arguments.expected, key ) ) expect( usage.last[ key ] ).toBe( arguments.expected[ key ], "last [#key#]" );
			else expect( usage.last ).notToHaveKey( key );
		}
		expect( usage.last.model ).toBe( arguments.model );
		expect( usage.last.streamed ).toBe( arguments.streamed );
		expect( usage.last.durationMs ).toBeGTE( 0 );
		expect( usage.last.estimated ).toBeFalse();
		expect( variables.handler.getErrors() ).toBeEmpty();
	}

	private function usageClass() {
		return createObject( "java", "lucee.runtime.ai.AIUsage" );
	}

	private string function findLine( required string needle ) {
		// the log is written asynchronously
		var tries = 0;
		while ( tries++ < 30 ) {
			if ( fileExists( variables.logFile ) ) {
				for ( var line in listToArray( fileRead( variables.logFile ), chr( 10 ) ) ) {
					if ( line contains arguments.needle ) return line;
				}
			}
			sleep( 100 );
		}
		fail( "no line with [#arguments.needle#] in the log [#variables.logFile#]" );
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
