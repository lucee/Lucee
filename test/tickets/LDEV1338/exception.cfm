<cfscript>
	try {
		render( "before <cfthrow message='boom'> after", true );
		writeOutput( "not thrown" );
	}
	catch ( any e ) {
		// output of the failed render must not leak, and the response must still work
		writeOutput( e.message == "boom" ? "caught" : "wrong:" & e.message );
	}
	writeOutput( "|after" );
</cfscript>
