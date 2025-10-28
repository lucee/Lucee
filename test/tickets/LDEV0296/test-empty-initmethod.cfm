<cfscript>
	try {
		o = new emptyInitmethod();
		writeOutput( "FAIL: should have thrown error" );
	} catch ( any e ) {
		writeOutput( "PASS" );
	}
</cfscript>
