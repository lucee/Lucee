<cfscript>
	r = render( "<b>hi <cfoutput>#1+2#</cfoutput></b>", true );
	writeOutput( r & "|" );
</cfscript>
