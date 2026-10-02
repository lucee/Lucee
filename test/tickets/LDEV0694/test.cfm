<cfscript>
	// Test that virtual directory CFML execution works via include
	try {
		include "/vdir/index.cfm";
		writeOutput( "SUCCESS: " );
	}
	catch ( any e ) {
		writeOutput( "FAIL: " & e.message );
	}
</cfscript>
