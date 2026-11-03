component extends="org.lucee.cfml.test.LuceeTestCase" labels="mvn" {

	function beforeAll() {
		variables.dir = getTempDirectory() & "ldev6663-" & createUUID() & "/";
		directoryCreate( variables.dir );

		// minimal HTTP server on the loopback interface, answers every request with the configured status
		variables.status = createObject( "java", "java.util.concurrent.atomic.AtomicInteger" ).init( 404 );
		variables.hits = createObject( "java", "java.util.concurrent.atomic.AtomicInteger" ).init( 0 );
		variables.serverSocket = createObject( "java", "java.net.ServerSocket" ).init( 0, 5, createObject( "java", "java.net.InetAddress" ).getLoopbackAddress() );
		variables.baseUrl = "http://127.0.0.1:" & variables.serverSocket.getLocalPort() & "/";
		variables.threadName = "ldev6663-" & createUUID();
		thread name=variables.threadName serverSocket=variables.serverSocket status=variables.status hits=variables.hits {
			var CRLF = chr( 13 ) & chr( 10 );
			while ( !attributes.serverSocket.isClosed() ) {
				try {
					var sock = attributes.serverSocket.accept();
				}
				catch ( any e ) {
					break; // server socket was closed
				}
				try {
					var reader = createObject( "java", "java.io.BufferedReader" ).init( createObject( "java", "java.io.InputStreamReader" ).init( sock.getInputStream() ) );
					var line = reader.readLine();
					while ( !isNull( line ) && len( line ) ) line = reader.readLine(); // read the request headers
					attributes.hits.incrementAndGet();
					var sc = attributes.status.get();
					var resp = "HTTP/1.1 " & sc & " Test" & CRLF;
					if ( sc == 429 || sc == 503 ) resp &= "Retry-After: 120" & CRLF;
					resp &= "Content-Length: 0" & CRLF & "Connection: close" & CRLF & CRLF;
					var out = sock.getOutputStream();
					out.write( charsetDecode( resp, "iso-8859-1" ) );
					out.flush();
				}
				finally {
					sock.close();
				}
			}
		}
	}

	function afterAll() {
		if ( !isNull( variables.serverSocket ) ) variables.serverSocket.close();
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6663 transient Maven download failures must not be negative-cached", body=function() {

			it( title="a network error (connection refused) does not write a .lastUpdated file", body=function( currentSpec ) {
				// nothing listens on the loopback discard port, so every attempt fails with a network exception
				var r = download( "ldev6663-refused", "http://127.0.0.1:9/" );
				expect( r.error ).toInclude( "Failed to download" );
				expect( fileExists( r.lastUpdated ) ).toBeFalse( "transient failure must not be negative-cached" );

				// a second attempt must really try again, not fail from the negative cache
				var second = download( "ldev6663-refused", "http://127.0.0.1:9/" );
				expect( second.error ).toInclude( "Failed to download" );
				expect( second.error ).notToInclude( "cached for" );
			});

			it( title="HTTP 429 is not negative-cached and keeps the Retry-After in the error", body=function( currentSpec ) {
				setStatus( 429 );
				var r = download( "ldev6663-429", variables.baseUrl );
				expect( variables.hits.get() ).toBe( 1 );
				expect( r.error ).toInclude( "429" );
				expect( r.error ).toInclude( "Retry-After: 120" );
				expect( fileExists( r.lastUpdated ) ).toBeFalse( "429 must not be negative-cached" );

				var second = download( "ldev6663-429", variables.baseUrl );
				expect( variables.hits.get() ).toBe( 2, "second attempt must hit the repository again" );
				expect( second.error ).toInclude( "429" );
				expect( second.error ).notToInclude( "cached for" );
			});

			it( title="HTTP 5xx is not negative-cached", body=function( currentSpec ) {
				loop array=[ 500, 502, 503 ] item="local.sc" {
					setStatus( sc );
					var r = download( "ldev6663-" & sc, variables.baseUrl );
					expect( r.error ).toInclude( toString( sc ) );
					expect( fileExists( r.lastUpdated ) ).toBeFalse( sc & " must not be negative-cached" );

					var second = download( "ldev6663-" & sc, variables.baseUrl );
					expect( variables.hits.get() ).toBe( 2, "second attempt for " & sc & " must hit the repository again" );
					expect( second.error ).notToInclude( "cached for" );
				}
			});

			it( title="a definitive HTTP 404 is still negative-cached", body=function( currentSpec ) {
				setStatus( 404 );
				var r = download( "ldev6663-404", variables.baseUrl );
				expect( variables.hits.get() ).toBe( 1 );
				expect( r.error ).toInclude( "Failed to download" );
				expect( fileExists( r.lastUpdated ) ).toBeTrue( "404 must still be negative-cached" );

				var second = download( "ldev6663-404", variables.baseUrl );
				expect( variables.hits.get() ).toBe( 1, "second attempt must be served from the negative cache" );
				expect( second.error ).toInclude( "cached for" );
			});

		});
	}

	private void function setStatus( required numeric sc ) {
		variables.status.set( arguments.sc );
		variables.hits.set( 0 );
	}

	private struct function download( required string artifactId, required string repoUrl ) {
		var res = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" ).toResourceNotExisting( getPageContext(), variables.dir );
		var pom = createObject( "java", "lucee.runtime.mvn.POM" ).getInstance( res, "org.lucee.test", arguments.artifactId, "1.0.0", javaCast( "null", "" ) );
		var repos = createObject( "java", "java.util.ArrayList" ).init();
		repos.add( createObject( "java", "lucee.runtime.mvn.Repository" ).init( arguments.repoUrl ) );

		var error = "";
		try {
			createObject( "java", "lucee.runtime.mvn.MavenUtil" ).download( pom, repos, "pom", javaCast( "null", "" ) );
		}
		catch ( any e ) {
			error = e.message;
		}
		expect( error ).notToBeEmpty( "the download must fail" );
		return {
			error: error,
			lastUpdated: variables.dir & "org/lucee/test/" & arguments.artifactId & "/1.0.0/" & arguments.artifactId & "-1.0.0.pom.lastUpdated"
		};
	}

}
