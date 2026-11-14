<cfscript>
	sys = createObject( "java", "java.lang.System" );
	if ( url.formFirst ?: false ) formBefore = form.a ?: "";

	first = getHTTPRequestData().content;
	second = getHTTPRequestData().content;
	explicitTrue = getHTTPRequestData( true ).content;
	withoutBody = getHTTPRequestData( false );
	third = getHTTPRequestData().content;

	isBin = isBinary( first );
	result = {
		isBinary: isBin,
		sameInstance: sys.identityHashCode( first ) == sys.identityHashCode( second )
			&& sys.identityHashCode( first ) == sys.identityHashCode( explicitTrue )
			&& sys.identityHashCode( first ) == sys.identityHashCode( third ),
		sameValue: ( isBin ? ( hash( first ) == hash( second ) && hash( first ) == hash( third ) ) : ( first == second && first == third ) ),
		length: isBin ? arrayLen( first ) : len( first ),
		hash: isBin ? hash( first ) : "",
		content: isBin ? "" : first,
		hasContentWithoutBody: structKeyExists( withoutBody, "content" ),
		formA: form.a ?: ""
	};
	writeOutput( serializeJSON( result ) );
</cfscript>
