<cfscript>
	try {
		o = new missingMethod();
		writeOutput( "FAIL: should have thrown error" );
	} catch ( any e ) {
		writeOutput( "PASS" );
	}
</cfscript>
