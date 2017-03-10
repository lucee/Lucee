<cfscript>
	try{
		obj = new Rate();
		result = obj.Name('test');
	} catch ( any e){
		result = e.message
	} 
	writeOutput(result);
</cfscript>