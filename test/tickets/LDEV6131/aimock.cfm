<cfscript>
	// connection to the aimock service, defined by a struct (flat form, class derived from the type)
	conn = { "type": "other", "url": "http://#url.server#:#url.port#/v1/", "secretKey": "mock", "model": "gpt-4" };
	ais = createAISession( conn, "You are a test assistant" );
	echo( serializeJSON( {
		"answer": inquiryAISession( ais, "ping" ),
		"class": aiGetMetaData( conn ).name,
		"same": aiGetMetaData( conn ).id == aiGetMetaData( duplicate( conn ) ).id
	} ) );
</cfscript>
