component {

	variables.calls = [];

	function init( struct config = {} ) {
		variables.config = arguments.config;
		return this;
	}

	function getCalls() {
		return variables.calls;
	}

	function onBeforePromptSent( event ) {
		arrayAppend( variables.calls, { hook: "onBeforePromptSent", stream: event.stream, connection: event.connection, provider: event.provider, model: event.model } );
		if ( variables.config.cancel ?: false ) return false;
		event.request.listener_marker = "component";
	}

	function onResponse( event ) {
		arrayAppend( variables.calls, { hook: "onResponse", connection: event.connection } );
	}

	function onStreamingResponse( event ) {
		arrayAppend( variables.calls, { hook: "onStreamingResponse", index: event.index } );
		if ( ( variables.config.abortAt ?: 0 ) == event.index ) return false;
	}

}
