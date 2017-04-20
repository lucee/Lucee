<cfscript>
	dynamiVar = 'static';

	try {
		data = '#dynamiVar#'::getData();
		writeOutput(data.bar);
	} catch ( any e ) {
		writeOutput(e.message);
	}
	
</cfscript>
