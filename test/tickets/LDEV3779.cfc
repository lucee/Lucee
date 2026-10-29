component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	function beforeAll() {
		// minimal local HTTP server (plain ServerSocket), records every request (request line and headers)
		variables.requests = createObject( "java", "java.util.concurrent.CopyOnWriteArrayList" ).init();
		variables.serverSocket = createObject( "java", "java.net.ServerSocket" ).init( 0, 50,
			createObject( "java", "java.net.InetAddress" ).getByName( "127.0.0.1" ) );
		variables.baseUrl = "http://127.0.0.1:" & variables.serverSocket.getLocalPort() & "/";
		variables.threadName = "LDEV3779-" & createUUID();
		thread name=variables.threadName action="run" serverSocket=variables.serverSocket requests=variables.requests {
			while ( !attributes.serverSocket.isClosed() ) {
				try {
					var socket = attributes.serverSocket.accept();
				}
				catch ( any e ) {
					break; // socket closed in afterAll
				}
				try {
					var reader = createObject( "java", "java.io.BufferedReader" ).init(
						createObject( "java", "java.io.InputStreamReader" ).init( socket.getInputStream(), "UTF-8" ) );
					var requestLine = reader.readLine();
					var raw = requestLine;
					var line = reader.readLine();
					while ( !isNull( line ) && len( line ) ) {
						raw &= chr( 10 ) & line;
						line = reader.readLine();
					}
					attributes.requests.add( raw );
					var path = listFirst( listGetAt( requestLine, 2, " " ), "?" );
					var status = left( path, 6 ) == "/error" ? "500 Internal Server Error" : "200 OK";
					var body = "ok:" & path;
					var rsp = "HTTP/1.1 " & status & chr( 13 ) & chr( 10 )
						& "Content-Type: text/plain" & chr( 13 ) & chr( 10 )
						& "Content-Length: " & len( body ) & chr( 13 ) & chr( 10 )
						& "Connection: close" & chr( 13 ) & chr( 10 ) & chr( 13 ) & chr( 10 ) & body;
					var out = socket.getOutputStream();
					out.write( charsetDecode( rsp, "UTF-8" ) );
					out.flush();
				}
				finally {
					socket.close();
				}
			}
		}
	}

	function afterAll() {
		if ( !isNull( variables.serverSocket ) ) variables.serverSocket.close();
		thread action="join" name=variables.threadName timeout=5000;
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-3779 listener for the tag http", body=function() {

			it( title="without a listener the tag behaves as before", body=function( currentSpec ) {
				variables.requests.clear();
				http url=variables.baseUrl & "plain" result="local.rsp";
				expect( local.rsp.fileContent ).toBe( "ok:/plain" );
				expect( variables.requests.size() ).toBe( 1 );
			});

			it( title="before can modify the request", body=function( currentSpec ) {
				variables.requests.clear();
				var base = variables.baseUrl;
				http url=base & "original" method="GET" result="local.rsp" listener={
					before: function( args, caller ) {
						args.url = base & "changed";
						arrayAppend( args.params, { type: "header", name: "X-Listener", value: "before" } );
						arrayAppend( args.params, { type: "url", name: "q", value: "1" } );
						return arguments;
					}
				};
				expect( local.rsp.fileContent ).toBe( "ok:/changed" );
				expect( variables.requests.size() ).toBe( 1 );
				var req = variables.requests.get( 0 );
				expect( req ).toInclude( "/changed?q=1" );
				expect( req ).toInclude( "X-Listener: before" );
			});

			it( title="before can send back only the changed arguments", body=function( currentSpec ) {
				variables.requests.clear();
				var base = variables.baseUrl;
				http url=base & "original" result="local.rsp" listener={
					before: function( args, caller ) {
						return { url: base & "partial", method: "POST" };
					}
				};
				expect( local.rsp.fileContent ).toBe( "ok:/partial" );
				expect( variables.requests.get( 0 ) ).toInclude( "POST /partial" );
			});

			it( title="before gets the attributes of the tag and the caller", body=function( currentSpec ) {
				variables.requests.clear();
				var seen = {};
				http url=variables.baseUrl & "seen" method="POST" result="local.rsp" listener={
					before: function( args, caller ) {
						seen.args = duplicate( args );
						seen.caller = duplicate( caller );
					}
				} {
					httpparam type="header" name="X-Seen" value="yes";
					httpparam type="formfield" name="f" value="v";
				};
				expect( seen.args.url ).toBe( variables.baseUrl & "seen" );
				expect( seen.args.method ).toBe( "POST" );
				expect( seen.args.result ).toBe( "local.rsp" );
				expect( arrayLen( seen.args.params ) ).toBe( 2 );
				expect( seen.args.params[ 1 ].type ).toBe( "header" );
				expect( seen.args.params[ 1 ].name ).toBe( "X-Seen" );
				expect( seen.args.params[ 2 ].type ).toBe( "formfield" );
				expect( seen.caller.template ).toInclude( "LDEV3779.cfc" );
				expect( local.rsp.fileContent ).toBe( "ok:/seen" );
			});

			it( title="before can answer the request, no request is sent", body=function( currentSpec ) {
				variables.requests.clear();
				http url=variables.baseUrl & "cached" result="local.rsp" listener={
					before: function( args, caller ) {
						return { result: { fileContent: "from local cache", statusCode: "200 OK" } };
					}
				};
				expect( local.rsp.fileContent ).toBe( "from local cache" );
				expect( variables.requests.size() ).toBe( 0 );
			});

			it( title="after gets the result and can modify it", body=function( currentSpec ) {
				variables.requests.clear();
				var seen = {};
				http url=variables.baseUrl & "after" result="local.rsp" listener={
					after: function( args, caller, result ) {
						seen.content = result.fileContent;
						seen.url = args.url;
						result.fileContent = ucase( result.fileContent );
						return { result: result };
					}
				};
				expect( seen.content ).toBe( "ok:/after" );
				expect( seen.url ).toBe( variables.baseUrl & "after" );
				expect( local.rsp.fileContent ).toBe( "OK:/AFTER" );
			});

			it( title="after without a return value keeps the result", body=function( currentSpec ) {
				variables.requests.clear();
				var called = { after: false };
				http url=variables.baseUrl & "keep" result="local.rsp" listener={
					after: function( args, caller, result ) {
						called.after = true;
					}
				};
				expect( called.after ).toBeTrue();
				expect( local.rsp.fileContent ).toBe( "ok:/keep" );
			});

			it( title="a component can be used as listener", body=function( currentSpec ) {
				variables.requests.clear();
				http url=variables.baseUrl & "cfc" result="local.rsp" listener=new LDEV3779.Listener();
				expect( local.rsp.fileContent ).toBe( "[ok:/cfccomponent]" );
				expect( variables.requests.get( 0 ) ).toInclude( "X-Listener: component" );
			});

			it( title="error can handle the exception with a result", body=function( currentSpec ) {
				variables.requests.clear();
				var seen = {};
				var afterCalled = { value: false };
				http url=variables.baseUrl & "error" result="local.rsp" throwOnError=true listener={
					error: function( args, caller, exception ) {
						seen.message = exception.message;
						return { result: { fileContent: "handled", statusCode: "200 handled" } };
					},
					after: function( args, caller, result ) {
						afterCalled.value = true;
					}
				};
				expect( seen.message ).toInclude( "500" );
				expect( local.rsp.fileContent ).toBe( "handled" );
				expect( afterCalled.value ).toBeTrue();
			});

			it( title="error with a component listener", body=function( currentSpec ) {
				variables.requests.clear();
				http url=variables.baseUrl & "error" result="local.rsp" throwOnError=true listener=new LDEV3779.Listener();
				expect( local.rsp.fileContent ).toInclude( "handled:" );
				expect( local.rsp.fileContent ).toInclude( "500" );
			});

			it( title="exception is thrown when error does not return a result", body=function( currentSpec ) {
				variables.requests.clear();
				var called = { error: false };
				var msg = "";
				try {
					http url=variables.baseUrl & "error" result="local.rsp" throwOnError=true listener={
						error: function( args, caller, exception ) {
							called.error = true;
						}
					};
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( called.error ).toBeTrue();
				expect( msg ).toInclude( "500" );
			});

			it( title="exception is thrown when the listener has no error function", body=function( currentSpec ) {
				variables.requests.clear();
				var msg = "";
				try {
					http url=variables.baseUrl & "error" result="local.rsp" throwOnError=true listener={
						before: function( args, caller ) {}
					};
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toInclude( "500" );
			});

			it( title="exception of the listener is not swallowed", body=function( currentSpec ) {
				variables.requests.clear();
				var msg = "";
				try {
					http url=variables.baseUrl & "boom" result="local.rsp" listener={
						before: function( args, caller ) {
							throw( message="LDEV-3779 listener failure" );
						}
					};
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toBe( "LDEV-3779 listener failure" );
				expect( variables.requests.size() ).toBe( 0 );
			});

			it( title="an invalid param type from before is an error", body=function( currentSpec ) {
				variables.requests.clear();
				var msg = "";
				try {
					http url=variables.baseUrl & "invalid" result="local.rsp" listener={
						before: function( args, caller ) {
							arrayAppend( args.params, { type: "nope", name: "a", value: "b" } );
							return arguments;
						}
					};
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).toInclude( "invalid type [nope]" );
				expect( variables.requests.size() ).toBe( 0 );
			});

		});

		describe( title="LDEV-3779 listener for the tag http defined in the application", body=function() {

			it( title="this.http.listener in the Application.cfc is used", body=function( currentSpec ) {
				variables.requests.clear();
				var result = _InternalRequest( template=createURI( "LDEV3779/index.cfm" ), urls={ mode: "app", target: variables.baseUrl & "app" } );
				expect( result.filecontent.trim() ).toBe( "ok:/app|application-after" );
				expect( variables.requests.size() ).toBe( 1 );
				expect( variables.requests.get( 0 ) ).toInclude( "X-Listener: application" );
			});

			it( title="the attribute listener overrides the listener of the application", body=function( currentSpec ) {
				variables.requests.clear();
				var result = _InternalRequest( template=createURI( "LDEV3779/index.cfm" ), urls={ mode: "override", target: variables.baseUrl & "override" } );
				// the after of the application is not used
				expect( result.filecontent.trim() ).toBe( "ok:/override" );
				expect( variables.requests.size() ).toBe( 1 );
				expect( variables.requests.get( 0 ) ).toInclude( "X-Listener: tag" );
				expect( variables.requests.get( 0 ) ).notToInclude( "X-Listener: application" );
			});

			it( title="the listener can be set with cfapplication", body=function( currentSpec ) {
				variables.requests.clear();
				var result = _InternalRequest( template=createURI( "LDEV3779/index.cfm" ), urls={ mode: "cfapplication", target: variables.baseUrl & "cfapp" } );
				expect( result.filecontent.trim() ).toBe( "ok:/cfapp|cfapplication-after" );
				// the before of the Application.cfc is replaced as well
				expect( variables.requests.get( 0 ) ).notToInclude( "X-Listener: application" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & arguments.calledName;
	}

}
