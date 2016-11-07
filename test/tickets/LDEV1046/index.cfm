<cfscript>
	try {
		echo(serialize(getApplicationMetadata()));
	}
	catch(any e) {
		writeOutput(e.Message);
	}
</cfscript>