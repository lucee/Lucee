component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	function run( testResults, testBox ) {
		describe( "LDEV-2449 cfhttp and the UTF-8 byte order mark", function() {

			it( title="a UTF-8 BOM at the start of a text response is not part of cfhttp.fileContent", body=function( currentSpec ) {
				// BOM EF BB BF followed by {"a":1}
				var result = getResponse( "EFBBBF7B2261223A317D", "application/json; charset=utf-8" );
				expect( result.fileContent ).toBe( '{"a":1}' );
				expect( isJSON( result.fileContent ) ).toBeTrue();
			});

			it( title="a response without a BOM is not changed", body=function( currentSpec ) {
				var result = getResponse( "7B2261223A317D", "application/json; charset=utf-8" );
				expect( result.fileContent ).toBe( '{"a":1}' );
			});

			it( title="a BOM in the middle of the content is kept", body=function( currentSpec ) {
				// {"a":1} followed by BOM
				var result = getResponse( "7B2261223A317DEFBBBF", "text/plain; charset=utf-8" );
				expect( len( result.fileContent ) ).toBe( 8 );
				expect( asc( right( result.fileContent, 1 ) ) ).toBe( 65279 );
			});

		});
	}

	/**
	* starts a minimal http server on a free local port, requests it with cfhttp and returns the cfhttp result,
	* the server answers with the given body (hex) and content type
	*/
	private struct function getResponse( required string bodyHex, required string contentType ) {
		var ss = createObject( "java", "java.net.ServerSocket" ).init( 0 );
		ss.setSoTimeout( 20000 );
		var port = ss.getLocalPort();
		var body = binaryDecode( arguments.bodyHex, "hex" );
		var crlf = chr( 13 ) & chr( 10 );
		var head = "HTTP/1.1 200 OK" & crlf & "Content-Type: #arguments.contentType#" & crlf & "Content-Length: #arrayLen( body )#" & crlf & "Connection: close" & crlf & crlf;
		try {
			thread name="ldev2449_#createUniqueId()#" ss=ss head=head body=body {
				var sock = attributes.ss.accept();
				try {
					sock.setSoTimeout( 20000 );
					var is = sock.getInputStream();
					var req = "";
					while ( !req.endsWith( chr( 13 ) & chr( 10 ) & chr( 13 ) & chr( 10 ) ) ) {
						var b = is.read();
						if ( b == -1 ) break;
						req &= chr( b );
					}
					var os = sock.getOutputStream();
					os.write( javacast( "byte[]", attributes.head.getBytes( "UTF-8" ) ) );
					os.write( attributes.body );
					os.flush();
				}
				finally {
					sock.close();
				}
			}
			http method="GET" url="http://127.0.0.1:#port#/" result="local.res" timeout=20;
			threadJoin( "", 30000 );
		}
		finally {
			ss.close();
		}
		return res;
	}

}
