component {

	function init( string scheme, struct args ) {
		variables.scheme = arguments.scheme;
		return this;
	}

	function getResource( string path ) {
		if ( !structKeyExists( request, "LDEV3292_calls" ) ) request.LDEV3292_calls = [];
		arrayAppend( request.LDEV3292_calls, arguments.path );
		server.LDEV3292_last = {
			path: arguments.path,
			appName: getApplicationSettings().name,
			requestMarker: request.LDEV3292_marker ?: ""
		};
		return this;
	}
}
