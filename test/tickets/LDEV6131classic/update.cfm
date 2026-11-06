<cfscript>
	label = "ldev6131-" & createUUID();
	conn = { "component": "test.tickets.LDEV6131.engines.Engine", "custom": { "session": "EchoSession", "label": label, "model": "echo" } };
	result = {};

	// not defined yet
	result.hasBefore = aiHas( "ldev6131-app" );

	application action="update" ai="#{ "ldev6131-app": conn }#";

	result.hasAfter = aiHas( "ldev6131-app" );
	result.answer = inquiryAISession( createAISession( "ldev6131-app", "be terse" ), "hello" );
	result.label = aiGetMetaData( "ldev6131-app" ).label;
	result.listed = "";
	for ( c in aiGetConnections( "array" ) ) {
		if ( c.name == "ldev6131-app" ) result.listed = c.source;
	}

	// a second update replaces the connections defined by the application
	application action="update" ai="#{ "ldev6131-app2": conn }#";
	result.hasFirstAfterReplace = aiHas( "ldev6131-app" );
	result.hasSecond = aiHas( "ldev6131-app2" );

	// an invalid connection is reported
	try {
		application action="update" ai="#{ "ldev6131-bad": { "component": "test.tickets.LDEV6131.engines.DoesNotExist" } }#";
		result.invalid = "no exception";
	}
	catch ( e ) {
		result.invalid = "exception";
	}
	result.hasSecondAfterInvalid = aiHas( "ldev6131-app2" );

	echo( serializeJSON( result ) );
</cfscript>
