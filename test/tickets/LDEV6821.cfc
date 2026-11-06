component extends="org.lucee.cfml.test.LuceeTestCase" labels="file,http,content" {

	function beforeAll() {
		// minimal local HTTP server (plain ServerSocket) that records every request ("METHOD /path")
		variables.requests = createObject( "java", "java.util.concurrent.CopyOnWriteArrayList" ).init();
		variables.serverSocket = createObject( "java", "java.net.ServerSocket" ).init( 0, 50,
			createObject( "java", "java.net.InetAddress" ).getByName( "127.0.0.1" ) );
		variables.baseUrl = "http://127.0.0.1:" & variables.serverSocket.getLocalPort() & "/";
		variables.threadName = "LDEV6821-" & createUUID();
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
					var line = reader.readLine();
					while ( !isNull( line ) && len( line ) ) line = reader.readLine();
					var method = listFirst( requestLine, " " );
					var path = listFirst( listGetAt( requestLine, 2, " " ), "?" );
					attributes.requests.add( method & " " & path );
					var status = "200 OK";
					if ( path == "/missing.txt" ) status = "404 Not Found";
					else if ( path == "/forbidden.txt" ) status = "403 Forbidden";
					var body = left( status, 3 ) == "200" ? "LDEV-6821" : "error";
					var rsp = "HTTP/1.1 " & status & chr( 13 ) & chr( 10 )
						& "Content-Type: text/plain" & chr( 13 ) & chr( 10 )
						& "Content-Length: " & len( body ) & chr( 13 ) & chr( 10 )
						& "Connection: close" & chr( 13 ) & chr( 10 ) & chr( 13 ) & chr( 10 );
					if ( method != "HEAD" ) rsp &= body;
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
		describe( title="LDEV-6821 http resource sends no redundant HEAD/GET requests", body=function() {

			describe( title="server that supports HEAD", body=function() {

				it( title="fileExists() sends one HEAD request", body=function( currentSpec ) {
					variables.requests.clear();
					expect( fileExists( variables.baseUrl & "file.txt" ) ).toBeTrue();
					expect( requestLog() ).toBe( "HEAD /file.txt" );
				});

				it( title="fileRead() sends one HEAD and one GET request", body=function( currentSpec ) {
					variables.requests.clear();
					expect( trim( fileRead( variables.baseUrl & "file.txt" ) ) ).toBe( "LDEV-6821" );
					expect( requestLog() ).toBe( "HEAD /file.txt,GET /file.txt" );
				});

				it( title="fileInfo() sends one HEAD and one GET request", body=function( currentSpec ) {
					variables.requests.clear();
					var info = fileInfo( variables.baseUrl & "file.txt" );
					expect( info.size ).toBe( 9 );
					expect( requestLog() ).toBe( "HEAD /file.txt,GET /file.txt" );
				});

				it( title="cfcontent file=url with a type sends one HEAD and one GET request", body=function( currentSpec ) {
					variables.requests.clear();
					var result = _internalRequest(
						template: createURI( "LDEV6821/content.cfm" ),
						url: { src: variables.baseUrl & "file.txt" }
					);
					expect( trim( result.fileContent ) ).toBe( "LDEV-6821" );
					expect( requestLog() ).toBe( "HEAD /file.txt,GET /file.txt" );
				});

				it( title="cfcontent file=url without a type sends one HEAD request and one GET to detect the type and one to serve the file", body=function( currentSpec ) {
					variables.requests.clear();
					var result = _internalRequest(
						template: createURI( "LDEV6821/content-detect-type.cfm" ),
						url: { src: variables.baseUrl & "file.txt" }
					);
					expect( trim( result.fileContent ) ).toBe( "LDEV-6821" );
					expect( requestLog() ).toBe( "HEAD /file.txt,GET /file.txt,GET /file.txt" );
				});

			});

			describe( title="missing and forbidden files", body=function() {

				it( title="fileExists() is false for 404 with only one HEAD request", body=function( currentSpec ) {
					variables.requests.clear();
					expect( fileExists( variables.baseUrl & "missing.txt" ) ).toBeFalse();
					expect( requestLog() ).toBe( "HEAD /missing.txt" );
				});

				it( title="fileExists() is false for 403 with only one HEAD request", body=function( currentSpec ) {
					variables.requests.clear();
					expect( fileExists( variables.baseUrl & "forbidden.txt" ) ).toBeFalse();
					expect( requestLog() ).toBe( "HEAD /forbidden.txt" );
				});

			});

		});
	}

	private string function requestLog() {
		return arrayToList( variables.requests.toArray() );
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
