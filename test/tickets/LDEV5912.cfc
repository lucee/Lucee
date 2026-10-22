component extends="org.lucee.cfml.test.LuceeTestCase" labels="file,http" {

	function beforeAll() {
		// minimal local HTTP server (plain ServerSocket), records the method of every request
		variables.requests = createObject( "java", "java.util.concurrent.CopyOnWriteArrayList" ).init();
		variables.serverSocket = createObject( "java", "java.net.ServerSocket" ).init( 0, 50,
			createObject( "java", "java.net.InetAddress" ).getByName( "127.0.0.1" ) );
		variables.baseUrl = "http://127.0.0.1:" & variables.serverSocket.getLocalPort() & "/";
		variables.threadName = "LDEV5912-" & createUUID();
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
					// skip the headers
					var line = reader.readLine();
					while ( !isNull( line ) && len( line ) ) line = reader.readLine();
					var method = listFirst( requestLine, " " );
					attributes.requests.add( method );
					var body = "LDEV-5912";
					var rsp = "HTTP/1.1 200 OK" & chr( 13 ) & chr( 10 )
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
		variables.dir = getTempDirectory() & "LDEV5912-" & createUUID() & "/";
		directoryCreate( variables.dir );
	}

	function afterAll() {
		if ( !isNull( variables.serverSocket ) ) variables.serverSocket.close();
		thread action="join" name=variables.threadName timeout=5000;
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-5912 fileCopy() from a http resource", body=function() {

			it( title="copies the file", body=function( currentSpec ) {
				var trg = variables.dir & "copy.txt";
				fileCopy( variables.baseUrl & "file.txt", trg );
				expect( fileRead( trg ) ).toBe( "LDEV-5912" );
			});

			it( title="makes at most one HEAD request before downloading", body=function( currentSpec ) {
				variables.requests.clear();
				fileCopy( variables.baseUrl & "file.txt", variables.dir & "copy2.txt" );
				var methods = arrayToList( variables.requests.toArray() );
				var heads = 0;
				loop list=methods item="local.m" {
					if ( local.m == "HEAD" ) heads++;
				}
				expect( heads ).toBeLTE( 1, "requests: " & methods );
				expect( listFind( methods, "GET" ) ).toBeGT( 0, "requests: " & methods );
			});

		});
	}
}
