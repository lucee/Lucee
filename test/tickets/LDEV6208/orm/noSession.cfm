<cfscript>
	// ORM is enabled for this application, but nothing has touched the ORM yet
	writeOutput( serializeJSON( { "before": isWithinTransaction() } ) );
</cfscript>
