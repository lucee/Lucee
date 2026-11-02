<cfscript>
	// builds a connection struct for the CFML echo engine, the label makes it unique
	function canonical( label ) {
		return { "component": "engines.Engine", "custom": { "session": "EchoSession", "label": label, "model": "echo" } };
	}

	server.ldev6131_inits = 0;
	label = "ldev6131-" & createUUID();
	def = canonical( label );

	s1 = createAISession( def, "be terse" );
	answer1 = inquiryAISession( s1, "hello" );
	initsAfterFirst = server.ldev6131_inits;

	// same definition again, built from scratch
	s2 = createAISession( canonical( label ), "other" );
	initsAfterSecond = server.ldev6131_inits;

	// same definition, other key order and key case
	reordered = {
		"COMPONENT": "engines.Engine",
		"Custom": { "MODEL": "echo", "Label": label, "SESSION": "EchoSession" }
	};
	s3 = createAISession( reordered, "third" );
	initsAfterThird = server.ldev6131_inits;

	// a different definition needs a different engine
	otherLabel = label & "-other";
	s4 = createAISession( canonical( otherLabel ), "fourth" );
	initsAfterOther = server.ldev6131_inits;

	// flat form, the engine properties are set directly in the struct
	flatLabel = label & "-flat";
	flat = { "component": "engines.Engine", "session": "EchoSession", "label": flatLabel, "model": "echo" };
	s5 = createAISession( flat, "flat" );

	// all the other functions
	json = serializeAISession( s1 );
	loaded = loadAISession( def, json );

	echo( serializeJSON( {
		"answer1": answer1,
		"answer2": inquiryAISession( s2, "hello" ),
		"answer3": inquiryAISession( s3, "hello" ),
		"answer5": inquiryAISession( s5, "hello" ),
		"initsAfterFirst": initsAfterFirst,
		"initsAfterSecond": initsAfterSecond,
		"initsAfterThird": initsAfterThird,
		"initsAfterOther": initsAfterOther,
		"id1": aiGetMetaData( def ).id,
		"id2": aiGetMetaData( reordered ).id,
		"idOther": aiGetMetaData( canonical( otherLabel ) ).id,
		"label": aiGetMetaData( def ).label,
		"flatLabel": aiGetMetaData( flat ).label,
		"sessionEngineLabel": s1.getEngine().getLabel(),
		"has": aiHas( def ),
		"hasFlat": aiHas( flat ),
		"hasName": aiHas( "ldev6131-does-not-exist" ),
		"loadedAnswer": inquiryAISession( loaded, "again" ),
		"models": isArray( aiGetModels( def, "array" ) ),
		"engineById": inquiryAISession( createAISession( "id:" & aiGetMetaData( def ).id, "by id" ), "x" )
	} ) );
</cfscript>
