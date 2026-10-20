<cfscript>
	try {
		echo( "OK:" & reFind( "b+", "abbbc" ) );
	}
	catch ( any e ) {
		echo( "ERR:" & e.message );
	}
</cfscript>
