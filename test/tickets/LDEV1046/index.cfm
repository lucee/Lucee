<cfscript>
	param name="FORM.Scene" default="1";

	try {
		if(FORM.Scene EQ 1){
			// Supported on both ACF & Lucee
			result1 = getApplicationMetadata();
		}else{
			// Supported only on Lucee
			result2 = getApplicationSettings();
		}
	}
	catch(any e) {
		writeOutput(e.Message);
	}
</cfscript>