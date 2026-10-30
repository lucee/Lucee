<cfscript>
	r = render( '<outer><cfloop from="1" to="2" index="i"><cfoutput><inner>##i##</inner></cfoutput></cfloop></outer>', true );
	writeOutput( r & "|" );
</cfscript>
