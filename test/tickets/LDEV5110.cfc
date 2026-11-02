component extends="org.lucee.cfml.test.LuceeTestCase" labels="ai" {

	function beforeAll() {
		variables.mock = new LDEV5110.SseMock();
		variables.port = mock.start( chunks=10, delay=25 );
		variables.baseUrl = "http://127.0.0.1:#port#";
		variables.engines = {
			openai: { class: "lucee.runtime.ai.openai.OpenAIEngine", custom: { type: "other", url: baseUrl & "/v1/", secretKey: "mock", model: "mock-model" } },
			claude: { class: "lucee.runtime.ai.anthropic.ClaudeEngine", custom: { type: "other", url: baseUrl & "/v1/", apiKey: "mock", model: "mock-model" } },
			gemini: { class: "lucee.runtime.ai.google.GeminiEngine", custom: { type: "other", url: baseUrl & "/v1beta/", apiKey: "mock", model: "mock-model" } }
		};
		for ( var name in engines ) {
			admin
				action="updateAIConnection"
				type="server"
				password="#server.SERVERADMINPASSWORD#"
				name="ldev5110-#name#"
				class="#engines[ name ].class#"
				custom="#engines[ name ].custom#";
		}
	}

	function afterAll() {
		for ( var name in [ "openai", "claude", "gemini" ] ) {
			try {
				admin action="removeAIConnection" type="server" password="#server.SERVERADMINPASSWORD#" name="ldev5110-#name#";
			}
			catch ( any e ) {
				systemOutput( "could not remove the AI connection ldev5110-#name#: #e.message#", true );
			}
		}
		if ( !isNull( variables.mock ) ) variables.mock.stop();
	}

	function run( testResults, testBox ) {
		for ( var engine in [ "openai", "claude", "gemini" ] ) {
			describe( title="LDEV-5110 cancel the inquiry from the listener, engine #engine#", body=function( data ) {

				it( title="a listener without return value gets all chunks", data={ engine: engine }, body=function( data ) {
					var seen = [];
					var ais = createAISession( name="ldev5110-#data.engine#" );
					var answer = inquiryAISession( ais, "go", function( part ) {
						arrayAppend( seen, arguments.part );
					} );
					expect( answer ).toBe( mock.fullText() );
					expect( arrayLen( seen ) ).toBe( mock.getChunks() );
					expect( waitFor( "finished" ) ).toBeTrue();
					expect( mock.getStats().sent ).toBe( mock.getChunks() );
				} );

				it( title="a listener returning true gets all chunks", data={ engine: engine }, body=function( data ) {
					var seen = [];
					var ais = createAISession( name="ldev5110-#data.engine#" );
					var answer = inquiryAISession( ais, "go", function( part ) {
						arrayAppend( seen, arguments.part );
						return true;
					} );
					expect( answer ).toBe( mock.fullText() );
					expect( arrayLen( seen ) ).toBe( mock.getChunks() );
					expect( waitFor( "finished" ) ).toBeTrue();
				} );

				it( title="a listener returning a value that is not boolean false does not cancel", data={ engine: engine }, body=function( data ) {
					var seen = [];
					var ais = createAISession( name="ldev5110-#data.engine#" );
					var answer = inquiryAISession( ais, "go", function( part ) {
						arrayAppend( seen, arguments.part );
						return "no";
					} );
					expect( answer ).toBe( mock.fullText() );
					expect( arrayLen( seen ) ).toBe( mock.getChunks() );
				} );

				it( title="a listener returning false cancels, the answer is what was received so far", data={ engine: engine }, body=function( data ) {
					var seen = [];
					var ais = createAISession( name="ldev5110-#data.engine#" );
					var answer = inquiryAISession( ais, "go", function( part, index ) {
						arrayAppend( seen, arguments.part );
						return arguments.index < 3;
					} );
					expect( answer ).toBe( mock.word( 1 ) & mock.word( 2 ) & mock.word( 3 ) );
					// the listener is not called again after it cancelled
					expect( seen ).toBe( [ mock.word( 1 ), mock.word( 2 ), mock.word( 3 ) ] );
					// the connection is closed, the endpoint notices that and stops sending
					expect( waitFor( "aborted" ) ).toBeTrue();
					expect( mock.getStats().sent ).toBeLT( mock.getChunks() );
					expect( mock.getStats().finished ).toBeFalse();
				} );

				it( title="cancel on the first chunk", data={ engine: engine }, body=function( data ) {
					var calls = 0;
					var ais = createAISession( name="ldev5110-#data.engine#" );
					var answer = inquiryAISession( ais, "go", function( part ) {
						calls++;
						return false;
					} );
					expect( calls ).toBe( 1 );
					expect( answer ).toBe( mock.word( 1 ) );
					expect( waitFor( "aborted" ) ).toBeTrue();
				} );

				it( title="cancel works with a multipart question", data={ engine: engine }, body=function( data ) {
					var calls = 0;
					var ais = createAISession( name="ldev5110-#data.engine#" );
					var answer = inquiryAISession( ais, [ "go", "on" ], function( part, index ) {
						calls++;
						return arguments.index < 2;
					} );
					expect( calls ).toBe( 2 );
					expect( answer ).toBe( mock.word( 1 ) & mock.word( 2 ) );
					expect( waitFor( "aborted" ) ).toBeTrue();
				} );

				it( title="the session can be used again after a cancel", data={ engine: engine }, body=function( data ) {
					var ais = createAISession( name="ldev5110-#data.engine#" );
					var first = inquiryAISession( ais, "go", function( part ) {
						return false;
					} );
					expect( first ).toBe( mock.word( 1 ) );
					expect( waitFor( "aborted" ) ).toBeTrue();
					var second = inquiryAISession( ais, "again", function( part ) {
						return true;
					} );
					expect( second ).toBe( mock.fullText() );
					expect( waitFor( "finished" ) ).toBeTrue();
				} );

			} );
		}
	}

	/**
	 * waits until the mock endpoint reports the state, it is set by another thread
	 */
	private boolean function waitFor( required string state ) {
		var end = getTickCount() + 10000;
		while ( getTickCount() < end ) {
			if ( mock.getStats()[ arguments.state ] ) return true;
			sleep( 50 );
		}
		return false;
	}

}
