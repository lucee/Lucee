<cfscript>
	param name="url.useTouch" default="false";

	if ( url.useTouch ) {
		sessionTouch();
	}

	echo( serializeJSON( {
		cfid: session.cfid,
		useTouch: url.useTouch
	} ) );
</cfscript>
