/**
 * Minimal local AI endpoint streaming "text/event-stream" responses in the OpenAI, Claude and Gemini format.
 * It needs no API key and no external service. It records how many chunks it sent and whether the client
 * went away before the stream was finished.
 */
component {

	variables.port = 0;
	variables.running = false;
	variables.serverSocket = "";
	variables.stats = { requests: 0, sent: 0, finished: false, aborted: false };
	variables.chunks = 10;
	variables.delay = 20;

	public numeric function start( numeric chunks=10, numeric delay=20 ) {
		variables.chunks = arguments.chunks;
		variables.delay = arguments.delay;
		variables.serverSocket = createObject( "java", "java.net.ServerSocket" ).init( 0, 50, createObject( "java", "java.net.InetAddress" ).getByName( "127.0.0.1" ) );
		variables.port = variables.serverSocket.getLocalPort();
		variables.running = true;

		var mock = this;
		var ss = variables.serverSocket;
		thread name="ldev5110mock#variables.port#" action="run" mock=mock ss=ss {
			while ( attributes.mock.isRunning() ) {
				try {
					var socket = attributes.ss.accept();
				}
				catch ( any e ) {
					// the server socket was closed
					break;
				}
				thread action="run" mock=attributes.mock socket=socket {
					attributes.mock.handle( attributes.socket );
				}
			}
		};
		return variables.port;
	}

	public boolean function isRunning() {
		return variables.running;
	}

	public void function stop() {
		variables.running = false;
		if ( isObject( variables.serverSocket ) ) variables.serverSocket.close();
	}

	public struct function getStats() {
		return duplicate( variables.stats );
	}

	public string function word( required numeric index ) {
		return "w" & arguments.index & " ";
	}

	public string function fullText() {
		var s = "";
		loop from=1 to=variables.chunks index="local.i" {
			s &= word( i );
		}
		return s;
	}

	public numeric function getChunks() {
		return variables.chunks;
	}

	public void function handle( required socket ) {
		var is = arguments.socket.getInputStream();
		var os = arguments.socket.getOutputStream();
		try {
			var req = readRequest( is );
			variables.stats = { requests: variables.stats.requests + 1, sent: 0, finished: false, aborted: false };

			var format = "openai";
			if ( req.path contains "messages" ) format = "claude";
			else if ( req.path contains "streamGenerateContent" ) format = "gemini";

			os.write( "HTTP/1.1 200 OK#chr( 13 )##chr( 10 )#Content-Type: text/event-stream; charset=UTF-8#chr( 13 )##chr( 10 )#Cache-Control: no-cache#chr( 13 )##chr( 10 )#Connection: close#chr( 13 )##chr( 10 )##chr( 13 )##chr( 10 )#".getBytes( "UTF-8" ) );
			os.flush();

			loop from=1 to=variables.chunks index="local.i" {
				os.write( event( format, word( i ) ).getBytes( "UTF-8" ) );
				os.flush();
				variables.stats.sent = i;
				sleep( variables.delay );
			}
			os.write( trailer( format ).getBytes( "UTF-8" ) );
			os.flush();
			variables.stats.finished = true;
		}
		catch ( any e ) {
			// the client closed the connection before the end of the stream
			variables.stats.aborted = true;
			variables.stats.error = e.message;
		}
		finally {
			try {
				arguments.socket.close();
			}
			catch ( any e ) {
			}
		}
	}

	private string function event( required string format, required string text ) {
		var nl = chr( 10 );
		var payload = "";
		switch ( arguments.format ) {
			case "claude":
				payload = serializeJSON( { "type": "content_block_delta", "index": 0, "delta": { "type": "text_delta", "text": arguments.text } } );
				return "event: content_block_delta" & nl & "data: " & payload & nl & nl;
			case "gemini":
				payload = serializeJSON( { "candidates": [ { "content": { "role": "model", "parts": [ { "text": arguments.text } ] } } ] } );
				return "data: " & payload & nl & nl;
			default:
				payload = serializeJSON( { "id": "mock", "object": "chat.completion.chunk", "choices": [ { "index": 0, "delta": { "content": arguments.text } } ] } );
				return "data: " & payload & nl & nl;
		}
	}

	private string function trailer( required string format ) {
		var nl = chr( 10 );
		switch ( arguments.format ) {
			case "claude":
				return "event: message_stop" & nl & 'data: {"type":"message_stop"}' & nl & nl;
			case "gemini":
				return "";
			default:
				return "data: [DONE]" & nl & nl;
		}
	}

	private struct function readRequest( required is ) {
		var reader = createObject( "java", "java.io.BufferedReader" ).init( createObject( "java", "java.io.InputStreamReader" ).init( arguments.is, "ISO-8859-1" ) );
		var requestLine = reader.readLine();
		var path = listGetAt( requestLine, 2, " " );
		var contentLength = 0;
		var line = reader.readLine();
		while ( !isNull( line ) && len( line ) ) {
			if ( lCase( listFirst( line, ":" ) ) == "content-length" ) contentLength = val( listRest( line, ":" ) );
			line = reader.readLine();
		}
		// the body is not needed, but it has to be consumed
		loop from=1 to=contentLength index="local.i" {
			reader.read();
		}
		return { path: path };
	}

}
