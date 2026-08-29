<cfscript>
	// Test that virtual directory is rejected with wrong shared key
	try {
		include "/vdir/index.cfm";
		writeOutput( "FAIL: Virtual directory accessible with wrong key" );
	}
	catch ( any e ) {
		// Expected: should fail to resolve with wrong key
		writeOutput( "FAIL: " & e.message );
	}
</cfscript>
