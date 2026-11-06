component {

	variables.requests = createObject( "java", "java.util.concurrent.CopyOnWriteArrayList" ).init();
	variables.errors = createObject( "java", "java.util.concurrent.CopyOnWriteArrayList" ).init();

	/* a minimal HTTP server on a socket, the HTTP server of the JDK is not visible from within Lucee */
	function start() {
		variables.ss = createObject( "java", "java.net.ServerSocket" ).init( 0, 50, createObject( "java", "java.net.InetAddress" ).getByName( "127.0.0.1" ) );
		thread name="ldev6527-mock-#createUUID()#" action="run" mock=this ss=variables.ss {
			while ( !attributes.ss.isClosed() ) {
				try {
					var socket = attributes.ss.accept();
				}
				catch ( any e ) {
					if ( attributes.ss.isClosed() ) break;
					attributes.mock.addError( "accept: " & e.message );
					continue;
				}
				attributes.mock.serve( socket );
			}
		}
		return this;
	}

	function stop() {
		if ( !isNull( variables.ss ) ) variables.ss.close();
	}

	function getPort() {
		return variables.ss.getLocalPort();
	}

	function addError( required string msg ) {
		variables.errors.add( arguments.msg );
	}

	function getRequests() {
		var result = [];
		for ( var r in variables.requests.toArray() ) arrayAppend( result, r );
		return result;
	}

	function getErrors() {
		var result = [];
		for ( var r in variables.errors.toArray() ) arrayAppend( result, r );
		return result;
	}

	function reset() {
		variables.requests.clear();
		variables.errors.clear();
	}

	public void function serve( required any socket ) {
		try {
			var reader = createObject( "java", "java.io.BufferedReader" ).init( createObject( "java", "java.io.InputStreamReader" ).init( socket.getInputStream(), "UTF-8" ) );
			var requestLine = reader.readLine();
			var path = listGetAt( requestLine, 2, " " );
			var length = 0;
			var line = reader.readLine();
			while ( !isNull( line ) && len( line ) ) {
				if ( left( line, 15 ) == "Content-Length:" || left( line, 15 ) == "content-length:" ) length = int( trim( mid( line, 16, 20 ) ) );
				line = reader.readLine();
			}
			var raw = "";
			if ( length > 0 ) {
				var buf = createObject( "java", "java.lang.reflect.Array" ).newInstance( createObject( "java", "java.lang.Character" ).TYPE, length );
				var got = 0;
				while ( got < length ) {
					var n = reader.read( buf, got, length - got );
					if ( n < 0 ) break;
					got += n;
				}
				raw = createObject( "java", "java.lang.String" ).init( buf, 0, got );
			}
			var body = isJSON( raw ) ? deserializeJSON( raw ) : {};
			variables.requests.add( { path: path, body: body } );

			var isStream = ( body.stream ?: false ) == true || path contains "streamGenerateContent";
			var model = body.model ?: "";
			var status = 200;
			var contentType = isStream ? "text/event-stream" : "application/json";
			var out = "";

			if ( path contains "/openai/" ) {
				if ( model == "nostreamopts" && structKeyExists( body, "stream_options" ) ) {
					status = 400;
					contentType = "application/json";
					out = serializeJSON( { error: { message: "Unknown parameter: 'stream_options'.", type: "invalid_request_error" } } );
				}
				else out = openai( body, isStream, model );
			}
			else if ( path contains "/claude/" ) out = claude( isStream );
			else if ( path contains "/gemini/" ) out = gemini( isStream );
			else {
				status = 404;
				out = "{}";
			}

			var bytes = out.getBytes( "UTF-8" );
			var cr = chr( 13 ) & chr( 10 );
			var os = socket.getOutputStream();
			os.write( ( "HTTP/1.1 #status# Mock#cr#Content-Type: #contentType##cr#Content-Length: #arrayLen( bytes )##cr#Connection: close#cr##cr#" ).getBytes( "UTF-8" ) );
			os.write( bytes );
			os.flush();
		}
		catch ( any e ) {
			variables.errors.add( e.message & " " & e.detail );
		}
		finally {
			socket.close();
		}
	}

	private string function sse( required array chunks, boolean withEvent=false ) {
		var sb = "";
		for ( var c in arguments.chunks ) {
			if ( isSimpleValue( c ) ) sb &= "data: #c##chr( 10 )##chr( 10 )#";
			else {
				if ( arguments.withEvent ) sb &= "event: #c.type##chr( 10 )#";
				sb &= "data: #serializeJSON( c )##chr( 10 )##chr( 10 )#";
			}
		}
		return sb;
	}

	private string function openai( required struct body, required boolean isStream, required string model ) {
		var usage = {
			prompt_tokens: 20,
			completion_tokens: 5,
			total_tokens: 25,
			prompt_tokens_details: { cached_tokens: 8 },
			completion_tokens_details: { reasoning_tokens: 2 }
		};
		var withUsage = model != "nousage";
		if ( !isStream ) {
			var rsp = { model: "gpt-mock-2024", choices: [ { message: { role: "assistant", content: "Hello" }, finish_reason: "stop" } ] };
			if ( withUsage ) rsp.usage = usage;
			return serializeJSON( rsp );
		}
		var chunks = [
			{ model: "gpt-mock-2024", choices: [ { delta: { role: "assistant", content: "Hel" } } ] },
			{ model: "gpt-mock-2024", choices: [ { delta: { content: "lo" }, finish_reason: "stop" } ] }
		];
		// usage is only sent when it was asked for
		if ( withUsage && structKeyExists( body, "stream_options" ) && ( body.stream_options.include_usage ?: false ) ) {
			arrayAppend( chunks, { model: "gpt-mock-2024", choices: [], usage: usage } );
		}
		arrayAppend( chunks, "[DONE]" );
		return sse( chunks );
	}

	private string function claude( required boolean isStream ) {
		if ( !isStream ) {
			return serializeJSON( {
				model: "claude-mock-1",
				content: [ { type: "text", text: "Hello" } ],
				usage: { input_tokens: 25, cache_read_input_tokens: 10, output_tokens: 15 }
			} );
		}
		return sse( [
			{ type: "message_start", message: { model: "claude-mock-1", usage: { input_tokens: 25, cache_read_input_tokens: 10, output_tokens: 1 } } },
			{ type: "content_block_start", index: 0, content_block: { type: "text", text: "" } },
			{ type: "content_block_delta", index: 0, delta: { type: "text_delta", text: "Hel" } },
			{ type: "content_block_delta", index: 0, delta: { type: "text_delta", text: "lo" } },
			{ type: "content_block_stop", index: 0 },
			{ type: "message_delta", delta: { stop_reason: "end_turn" }, usage: { output_tokens: 15 } },
			{ type: "message_stop" }
		], true );
	}

	private string function gemini( required boolean isStream ) {
		var meta = { promptTokenCount: 30, candidatesTokenCount: 10, thoughtsTokenCount: 4, totalTokenCount: 44, cachedContentTokenCount: 6 };
		if ( !isStream ) {
			return serializeJSON( {
				modelVersion: "gemini-mock-1",
				candidates: [ { content: { role: "model", parts: [ { text: "Hello" } ] } } ],
				usageMetadata: meta
			} );
		}
		return sse( [
			{ modelVersion: "gemini-mock-1", candidates: [ { content: { role: "model", parts: [ { text: "Hel" } ] } } ] },
			{ modelVersion: "gemini-mock-1", candidates: [ { content: { role: "model", parts: [ { text: "lo" } ] } } ], usageMetadata: meta }
		] );
	}
}
