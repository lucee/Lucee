<cfscript>
	try {
		throw message="Access Denied" type="MyCustomError";
	} 
	catch (any e) 
	{
		writeoutput(e.message);
	}
</cfscript>