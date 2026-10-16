<cfscript>
	try {
		myMissingFunction( "test" );
	} catch( any e ) {
		echo( e.message );
	}
</cfscript>