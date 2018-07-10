<cfscript>
	try {
		obj = new Rate();
		result = obj.getRate();
	} catch ( any e ){
		result = e.message;
	}
	writeOutput(result);
</cfscript>