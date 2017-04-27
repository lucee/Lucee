<cfscript>
	try{
		result = isNull(a0?.b?.c?.d);
	}catch( any e ){
		result = e.message;
	}
	writeOutput(result);
</cfscript>