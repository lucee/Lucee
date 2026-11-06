component extends="org.lucee.cfml.test.LuceeTestCase" labels="ai" {

	variables.names = [ "ldev6528-none", "ldev6528-session", "ldev6528-log", "ldev6528-warn", "ldev6528-request", "ldev6528-request-log", "ldev6528-claude", "ldev6528-gemini", "ldev6528-invalid" ];

	function beforeAll() {
		variables.dir = getTempDirectory() & "ldev6528-" & createUUID() & "/";
		directoryCreate( variables.dir );
		variables.logFile = variables.dir & "ai.log";

		variables.handler = new LDEV6527.MockProvider();
		variables.handler.start();
		variables.base = "http://127.0.0.1:#variables.handler.getPort()#";

		var openai = { type: "other", url: base & "/openai/v1/", secretKey: "mock", model: "gpt-mock" };
		var claude = { type: "other", url: base & "/claude/v1/", apiKey: "mock", model: "claude-mock" };
		var gemini = { type: "other", url: base & "/gemini/", apiKey: "mock", model: "gemini-mock" };
		var oai = "lucee.runtime.ai.openai.OpenAIEngine";

		// every call of the mock uses 25 tokens (OpenAI: 20 in, 5 out)
		createConnection( "ldev6528-none", oai, openai );
		createConnection( "ldev6528-session", oai, duplicate( openai ).append( { maxTokensPerSession: 60 } ) );
		createConnection( "ldev6528-log", oai, duplicate( openai ).append( { maxTokensPerSession: "30", tokenLimitAction: "log" } ) );
		createConnection( "ldev6528-warn", oai, duplicate( openai ).append( { maxTokensPerSession: 100, tokenWarningThreshold: 0.5 } ) );
		createConnection( "ldev6528-request", oai, duplicate( openai ).append( { maxTokensPerRequest: 15 } ) );
		createConnection( "ldev6528-request-log", oai, duplicate( openai ).append( { maxTokensPerRequest: 15, tokenLimitAction: "log" } ) );
		createConnection( "ldev6528-claude", "lucee.runtime.ai.anthropic.ClaudeEngine", duplicate( claude ).append( { maxTokensPerRequest: 30 } ) );
		createConnection( "ldev6528-gemini", "lucee.runtime.ai.google.GeminiEngine", duplicate( gemini ).append( { maxTokensPerRequest: 40 } ) );

		// write the ai log to a file we can read
		var pw = server.SERVERADMINPASSWORD;
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

		describe( "LDEV-6528 maxTokensPerSession", function() {

			it( "refuses the call when the session limit is reached and does not send it", function() {
				variables.handler.reset();
				var sess = createAISession( name="ldev6528-session" );
				// 25, 50 and 75 tokens, the limit is 60, so the third call is still allowed
				inquiryAISession( sess, "one" );
				inquiryAISession( sess, "two" );
				inquiryAISession( sess, "three" );
				expect( AIGetUsage( sess ).total ).toBe( 75 );
				expect( variables.handler.getRequests() ).toHaveLength( 3 );

				var e = catchLimit( sess );
				expect( e.type ).toBe( "application" );
				expect( e.errorcode ).toBe( "ai.token.limit.exceeded" );
				expect( e.message ).toInclude( "maxTokensPerSession=60" );
				expect( e.message ).toInclude( "ldev6528-session" );
				expect( e.message ).toInclude( "75 tokens" );
				expect( variables.handler.getRequests() ).toHaveLength( 3 );
				// a refused call is not a request
				var usage = AIGetUsage( sess );
				expect( usage.requests ).toBe( 3 );
				expect( usage.failedRequests ).toBe( 0 );
				expect( usage.total ).toBe( 75 );
			});

			it( "the limit is per session, a new session starts again", function() {
				var a = createAISession( name="ldev6528-session" );
				var b = createAISession( name="ldev6528-session" );
				for ( var i = 1; i <= 3; i++ ) inquiryAISession( a, "q" & i );
				catchLimit( a );
				expect( inquiryAISession( b, "one" ) ).toBe( "Hello" );
			});

			it( "applies to streamed inquiries as well", function() {
				variables.handler.reset();
				var sess = createAISession( name="ldev6528-session" );
				for ( var i = 1; i <= 3; i++ ) inquiryAISession( sess, "q" & i );
				var parts = [];
				var listener = function( part ) { arrayAppend( parts, part ); };
				var caught = {};
				try {
					inquiryAISession( sess, "stream", listener );
				}
				catch ( any ex ) {
					caught = ex;
				}
				expect( caught ).toHaveKey( "errorcode" );
				expect( caught.errorcode ).toBe( "ai.token.limit.exceeded" );
				expect( parts ).toBeEmpty();
				expect( variables.handler.getRequests() ).toHaveLength( 3 );
			});

			it( "a loaded session continues where the serialized one stopped", function() {
				var sess = createAISession( name="ldev6528-session" );
				for ( var i = 1; i <= 3; i++ ) inquiryAISession( sess, "q" & i );
				var loaded = LoadAISession( "ldev6528-session", SerializeAISession( sess ) );
				expect( AIGetUsage( loaded ).total ).toBe( 75 );
				catchLimit( loaded );
			});

			it( "a limit of 0 or no limit never refuses", function() {
				var sess = createAISession( name="ldev6528-none" );
				for ( var i = 1; i <= 6; i++ ) inquiryAISession( sess, "q" & i );
				expect( AIGetUsage( sess ).total ).toBe( 150 );
				expect( AIGetMetaData( sess ) ).notToHaveKey( "tokenLimits" );
			});

			it( "is part of the metadata of the connection", function() {
				var limits = AIGetMetaData( createAISession( name="ldev6528-session" ) ).tokenLimits;
				expect( limits.maxTokensPerSession ).toBe( 60 );
				expect( limits.tokenLimitAction ).toBe( "throw" );
				expect( limits ).notToHaveKey( "maxTokensPerRequest" );
				limits = AIGetMetaData( createAISession( name="ldev6528-warn" ) ).tokenLimits;
				expect( limits.tokenWarningThreshold ).toBe( 0.5 );
			});
		});

		describe( "LDEV-6528 tokenLimitAction=log", function() {

			it( "writes a WARN and still runs the call", function() {
				variables.handler.reset();
				var sess = createAISession( name="ldev6528-log" );
				// 25 tokens, below the limit of 30, then 50 and 75 above it
				for ( var i = 1; i <= 3; i++ ) expect( inquiryAISession( sess, "q" & i ) ).toBe( "Hello" );
				expect( variables.handler.getRequests() ).toHaveLength( 3 );
				expect( AIGetUsage( sess ).total ).toBe( 75 );

				var line = findLine( "ai.limit=maxTokensPerSession", sess.getId() );
				expect( line ).toInclude( "WARN" );
				expect( line ).toInclude( "ai.limit.max=30" );
				expect( line ).toInclude( "ai.limit.used=50" );
				expect( line ).toInclude( "ai.limit.action=log" );
				expect( line ).toInclude( "ai.limit.phase=before" );
				expect( line ).toInclude( "ai.connection=ldev6528-log" );
			});
		});

		describe( "LDEV-6528 tokenWarningThreshold", function() {

			it( "writes one WARN per session when the threshold is crossed, the calls are not refused", function() {
				var sess = createAISession( name="ldev6528-warn" );
				// threshold is 50 of 100 tokens
				inquiryAISession( sess, "one" );
				sleep( 300 );
				expect( countLines( "ai.limit=tokenWarningThreshold", sess.getId() ) ).toBe( 0 );
				inquiryAISession( sess, "two" );
				inquiryAISession( sess, "three" );
				var line = findLine( "ai.limit=tokenWarningThreshold", sess.getId() );
				expect( line ).toInclude( "WARN" );
				expect( line ).toInclude( "ai.limit.max=100" );
				expect( line ).toInclude( "ai.limit.used=50" );
				sleep( 300 );
				expect( countLines( "ai.limit=tokenWarningThreshold", sess.getId() ) ).toBe( 1 );
			});

			it( "a loaded session that is already above the threshold does not warn again", function() {
				var sess = createAISession( name="ldev6528-warn" );
				inquiryAISession( sess, "one" );
				inquiryAISession( sess, "two" );
				findLine( "ai.limit=tokenWarningThreshold", sess.getId() );
				var loaded = LoadAISession( "ldev6528-warn", SerializeAISession( sess ) );
				inquiryAISession( loaded, "three" );
				sleep( 300 );
				// the loaded session has its own id
				expect( countLines( "ai.limit=tokenWarningThreshold", loaded.getId() ) ).toBe( 0 );
			});
		});

		describe( "LDEV-6528 maxTokensPerRequest", function() {

			it( "is not sent to the provider as is, but limits the answer", function() {
				variables.handler.reset();
				inquiryAISession( createAISession( name="ldev6528-request" ), "one" );
				var body = variables.handler.getRequests()[ 1 ].body;
				expect( body ).notToHaveKey( "maxTokensPerRequest" );
				expect( body ).notToHaveKey( "tokenLimitAction" );
				expect( body.max_tokens ).toBe( 15 );
				expect( body ).notToHaveKey( "max_completion_tokens" );
			});

			it( "the settings are never sent to the provider", function() {
				variables.handler.reset();
				inquiryAISession( createAISession( name="ldev6528-warn" ), "one" );
				inquiryAISession( createAISession( name="ldev6528-session" ), "one" );
				inquiryAISession( createAISession( name="ldev6528-log" ), "one" );
				for ( var req in variables.handler.getRequests() ) {
					for ( var key in [ "maxTokensPerSession", "maxTokensPerRequest", "tokenLimitAction", "tokenWarningThreshold" ] ) {
						expect( req.body ).notToHaveKey( key );
					}
					expect( req.body ).notToHaveKey( "max_tokens" );
				}
			});

			it( "refuses the next call when the request before already sent as much as the limit", function() {
				variables.handler.reset();
				var sess = createAISession( name="ldev6528-request" );
				// the first request is 20 input tokens, the limit is 15
				inquiryAISession( sess, "one" );
				var e = catchLimit( sess );
				expect( e.message ).toInclude( "maxTokensPerRequest=15" );
				expect( e.message ).toInclude( "20 input tokens" );
				expect( variables.handler.getRequests() ).toHaveLength( 1 );
			});

			it( "with tokenLimitAction=log the call runs and the overrun is logged", function() {
				variables.handler.reset();
				var sess = createAISession( name="ldev6528-request-log" );
				inquiryAISession( sess, "one" );
				inquiryAISession( sess, "two" );
				expect( variables.handler.getRequests() ).toHaveLength( 2 );
				var line = findLine( "ai.limit.phase=after", sess.getId() );
				expect( line ).toInclude( "ai.limit=maxTokensPerRequest" );
				expect( line ).toInclude( "ai.limit.max=15" );
				expect( line ).toInclude( "ai.limit.used=25" );
			});

			it( "Claude: max_tokens is the limit when it is lower than the default of 4096", function() {
				variables.handler.reset();
				var sess = createAISession( name="ldev6528-claude" );
				inquiryAISession( sess, "one" );
				var body = variables.handler.getRequests()[ 1 ].body;
				expect( body.max_tokens ).toBe( 30 );
				expect( body ).notToHaveKey( "maxTokensPerRequest" );
				// 35 input tokens
				catchLimit( sess );
			});

			it( "Gemini: maxOutputTokens is the limit", function() {
				variables.handler.reset();
				inquiryAISession( createAISession( name="ldev6528-gemini" ), "one" );
				var body = variables.handler.getRequests()[ 1 ].body;
				expect( body.generationConfig.maxOutputTokens ).toBe( 40 );
				expect( body ).notToHaveKey( "maxTokensPerRequest" );
			});

			it( "without a limit nothing changes for the provider", function() {
				variables.handler.reset();
				inquiryAISession( createAISession( name="ldev6528-none" ), "one" );
				var body = variables.handler.getRequests()[ 1 ].body;
				expect( body ).notToHaveKey( "max_tokens" );
				expect( body ).notToHaveKey( "max_completion_tokens" );
			});
		});

		describe( "LDEV-6528 invalid settings", function() {

			it( "a limit that is not a whole number is refused", function() {
				for ( var value in [ "abc", -5, 1.5, true ] ) {
					assertInvalid( { maxTokensPerSession: value }, "maxTokensPerSession" );
					assertInvalid( { maxTokensPerRequest: value }, "maxTokensPerRequest" );
				}
			});

			it( "tokenLimitAction has to be throw or log", function() {
				assertInvalid( { maxTokensPerSession: 10, tokenLimitAction: "ignore" }, "tokenLimitAction" );
			});

			it( "tokenWarningThreshold has to be a fraction", function() {
				assertInvalid( { maxTokensPerSession: 10, tokenWarningThreshold: 1.5 }, "tokenWarningThreshold" );
				assertInvalid( { maxTokensPerSession: 10, tokenWarningThreshold: "high" }, "tokenWarningThreshold" );
			});
		});
	}

	private function assertInvalid( required struct settings, required string name ) {
		var custom = { type: "other", url: variables.base & "/openai/v1/", secretKey: "mock", model: "gpt-mock" };
		custom.append( arguments.settings );
		var caught = {};
		try {
			createConnection( "ldev6528-invalid", "lucee.runtime.ai.openai.OpenAIEngine", custom );
			createAISession( name="ldev6528-invalid" );
		}
		catch ( any e ) {
			caught = e;
		}
		expect( caught ).toHaveKey( "message", "no error for #serializeJSON( arguments.settings )#" );
		expect( caught.message ).toInclude( arguments.name );
	}

	private struct function catchLimit( required sess ) {
		var caught = {};
		try {
			inquiryAISession( arguments.sess, "over the limit" );
		}
		catch ( any e ) {
			caught = e;
		}
		expect( caught ).toHaveKey( "errorcode", "the call was not refused" );
		expect( caught.errorcode ).toBe( "ai.token.limit.exceeded" );
		return caught;
	}

	private array function readLines() {
		if ( !fileExists( variables.logFile ) ) return [];
		return listToArray( fileRead( variables.logFile ), chr( 10 ) );
	}

	private numeric function countLines( required string needle, required string sessionId ) {
		var count = 0;
		for ( var line in readLines() ) {
			if ( line contains arguments.needle && line contains "gen_ai.conversation.id=" & arguments.sessionId ) count++;
		}
		return count;
	}

	private string function findLine( required string needle, required string sessionId ) {
		// the log is written asynchronously
		var tries = 0;
		while ( tries++ < 30 ) {
			for ( var line in readLines() ) {
				if ( line contains arguments.needle && line contains "gen_ai.conversation.id=" & arguments.sessionId ) return line;
			}
			sleep( 100 );
		}
		fail( "no line with [#arguments.needle#] for the session [#arguments.sessionId#] in the log [#variables.logFile#]" );
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
