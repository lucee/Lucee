component extends="org.lucee.cfml.test.LuceeTestCase" labels="file,http" {

	function beforeAll() {
		// minimal local HTTP server that never sends a Content-Length header:
		// /chunked.txt answers GET with a chunked body, /close.txt with a body ended by closing the connection
		variables.body = "LDEV-6922 response without a Content-Length header";
		variables.serverSocket = createObject( "java", "java.net.ServerSocket" ).init( 0, 50,
			createObject( "java", "java.net.InetAddress" ).getByName( "127.0.0.1" ) );
		variables.baseUrl = "http://127.0.0.1:" & variables.serverSocket.getLocalPort() & "/";
		variables.threadName = "LDEV6922-" & createUUID();
		thread name=variables.threadName action="run" serverSocket=variables.serverSocket body=variables.body {
			var crlf = chr( 13 ) & chr( 10 );
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
					var chunked = path == "/chunked.txt";
					var rsp = "HTTP/1.1 200 OK" & crlf
						& "Content-Type: text/plain" & crlf
						& ( chunked && method != "HEAD" ? "Transfer-Encoding: chunked" & crlf : "" )
						& "Connection: close" & crlf & crlf;
					if ( method != "HEAD" ) {
						if ( chunked ) rsp &= formatBaseN( len( attributes.body ), 16 ) & crlf & attributes.body & crlf & "0" & crlf & crlf;
						else rsp &= attributes.body;
					}
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
		describe( title="LDEV-6922 http resource without a Content-Length header", body=function() {

			describe( title="chunked GET response", body=function() {
				it( title="getFileInfo() reports the real size, not 0", body=function( currentSpec ) {
					expect( getFileInfo( variables.baseUrl & "chunked.txt" ).size ).toBe( len( variables.body ) );
				});
				it( title="fileInfo() reports the real size, not 0", body=function( currentSpec ) {
					expect( fileInfo( variables.baseUrl & "chunked.txt" ).size ).toBe( len( variables.body ) );
				});
				it( title="fileRead() returns the body", body=function( currentSpec ) {
					expect( fileRead( variables.baseUrl & "chunked.txt" ) ).toBe( variables.body );
				});
				it( title="fileReadBinary() returns the body", body=function( currentSpec ) {
					expect( charsetEncode( fileReadBinary( variables.baseUrl & "chunked.txt" ), "UTF-8" ) ).toBe( variables.body );
				});
			});

			describe( title="GET response ended by closing the connection", body=function() {
				it( title="getFileInfo() reports the real size, not 0", body=function( currentSpec ) {
					expect( getFileInfo( variables.baseUrl & "close.txt" ).size ).toBe( len( variables.body ) );
				});
				it( title="fileRead() returns the body", body=function( currentSpec ) {
					expect( fileRead( variables.baseUrl & "close.txt" ) ).toBe( variables.body );
				});
			});

		});
	}
}
