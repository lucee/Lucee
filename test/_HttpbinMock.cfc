component {

	variables.port = 0;
	variables.running = false;
	variables.serverSocket = 0;
	variables.ArrayClass = createObject( "java", "java.lang.reflect.Array" );

	public struct function start() {
		if ( variables.port > 0 ) {
			return { server: "127.0.0.1", port: variables.port };
		}

		variables.serverSocket = createObject( "java", "java.net.ServerSocket" ).init( 0 );
		variables.port = variables.serverSocket.getLocalPort();
		variables.running = true;

		var serverSocket = variables.serverSocket;
		var listenPort = variables.port;
		var mock = this;

		thread name="luceeHttpbinMock#listenPort#" action="run" serverSocket=serverSocket listenPort=listenPort mock=mock {
			while ( true ) {
				try {
					var socket = attributes.serverSocket.accept();
					thread action="run" socket=socket port=attributes.listenPort mock=attributes.mock {
						try {
							attributes.mock.handleConnection( attributes.socket, attributes.port );
						}
						catch ( any e ) {
							systemOutput( "HttpbinMock handle error: #e.message#", true );
							try {
								attributes.socket.close();
							}
							catch ( any closeError ) {}
						}
					}
				}
				catch ( any e ) {
					systemOutput( "HttpbinMock accept error: #e.message#", true );
				}
			}
		};

		sleep( 100 );

		cfhttp( url="http://127.0.0.1:#variables.port#/status/200", method="GET", timeout="2", throwOnError=true );

		return { server: "127.0.0.1", port: variables.port };
	}

	public void function handleConnection( required socket, required port ) {
		var inputStream = arguments.socket.getInputStream();
		var httpRequest = _readRequest( inputStream );
		var built = _buildResponse( httpRequest, arguments.port );
		var outputStream = arguments.socket.getOutputStream();

		outputStream.write( built.headers.getBytes( "UTF-8" ) );
		if ( structKeyExists( built, "bodyBytes" ) ) {
			outputStream.write( built.bodyBytes );
		}
		else if ( len( built.body ) ) {
			outputStream.write( built.body.getBytes( "UTF-8" ) );
		}

		outputStream.flush();
		arguments.socket.close();
	}

	private struct function _readRequest( required inputStream ) {
		var reader = createObject( "java", "java.io.InputStreamReader" ).init( arguments.inputStream, "UTF-8" );
		var buffered = createObject( "java", "java.io.BufferedReader" ).init( reader );

		var requestLine = buffered.readLine();
		if ( isNull( requestLine ) || !len( requestLine ) ) {
			return { method: "GET", path: "/", body: "", headers: {} };
		}

		var parts = listToArray( requestLine, " " );
		var method = arrayLen( parts ) gte 1 ? parts[ 1 ] : "GET";
		var path = arrayLen( parts ) gte 2 ? listFirst( parts[ 2 ], "?" ) : "/";
		var headers = {};
		var contentLength = 0;
		var line = buffered.readLine();
		while ( len( line ) ) {
			var colon = find( ":", line );
			if ( colon > 1 ) {
				var name = trim( left( line, colon - 1 ) );
				var value = trim( mid( line, colon + 1 ) );
				headers[ name ] = value;
				if ( lCase( name ) == "content-length" ) {
					contentLength = val( value );
				}
			}
			line = buffered.readLine();
		}

		var body = "";
		if ( contentLength > 0 ) {
			var builder = createObject( "java", "java.lang.StringBuilder" ).init();
			var ch = 0;
			var read = 0;
			while ( read < contentLength ) {
				ch = buffered.read();
				if ( ch == -1 ) {
					break;
				}
				builder.append( chr( ch ) );
				read++;
			}
			body = builder.toString();
		}

		return { method: uCase( method ), path: path, body: body, headers: headers };
	}

	private struct function _buildResponse( required struct httpRequest, required port ) {
		var statusCode = 200;
		var statusText = "OK";
		var responseBody = "";
		var responseHeaders = "Content-Type: application/json#chr(13)##chr(10)#";
		var reqPath = httpRequest[ "path" ];
		var reqMethod = httpRequest[ "method" ];

		if ( reqPath == "/uuid" || reqPath == "/uuid/" ) {
			responseBody = '{"uuid":"#lCase( createUUID() )#"}';
		}
		else if ( reFind( "^/status/[0-9]+/?$", reqPath ) ) {
			statusCode = val( listLast( reqPath, "/" ) );
			statusText = "Status";
			responseBody = "";
			responseHeaders = "";
		}
		else if ( reqPath == "/json" || reqPath == "/json/" ) {
			responseBody = '{"slideshow":{"author":"Lucee HttpbinMock"}}';
		}
		else if ( reFind( "^/delay/[0-9]+/?$", reqPath ) ) {
			var seconds = min( val( listLast( reqPath, "/" ) ), 30 );
			if ( seconds > 0 ) {
				sleep( seconds * 1000 );
			}
			responseBody = serializeJSON( { args: {}, url: "http://127.0.0.1:#arguments.port##reqPath#" } );
		}
		else if ( reqPath == "/gzip" || reqPath == "/gzip/" ) {
			if ( reqMethod == "HEAD" ) {
				return {
					headers: "HTTP/1.1 200 OK#chr(13)##chr(10)#Content-Type: application/json#chr(13)##chr(10)#Content-Encoding: gzip#chr(13)##chr(10)#Connection: close#chr(13)##chr(10)##chr(13)##chr(10)#",
					body: ""
				};
			}
			responseBody = serializeJSON( { gzipped: true, headers: {} } );
			var bodyBytes = _gzipBytes( responseBody );
			var bodyLength = _javaByteLength( bodyBytes );
			return {
				headers: "HTTP/1.1 200 OK#chr(13)##chr(10)#Content-Type: application/json#chr(13)##chr(10)#Content-Encoding: gzip#chr(13)##chr(10)#Content-Length: #bodyLength##chr(13)##chr(10)#Connection: close#chr(13)##chr(10)##chr(13)##chr(10)#",
				bodyBytes: bodyBytes,
				body: ""
			};
		}
		else if ( reqPath == "/headers" || reqPath == "/headers/" ) {
			responseBody = serializeJSON( { headers: httpRequest[ "headers" ] } );
		}
		else if ( reqPath == "/anything" || reqPath == "/anything/" || reqPath == "/delete" || reqPath == "/delete/" ) {
			responseBody = serializeJSON( {
				method: reqMethod,
				url: "http://127.0.0.1:#arguments.port##reqPath#",
				headers: httpRequest[ "headers" ],
				data: httpRequest[ "body" ]
			} );
		}
		else {
			statusCode = 404;
			statusText = "Not Found";
			responseBody = "Not Found";
			responseHeaders = "Content-Type: text/plain#chr(13)##chr(10)#";
		}

		return {
			headers: "HTTP/1.1 #statusCode# #statusText##chr(13)##chr(10)##responseHeaders#Content-Length: #len( responseBody )##chr(13)##chr(10)#Connection: close#chr(13)##chr(10)##chr(13)##chr(10)#",
			body: responseBody
		};
	}

	private numeric function _javaByteLength( required bytes ) {
		return variables.ArrayClass.getLength( arguments.bytes );
	}

	private any function _gzipBytes( required string text ) {
		var ByteArrayOutputStream = createObject( "java", "java.io.ByteArrayOutputStream" );
		var GZIPOutputStream = createObject( "java", "java.util.zip.GZIPOutputStream" );
		var baos = ByteArrayOutputStream.init();
		var gzip = GZIPOutputStream.init( baos );
		var bytes = arguments.text.getBytes( "UTF-8" );
		var byteLength = _javaByteLength( bytes );
		gzip.write( bytes, 0, byteLength );
		gzip.close();
		return baos.toByteArray();
	}

}
