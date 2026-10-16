<cfscript>
	ais = createAISession( "ldev6585-echo", "be terse" );
	inquiryAISession( ais, "hello" );
	json = serializeAISession( ais );
	data = deserializeJSON( json );

	loaded = loadAISession( "ldev6585-echo", json );

	echo( serializeJSON( {
		"serialized": data.systemMessage ?: "",
		"loaded": loaded.getSystemMessage(),
		"answer": inquiryAISession( loaded, "again" )
	} ) );
</cfscript>
