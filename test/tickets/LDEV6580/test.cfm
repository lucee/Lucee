<cfscript>
	param name="url.engine" default="";
	param name="url.action" default="inquiry";

	msg = "";
	try {
		if ( url.action == "models" ) {
			aiGetMetaData( url.engine, true );
		}
		else {
			ais = createAISession( url.engine, "be terse" );
			inquiryAISession( ais, "hello" );
		}
		msg = "no exception";
	}
	catch ( any e ) {
		msg = e.type & " " & e.message & " " & ( e.detail ?: "" );
	}
	echo( msg );
</cfscript>
