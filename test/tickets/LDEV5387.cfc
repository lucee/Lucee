component extends="org.lucee.cfml.test.LuceeTestCase" labels="file,http,content" {

	function beforeAll() {
		// minimal local HTTP server (plain ServerSocket) that, like S3 presigned URLs or some web servers,
		// rejects HEAD requests but serves GET requests
		variables.requests = createObject( "java", "java.util.concurrent.CopyOnWriteArrayList" ).init();
		variables.serverSocket = createObject( "java", "java.net.ServerSocket" ).init( 0, 50,
			createObject( "java", "java.net.InetAddress" ).getByName( "127.0.0.1" ) );
		variables.baseUrl = "http://127.0.0.1:" & variables.serverSocket.getLocalPort() & "/";
		variables.threadName = "LDEV5387-" & createUUID();
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
					else if ( method == "HEAD" && path == "/head-forbidden.txt" ) status = "403 Forbidden";
					else if ( method == "HEAD" && path == "/head-not-allowed.txt" ) status = "405 Method Not Allowed";
					var body = left( status, 3 ) == "200" ? "LDEV-5387" : "error";
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
		describe( title="LDEV-5387 http resource whose server rejects HEAD requests", body=function() {

			it( title="fileExists() is true when HEAD returns 403 but GET returns 200", body=function( currentSpec ) {
				expect( fileExists( variables.baseUrl & "head-forbidden.txt" ) ).toBeTrue( arrayToList( variables.requests.toArray() ) );
			});

			it( title="fileExists() is true when HEAD returns 405 but GET returns 200", body=function( currentSpec ) {
				expect( fileExists( variables.baseUrl & "head-not-allowed.txt" ) ).toBeTrue( arrayToList( variables.requests.toArray() ) );
			});

			it( title="cfcontent file=url serves the file when HEAD returns 403", body=function( currentSpec ) {
				var result = _internalRequest(
					template: createURI( "LDEV5387/content.cfm" ),
					url: { src: variables.baseUrl & "head-forbidden.txt?X-Amz-Signature=abc" }
				);
				expect( trim( result.fileContent ) ).toBe( "LDEV-5387" );
			});

			it( title="fileExists() stays false for 404 and 403 on GET too (LDEV-3947)", body=function( currentSpec ) {
				expect( fileExists( variables.baseUrl & "missing.txt" ) ).toBeFalse();
				expect( fileExists( variables.baseUrl & "forbidden.txt" ) ).toBeFalse();
			});

			it( title="cfcontent file=url still throws when GET returns 403 (LDEV-3947)", body=function( currentSpec ) {
				expect( function() {
					_internalRequest(
						template: createURI( "LDEV5387/content.cfm" ),
						url: { src: variables.baseUrl & "forbidden.txt" }
					);
				}).toThrow();
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}
