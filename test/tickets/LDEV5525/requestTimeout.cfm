<cfscript>
	// simulate what the request timeout monitor does: record the timeout stacktrace and interrupt the request thread
	getPageContext().setTimeoutStackTrace();
	createObject( "java", "java.lang.Thread" ).currentThread().interrupt();
	try {
		cfexecute( name="sleep", arguments=url.duration, timeout="10", variable="out" );
		echo( ucase( "finished_marker" ) );
	}
	catch ( any e ) {
		echo( ucase( "caught_marker" ) & ":" & e.message );
	}
	echo( ucase( "after_marker" ) );
</cfscript>
