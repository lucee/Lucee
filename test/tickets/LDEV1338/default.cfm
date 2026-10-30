<cfscript>
	r = render( "<b>hi</b>" );
	// the rendered output went to the response before the following output
	writeOutput( "[" & r & "]|" );
</cfscript>
