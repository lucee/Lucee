component extends="org.lucee.cfml.test.LuceeTestCase" labels="ai" {

	variables.names = [ "ldev6540-openai", "ldev6540-claude", "ldev6540-gemini" ];

	function beforeAll() {
		variables.dir = getTempDirectory() & "ldev6540-" & createUUID() & "/";
		directoryCreate( variables.dir );
		variables.logFile = variables.dir & "ai.log";

		variables.handler = new LDEV6527.MockProvider();
		variables.handler.start();
		variables.base = "http://127.0.0.1:#variables.handler.getPort()#";

		createConnection( "ldev6540-openai", "lucee.runtime.ai.openai.OpenAIEngine", { type: "other", url: base & "/openai/v1/", secretKey: "mock", model: "gpt-mock" } );
		createConnection( "ldev6540-claude", "lucee.runtime.ai.anthropic.ClaudeEngine", { type: "other", url: base & "/claude/v1/", apiKey: "mock", model: "claude-mock" } );
		createConnection( "ldev6540-gemini", "lucee.runtime.ai.google.GeminiEngine", { type: "other", url: base & "/gemini/", apiKey: "mock", model: "gemini-mock" } );

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

		describe( "LDEV-6540 onBeforePromptSent, cancel", function() {

			it( "cancels the request when the hook returns false, nothing is sent", function() {
				each( providers(), function( p ) {
					variables.handler.reset();
					var sess = createAISession( name=p.name, listener={ onBeforePromptSent: function( event ) { return false; } } );
					var e = catchError( function() { inquiryAISession( sess, "hello" ); } );
					expect( e.type ).toBe( "application" );
					expect( e.errorcode ).toBe( "ai.listener.cancelled" );
					expect( e.message ).toInclude( p.name );
					expect( variables.handler.getRequests() ).toHaveLength( 0, p.name );
				} );
			});

			it( "a cancelled request is not a request and leaves no trace in the session", function() {
				each( providers(), function( p ) {
					var sess = createAISession( name=p.name, listener={ onBeforePromptSent: function( event ) { return false; } } );
					catchError( function() { inquiryAISession( sess, "cancel me" ); } );
					var usage = AIGetUsage( sess );
					expect( usage.requests ).toBe( 0, p.name );
					expect( usage.failedRequests ).toBe( 0, p.name );
					expect( usage.total ).toBe( 0, p.name );
					expect( serializeAISession( sess ) ).notToInclude( "cancel me" );
				} );
			});

			it( "the session is usable after a cancel", function() {
				var state = { cancel: true };
				var sess = createAISession( name="ldev6540-openai", listener={ onBeforePromptSent: function( event ) { return !state.cancel; } } );
				catchError( function() { inquiryAISession( sess, "one" ); } );
				state.cancel = false;
				expect( inquiryAISession( sess, "two" ) ).toBe( "Hello" );
				expect( AIGetUsage( sess ).requests ).toBe( 1 );
			});

			it( "cancels a streamed request, the response listener is never called", function() {
				each( providers(), function( p ) {
					variables.handler.reset();
					var state = { streamed: [] };
					var sess = createAISession( name=p.name, listener={ onBeforePromptSent: function( event ) { arrayAppend( state.streamed, event.stream ); return false; } } );
					var parts = [];
					var e = catchError( function() { inquiryAISession( sess, "stream", function( text ) { arrayAppend( parts, text ); } ); } );
					expect( e.errorcode ).toBe( "ai.listener.cancelled" );
					expect( state.streamed ).toBe( [ true ] );
					expect( parts ).toBeEmpty();
					expect( variables.handler.getRequests() ).toHaveLength( 0, p.name );
				} );
			});

			it( "writes the cancel to the ai log", function() {
				var sess = createAISession( name="ldev6540-openai", listener={ onBeforePromptSent: function( event ) { return false; } } );
				catchError( function() { inquiryAISession( sess, "log me" ); } );
				var found = false;
				for ( var line in listToArray( fileRead( variables.logFile ), chr( 10 ) ) ) {
					if ( line contains "request cancelled by the listener" && line contains "gen_ai.conversation.id=#sess.getId()#" ) found = true;
				}
				expect( found ).toBeTrue();
			});

			it( "an exception of the hook is passed on as it is and nothing is sent", function() {
				each( providers(), function( p ) {
					variables.handler.reset();
					var sess = createAISession( name=p.name, listener={ onBeforePromptSent: function( event ) { throw( type="ldev6540.custom", message="not allowed", errorcode="my.code" ); } } );
					var e = catchError( function() { inquiryAISession( sess, "hello" ); } );
					expect( e.type ).toBe( "ldev6540.custom" );
					expect( e.message ).toBe( "not allowed" );
					expect( e.errorcode ).toBe( "my.code" );
					expect( variables.handler.getRequests() ).toHaveLength( 0, p.name );
					expect( AIGetUsage( sess ).failedRequests ).toBe( 0, p.name );
				} );
			});
		});

		describe( "LDEV-6540 onBeforePromptSent, modify", function() {

			it( "gets the request, the connection, the provider, the model and the stream flag", function() {
				var expected = { "ldev6540-openai": { provider: "openai", model: "gpt-mock" }, "ldev6540-claude": { provider: "anthropic", model: "claude-mock" }, "ldev6540-gemini": { provider: "gcp.gemini", model: "gemini-mock" } };
				each( providers(), function( p ) {
					var seen = [];
					var sess = createAISession( name=p.name, listener={ onBeforePromptSent: function( event ) {
						arrayAppend( seen, { keys: listSort( structKeyList( event ), "text" ), connection: event.connection, provider: event.provider, model: event.model, stream: event.stream, request: isStruct( event.request ), session: isObject( event.session ) } );
					} } );
					inquiryAISession( sess, "hello" );
					inquiryAISession( sess, "hello", function( text ) {} );
					expect( seen ).toHaveLength( 2 );
					expect( seen[ 1 ].keys ).toBe( "connection,model,provider,request,session,stream" );
					expect( seen[ 1 ].connection ).toBe( p.name );
					expect( seen[ 1 ].provider ).toBe( expected[ p.name ].provider );
					expect( seen[ 1 ].model ).toBe( expected[ p.name ].model );
					expect( seen[ 1 ].request ).toBeTrue();
					expect( seen[ 1 ].session ).toBeTrue();
					expect( seen[ 1 ].stream ).toBeFalse();
					expect( seen[ 2 ].stream ).toBeTrue();
				} );
			});

			it( "changes in the event are sent to the provider", function() {
				each( providers(), function( p ) {
					variables.handler.reset();
					var sess = createAISession( name=p.name, listener={ onBeforePromptSent: function( event ) { event.request.listener_marker = "in place"; } } );
					expect( inquiryAISession( sess, "hello" ) ).toBe( "Hello" );
					var requests = variables.handler.getRequests();
					expect( requests ).toHaveLength( 1, p.name );
					expect( requests[ 1 ].body.listener_marker ).toBe( "in place", p.name );
				} );
			});

			it( "a returned struct replaces the request", function() {
				each( providers(), function( p ) {
					variables.handler.reset();
					var sess = createAISession( name=p.name, listener={ onBeforePromptSent: function( event ) {
						var json = replace( serializeJSON( event.request ), "original prompt", "rewritten prompt", "all" );
						return deserializeJSON( json );
					} } );
					expect( inquiryAISession( sess, "original prompt" ) ).toBe( "Hello" );
					var body = serializeJSON( variables.handler.getRequests()[ 1 ].body );
					expect( body ).toInclude( "rewritten prompt", p.name );
					expect( body ).notToInclude( "original prompt", p.name );
					// the history keeps what the caller asked
					expect( serializeAISession( sess ) ).toInclude( "original prompt" );
				} );
			});

			it( "true or nothing continue with the request as it is", function() {
				variables.handler.reset();
				var sess = createAISession( name="ldev6540-openai", listener={ onBeforePromptSent: function( event ) { return true; } } );
				expect( inquiryAISession( sess, "hello" ) ).toBe( "Hello" );
				expect( variables.handler.getRequests()[ 1 ].body ).notToHaveKey( "listener_marker" );
			});

			it( "replacing the request in the event with another struct is the same as returning it", function() {
				variables.handler.reset();
				var sess = createAISession( name="ldev6540-openai", listener={ onBeforePromptSent: function( event ) {
					var r = duplicate( event.request );
					r.listener_marker = "replaced";
					event.request = r;
				} } );
				inquiryAISession( sess, "hello" );
				expect( variables.handler.getRequests()[ 1 ].body.listener_marker ).toBe( "replaced" );
			});

			it( "refuses to change the stream flag of the request", function() {
				variables.handler.reset();
				var sess = createAISession( name="ldev6540-openai", listener={ onBeforePromptSent: function( event ) { event.request.stream = false; } } );
				var e = catchError( function() { inquiryAISession( sess, "hello", function( text ) {} ); } );
				expect( e.message ).toInclude( "stream" );
				expect( e.message ).toInclude( "onBeforePromptSent" );
				expect( variables.handler.getRequests() ).toHaveLength( 0 );
			});

			it( "refuses a return value that is not a struct, true, false or nothing", function() {
				var sess = createAISession( name="ldev6540-openai", listener={ onBeforePromptSent: function( event ) { return "cancel"; } } );
				var e = catchError( function() { inquiryAISession( sess, "hello" ); } );
				expect( e.message ).toInclude( "onBeforePromptSent" );
				expect( e.message ).toInclude( "has to return a struct, true, false or nothing" );
			});
		});

		describe( "LDEV-6540 onResponse", function() {

			it( "gets the raw response and changes made in the event are used", function() {
				variables.handler.reset();
				var state = { keys: "", model: "" };
				var sess = createAISession( name="ldev6540-openai", listener={ onResponse: function( event ) {
					state.keys = listSort( structKeyList( event ), "text" );
					state.model = event.response.model;
					event.response.choices[ 1 ].message.content = "Changed";
				} } );
				expect( inquiryAISession( sess, "hello" ) ).toBe( "Changed" );
				expect( state.keys ).toBe( "connection,model,provider,response,session" );
				expect( state.model ).toBe( "gpt-mock-2024" );
				expect( serializeAISession( sess ) ).toInclude( "Changed" );
			});

			it( "a returned struct replaces the response", function() {
				var sess = createAISession( name="ldev6540-claude", listener={ onResponse: function( event ) {
					var r = duplicate( event.response );
					r.content[ 1 ].text = "Changed";
					return r;
				} } );
				expect( inquiryAISession( sess, "hello" ) ).toBe( "Changed" );
			});

			it( "works for all providers", function() {
				var changes = {
					"ldev6540-openai": function( r ) { r.choices[ 1 ].message.content = "Changed"; },
					"ldev6540-claude": function( r ) { r.content[ 1 ].text = "Changed"; },
					"ldev6540-gemini": function( r ) { r.candidates[ 1 ].content.parts[ 1 ].text = "Changed"; }
				};
				each( providers(), function( p ) {
					var change = changes[ p.name ];
					var sess = createAISession( name=p.name, listener={ onResponse: function( event ) { change( event.response ); } } );
					expect( inquiryAISession( sess, "hello" ) ).toBe( "Changed", p.name );
				} );
			});

			it( "the usage is read from the response the hook returned", function() {
				var sess = createAISession( name="ldev6540-openai", listener={ onResponse: function( event ) { structDelete( event.response, "usage" ); } } );
				inquiryAISession( sess, "hello" );
				var usage = AIGetUsage( sess );
				expect( usage.requests ).toBe( 1 );
				expect( usage.unreportedRequests ).toBe( 1 );
				expect( usage.total ).toBe( 0 );
			});

			it( "is not called for a streamed response", function() {
				each( providers(), function( p ) {
					var state = { calls: 0 };
					var sess = createAISession( name=p.name, listener={ onResponse: function( event ) { state.calls++; } } );
					inquiryAISession( sess, "hello", function( text ) {} );
					expect( state.calls ).toBe( 0, p.name );
					inquiryAISession( sess, "hello" );
					expect( state.calls ).toBe( 1, p.name );
				} );
			});

			it( "an exception of the hook is passed on and the call counts as failed", function() {
				var sess = createAISession( name="ldev6540-openai", listener={ onResponse: function( event ) { throw( type="ldev6540.custom", message="bad response" ); } } );
				var e = catchError( function() { inquiryAISession( sess, "hello" ); } );
				expect( e.type ).toBe( "ldev6540.custom" );
				expect( e.message ).toBe( "bad response" );
				expect( AIGetUsage( sess ).failedRequests ).toBe( 1 );
				expect( serializeAISession( sess ) ).notToInclude( "hello" );
			});
		});

		describe( "LDEV-6540 onStreamingResponse", function() {

			it( "gets every chunk with its position, starting with 1", function() {
				each( providers(), function( p ) {
					var state = { indexes: [], types: [], keys: "" };
					var sess = createAISession( name=p.name, listener={ onStreamingResponse: function( event ) {
						arrayAppend( state.indexes, event.index );
						arrayAppend( state.types, isStruct( event.chunk ) );
						state.keys = listSort( structKeyList( event ), "text" );
					} } );
					var parts = [];
					inquiryAISession( sess, "hello", function( text ) { arrayAppend( parts, text ); } );
					expect( arrayToList( parts, "" ) ).toBe( "Hello", p.name );
					expect( state.keys ).toBe( "chunk,connection,index,model,provider,session", p.name );
					expect( arrayLen( state.indexes ) ).toBeGTE( 2, p.name );
					for ( var i = 1; i <= arrayLen( state.indexes ); i++ ) {
						expect( state.indexes[ i ] ).toBe( i, p.name );
						expect( state.types[ i ] ).toBeTrue();
					}
				} );
			});

			it( "a chunk changed in the event is passed on to the response listener", function() {
				var changes = {
					"ldev6540-openai": function( event ) { if ( event.index == 1 ) event.chunk.choices[ 1 ].delta.content = "HEL"; },
					"ldev6540-claude": function( event ) { if ( event.index == 3 ) event.chunk.delta.text = "HEL"; },
					"ldev6540-gemini": function( event ) {
						if ( event.index == 1 ) {
							var c = duplicate( event.chunk );
							c.candidates[ 1 ].content.parts[ 1 ].text = "HEL";
							return c;
						}
					}
				};
				each( providers(), function( p ) {
					var sess = createAISession( name=p.name, listener={ onStreamingResponse: changes[ p.name ] } );
					var parts = [];
					var result = inquiryAISession( sess, "hello", function( text ) { arrayAppend( parts, text ); } );
					expect( arrayToList( parts, "" ) ).toBe( "HELlo", p.name );
					expect( result.toString() ).toBe( "HELlo", p.name );
					expect( serializeAISession( sess ) ).toInclude( "HELlo" );
				} );
			});

			it( "aborts the stream when the hook returns false", function() {
				var abortAt = { "ldev6540-openai": 2, "ldev6540-claude": 4, "ldev6540-gemini": 2 };
				each( providers(), function( p ) {
					var state = { seen: [] };
					var sess = createAISession( name=p.name, listener={ onStreamingResponse: function( event ) {
						arrayAppend( state.seen, event.index );
						if ( event.index == abortAt[ p.name ] ) return false;
					} } );
					var parts = [];
					var e = catchError( function() { inquiryAISession( sess, "hello", function( text ) { arrayAppend( parts, text ); } ); } );
					expect( e.type ).toBe( "application", p.name );
					expect( e.errorcode ).toBe( "ai.listener.aborted", p.name );
					expect( e.message ).toInclude( "aborted by the listener after #abortAt[ p.name ] - 1# chunks", p.name );
					// no chunk after the aborted one, and the aborted chunk itself is not passed on
					expect( state.seen[ arrayLen( state.seen ) ] ).toBe( abortAt[ p.name ], p.name );
					expect( arrayToList( parts, "" ) ).toBe( "Hel", p.name );
				} );
			});

			it( "an aborted stream is a failed request and is not added to the history", function() {
				each( providers(), function( p ) {
					var sess = createAISession( name=p.name, listener={ onStreamingResponse: function( event ) { return false; } } );
					var parts = [];
					var e = catchError( function() { inquiryAISession( sess, "abort me", function( text ) { arrayAppend( parts, text ); } ); } );
					expect( e.errorcode ).toBe( "ai.listener.aborted", p.name );
					expect( parts ).toBeEmpty( p.name );
					var usage = AIGetUsage( sess );
					expect( usage.failedRequests ).toBe( 1, p.name );
					expect( usage.total ).toBe( 0, p.name );
					expect( serializeAISession( sess ) ).notToInclude( "abort me" );
				} );
			});

			it( "the session is usable after an abort", function() {
				var state = { abort: true };
				var sess = createAISession( name="ldev6540-openai", listener={ onStreamingResponse: function( event ) { return !state.abort; } } );
				catchError( function() { inquiryAISession( sess, "one", function( text ) {} ); } );
				state.abort = false;
				var parts = [];
				inquiryAISession( sess, "two", function( text ) { arrayAppend( parts, text ); } );
				expect( arrayToList( parts, "" ) ).toBe( "Hello" );
				expect( AIGetUsage( sess ).requests ).toBe( 1 );
			});

			it( "an exception of the hook ends the stream and is passed on as it is", function() {
				each( providers(), function( p ) {
					var sess = createAISession( name=p.name, listener={ onStreamingResponse: function( event ) { throw( type="ldev6540.custom", message="bad chunk #event.index#" ); } } );
					var e = catchError( function() { inquiryAISession( sess, "hello", function( text ) {} ); } );
					expect( e.type ).toBe( "ldev6540.custom", p.name );
					expect( e.message ).toBe( "bad chunk 1", p.name );
					expect( AIGetUsage( sess ).failedRequests ).toBe( 1, p.name );
				} );
			});

			it( "is not called for a response that is not streamed", function() {
				each( providers(), function( p ) {
					var state = { calls: 0 };
					var sess = createAISession( name=p.name, listener={ onStreamingResponse: function( event ) { state.calls++; } } );
					expect( inquiryAISession( sess, "hello" ) ).toBe( "Hello", p.name );
					expect( state.calls ).toBe( 0, p.name );
				} );
			});
		});

		describe( "LDEV-6540 listener component and registration", function() {

			it( "accepts a component", function() {
				variables.handler.reset();
				var listener = new LDEV6540.Listener();
				var sess = createAISession( name="ldev6540-openai", listener=listener );
				expect( inquiryAISession( sess, "hello" ) ).toBe( "Hello" );
				var parts = [];
				inquiryAISession( sess, "hello", function( text ) { arrayAppend( parts, text ); } );
				expect( arrayToList( parts, "" ) ).toBe( "Hello" );

				var calls = listener.getCalls();
				var hooks = [];
				for ( var c in calls ) arrayAppend( hooks, c.hook & ( structKeyExists( c, "index" ) ? c.index : "" ) );
				expect( hooks[ 1 ] ).toBe( "onBeforePromptSent" );
				expect( hooks[ 2 ] ).toBe( "onResponse" );
				expect( hooks[ 3 ] ).toBe( "onBeforePromptSent" );
				expect( hooks[ 4 ] ).toBe( "onStreamingResponse1" );
				expect( calls[ 1 ].stream ).toBeFalse();
				expect( calls[ 3 ].stream ).toBeTrue();
				expect( calls[ 1 ].connection ).toBe( "ldev6540-openai" );
				expect( variables.handler.getRequests()[ 1 ].body.listener_marker ).toBe( "component" );
			});

			it( "a component can cancel and abort", function() {
				var cancelling = new LDEV6540.Listener( { cancel: true } );
				var e = catchError( function() { inquiryAISession( createAISession( name="ldev6540-claude", listener=cancelling ), "hello" ); } );
				expect( e.errorcode ).toBe( "ai.listener.cancelled" );

				var aborting = new LDEV6540.Listener( { abortAt: 2 } );
				e = catchError( function() { inquiryAISession( createAISession( name="ldev6540-gemini", listener=aborting ), "hello", function( text ) {} ); } );
				expect( e.errorcode ).toBe( "ai.listener.aborted" );
			});

			it( "the listener can be passed positional and with the alias sessionListener", function() {
				var state = { calls: 0 };
				var listener = { onResponse: function( event ) { state.calls++; } };
				inquiryAISession( createAISession( "ldev6540-openai", "", 10, 0.5, listener ), "hello" );
				inquiryAISession( createAISession( name="ldev6540-openai", sessionListener=listener ), "hello" );
				expect( state.calls ).toBe( 2 );
			});

			it( "LoadAISession takes the listener, it is not part of the serialized session", function() {
				var state = { calls: 0 };
				var listener = { onBeforePromptSent: function( event ) { state.calls++; return false; } };
				var sess = createAISession( name="ldev6540-openai", listener=listener );
				var data = serializeAISession( sess );
				expect( data ).notToInclude( "onBeforePromptSent" );

				var loaded = loadAISession( "ldev6540-openai", data );
				expect( inquiryAISession( loaded, "hello" ) ).toBe( "Hello" );
				expect( state.calls ).toBe( 0 );

				loaded = loadAISession( "ldev6540-openai", data, listener );
				var e = catchError( function() { inquiryAISession( loaded, "hello" ); } );
				expect( e.errorcode ).toBe( "ai.listener.cancelled" );
				expect( state.calls ).toBe( 1 );
			});

			it( "a session without a listener works as before", function() {
				each( providers(), function( p ) {
					expect( inquiryAISession( createAISession( name=p.name ), "hello" ) ).toBe( "Hello", p.name );
				} );
			});

			it( "refuses a listener that is not a component or a struct", function() {
				var e = catchError( function() { createAISession( name="ldev6540-openai", listener="onResponse" ); } );
				expect( e.message ).toInclude( "component or a struct" );
			});

			it( "refuses a listener without any hook", function() {
				var e = catchError( function() { createAISession( name="ldev6540-openai", listener={ onSomething: function() {} } ); } );
				expect( e.message ).toInclude( "onBeforePromptSent" );
				expect( e.message ).toInclude( "onResponse" );
				expect( e.message ).toInclude( "onStreamingResponse" );
			});

			it( "only the functions of a struct count as hooks", function() {
				var e = catchError( function() { createAISession( name="ldev6540-openai", listener={ onResponse: "no function" } ); } );
				expect( e.message ).toInclude( "at least one" );
			});
		});
	}

	private array function providers() {
		return [ { name: "ldev6540-openai" }, { name: "ldev6540-claude" }, { name: "ldev6540-gemini" } ];
	}

	private function catchError( required function fn ) {
		try {
			arguments.fn();
		}
		catch ( any e ) {
			return e;
		}
		fail( "the call was expected to throw an exception" );
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
