<cfscript>
	// the struct has no class and no type to derive it from
	result = {};
	try {
		createAISession( { "model": "x" }, "msg" );
		result.noClass = "no exception";
	}
	catch ( e ) {
		result.noClass = e.type;
		result.noClassMessage = e.message;
	}
	try {
		aiHas( { "type": "unknown-provider", "model": "x" } );
		result.badType = "no exception";
	}
	catch ( e ) {
		result.badType = e.type;
	}
	try {
		createAISession( { "component": "engines.DoesNotExist", "custom": { "label": "x" } }, "msg" );
		result.badComponent = "no exception";
	}
	catch ( e ) {
		result.badComponent = "exception";
	}
	// a name that does not exist still behaves as before
	try {
		createAISession( "ldev6131-does-not-exist", "msg" );
		result.badName = "no exception";
	}
	catch ( e ) {
		result.badName = "exception";
	}
	echo( serializeJSON( result ) );
</cfscript>
